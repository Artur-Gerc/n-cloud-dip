package net.cloud.exception;

import net.cloud.dto.ErrorResponse;
import net.cloud.exception.authException.UserExistsException;
import net.cloud.exception.authException.UserNotFoundOrPasswordIncorrectException;
import net.cloud.exception.resourceException.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserNotFoundOrPasswordIncorrectException(UserNotFoundOrPasswordIncorrectException e) {

        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserExistsException(UserExistsException e) {
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUploadResourceException(UploadResourceException e){
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(InternalServerErrorException e){
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(NoDataException e){
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleResourceExistException(ResourceExistException e){
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleResourceExistException(InvalidDataException e){
        return new ResponseEntity<>(getErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        String errorMessage = String.join(", ", errors);
        return new ResponseEntity<>(new ErrorResponse(errorMessage), HttpStatus.BAD_REQUEST);
    }

    public ErrorResponse getErrorMessage(String errorMessage) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(errorMessage);
        return errorResponse;
    }
}
