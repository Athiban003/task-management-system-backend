package com.athiban.task_management.exception;

public class ProjectNotFoundException extends RuntimeException{
    public ProjectNotFoundException(String msg){
        super(msg);
    }
}
