package com.athiban.task_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false)
    private String name;
}
