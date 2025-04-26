package com.example.backend.exceptions;

import com.example.backend.exceptions.accepted.AppDownloadException;
import com.example.backend.exceptions.accepted.AppUpdateException;
import com.example.backend.exceptions.accepted.EmailSendingException;
import com.example.backend.exceptions.accepted.RequestPendingException;
import com.example.backend.exceptions.badcredentials.InvalidRequestException;
import com.example.backend.exceptions.conflict.AlreadyInRoleException;
import com.example.backend.exceptions.dto.ErrorResponseDto;
import com.example.backend.exceptions.notfound.AppNotFoundException;
import com.example.backend.exceptions.notfound.CardNotFoundException;
import com.example.backend.exceptions.paymentrequired.AppNotPurchasedException;
import com.example.backend.exceptions.prerequisites.*;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class
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
            InvalidRequestException.class,
            JwtException.class,
            WeakKeyException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Object> handleBadRequestException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Запрос не подходит для обработки",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            AppNotFoundException.class,
            CardNotFoundException.class,
            NoResourceFoundException.class
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

    @ExceptionHandler(AlreadyInRoleException.class)
    public ResponseEntity<Object> handleAlreadyInRoleException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Пользователь уже имеет роль, которого хочет приобрести",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException() {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "У вас нет прав на доступ к этому ресурсу",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(responseDto, HttpStatus.FORBIDDEN);
    }
}
