package com.athiban.task_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedActionException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<String> handleProjectNotFound(ProjectNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidProjectStateException.class)
    public ResponseEntity<String> handleInvalidProjectState(InvalidProjectStateException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLock(){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Project was modified by another request. Please retry.");
    }
}

