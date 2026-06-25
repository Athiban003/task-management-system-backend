package com.athiban.task_management.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail createProblemDetail(HttpStatus status, String detail,
                                              String title, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(type));
        problem.setProperty("timestamp", Instant.now());

        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }

        return problem;
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedActionException ex){
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                "Access Denied",
                "https://api.taskmanagement.com/errors/forbidden"
        );
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFound(ProjectNotFoundException ex){
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "Project Not Found",
                "https://api.taskmanagement.com/errors/not-found"
        );
    }

    @ExceptionHandler(InvalidProjectStateException.class)
    public ProblemDetail handleInvalidProjectState(InvalidProjectStateException ex) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                "Invalid State Transition",
                "https://api.taskmanagement.com/errors/invalid-state"
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(){
        ProblemDetail problem= createProblemDetail(
                HttpStatus.CONFLICT,
                "The resource was modified by another request. Please reload and try again.",
                "Concurrent Modification Detected",
                "https://api.taskmanagement.com/errors/optimistic-lock"
        );
        problem.setProperty("retryable", true);
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                "Authentication Failed",
                "https://api.taskmanagement.com/errors/authentication"
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ProblemDetail handleTokenExpired(TokenExpiredException ex) {
        ProblemDetail problem = createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                "Token Expired",
                "https://api.taskmanagement.com/errors/token-expired"
        );
        problem.setProperty("retryable", false);
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException ex){
        return createProblemDetail(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                "Email Already Exists",
                "https://api.taskmanagement.com/errors/email-exists"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields",
                "Validation Error",
                "https://api.taskmanagement.com/errors/validation"
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                "Internal Server Error",
                "https://api.taskmanagement.com/errors/internal"
        );
        log.error("Unexpected error: ",ex);
        return problem;
    }
}

