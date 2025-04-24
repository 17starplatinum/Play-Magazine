package com.example.backend.exceptions;

import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.accepted.EmailSendingException;
import com.example.backend.exceptions.accepted.RequestPendingException;
import com.example.backend.exceptions.badcredentials.InvalidRequestException;
import com.example.backend.exceptions.dto.ErrorResponseDto;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "error", ex.getMessage(),
                        "stackTrace", Arrays.toString(ex.getStackTrace())
                )
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            InvalidRequestException.class
    })
    public ResponseEntity<Object> handleBadCredentialsException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Неверное имя пользователя или пароль",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            AppNotFoundException.class,
            CardNotFoundException.class
    })
    public ResponseEntity<Object> handleUserNotFoundException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Ресурс не найден",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            AlreadyInRoleException.class,
            AppAlreadyPurchasedException.class,
            AppUpToDateException.class,
            BudgetExceededException.class,
            CardAlreadyExistsException.class,
            InsufficientFundsException.class,
            InvalidApplicationConfigException.class,
            InvalidRoleAssignmentException.class,
            ReviewAlreadyExistsException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ResponseEntity<Object> handleBadPrerequisiteException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.PRECONDITION_FAILED.value(),
                "Условие, необходимое для действия, не соблюдено",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler({
            AppDownloadException.class,
            AppUpdateException.class,
            RequestPendingException.class,
            EmailSendingException.class
    })
    public ResponseEntity<Object> handleIncompleteProcessingException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
            HttpStatus.ACCEPTED.value(),
                "Запрос принят, но невозможно завершить",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(AppNotPurchasedException.class)
    public ResponseEntity<Object> handleAppNotPurchasedException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.PAYMENT_REQUIRED.value(),
                "Требуется платёж для совершения этого действия",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Вам не разрешен доступ к этому ресурсу",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.FORBIDDEN);
    }
}
