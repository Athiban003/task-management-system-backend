package com.athiban.task_management.service;

import com.athiban.task_management.AbstractIntegrationTest;
import com.athiban.task_management.dto.UpdateProjectRequest;
import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.Role;
import com.athiban.task_management.models.User;
import com.athiban.task_management.repository.ProjectRepository;
import com.athiban.task_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setup() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.MANAGER);
        testUser = userRepository.save(testUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        testUser.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
                )
        );

        testProject = new Project(
                testUser,
                "Concurrency Test Project",
                "Testing concurrent updates",
                LocalDate.now().plusDays(30)
        );
        testProject = projectRepository.save(testProject);
    }

    @Test
    void whenTenThreadsUpdateSameProject_thenOnlyOneSucceeds() throws InterruptedException {
        int numberOfThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated by Thread");
        request.setDescription("Concurrent update test");
        request.setDeadline(LocalDate.now().plusDays(60));

        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    testUser.getEmail(),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
                            )
                    );
                    startLatch.await();

                    projectService.updateProject(testProject.getId(), request);

                    successCount.incrementAndGet();
                    System.out.println("Thread " + threadId + " succeeded");

                } catch (ObjectOptimisticLockingFailureException e) {
                    conflictCount.incrementAndGet();
                    System.out.println("Thread " + threadId + " conflict (expected)");
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " unexpected error: " + e.getMessage());
                }
                finally {
                    SecurityContextHolder.clearContext();
                }
            });
            threads[i].start();
        }

        startLatch.countDown();

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Successes: " + successCount.get());
        System.out.println("Conflicts: " + conflictCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(9);
    }
}