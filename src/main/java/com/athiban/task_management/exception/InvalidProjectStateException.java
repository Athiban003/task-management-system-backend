package com.athiban.task_management.exception;

public class InvalidProjectStateException extends RuntimeException{
    public InvalidProjectStateException(String msg){
        super(msg);
    }
}
