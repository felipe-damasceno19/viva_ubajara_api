package io.github.parqueubajara.api.handler;

import io.github.parqueubajara.api.exception.DuplicateEmailException;
import io.github.parqueubajara.api.exception.DuplicateRegistry;
import io.github.parqueubajara.api.exception.InvalidFileException;
import io.github.parqueubajara.api.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        List<ValidationErrorResponse.FieldMessage> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> new ValidationErrorResponse.FieldMessage(f.getField(), f.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(status).body(
                new ValidationErrorResponse(LocalDateTime.now(), status.value(),
                        "Erro de validação", fields)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Não encontrado", ex.getMessage(), request.getRequestURI())
        );
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<StandardError> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Arquivo muito grando", "O arquivo excede o tamanho máximo permitido de 5MB", request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleGeneric(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Erro interno", "Erro inesperado no servidor", request.getRequestURI())
        );
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<StandardError> hasInvalidFile(InvalidFileException ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Arquivo inválido", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<StandardError> handleIOException(IOException ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                            "Erro ao processar arquivo", "Não foi possível processar o arquivo enviado",
                                  request.getRequestURI())
                );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<StandardError> handleAccessDenied(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Acesso negado", "Você não tem permissão para esta ação", request.getRequestURI())
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<StandardError> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Erro ao realizar ação", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(DuplicateRegistry.class)
    public ResponseEntity<StandardError> handleDuplicateRegistry(DuplicateRegistry ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Erro ao realizar ação", ex.getMessage() , request.getRequestURI())
        );
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<StandardError> handleAuth(InternalAuthenticationServiceException ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value()
                        , "Erro ao realizar ação", "Usuário e/ou senha incorretos", request.getRequestURI())
        );
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<StandardError> handlePathVariable(MissingPathVariableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Erro ao realizar ação", "Erro! Parâmetros faltando", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardError> handleParameter (MissingPathVariableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
                new StandardError(LocalDateTime.now(), status.value(),
                        "Erro ao realizar ação", "Erro! Parametros vazios ou incorretos", request.getRequestURI()
                )
        );
    }

}
