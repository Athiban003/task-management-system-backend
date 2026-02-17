package com.athiban.task_management.exception;

public class UnauthorizedActionException extends  RuntimeException{
    public UnauthorizedActionException(String msg){
        super(msg);
    }
}
