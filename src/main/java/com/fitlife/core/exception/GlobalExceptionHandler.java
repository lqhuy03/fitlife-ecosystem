package com.fitlife.core.exception;

import com.fitlife.core.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j // Tự động inject Logger
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. CATCHING VALIDATION ERROR (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // CLEAN CODE: Truyền danh sách lỗi vào hàm error()
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Dữ liệu đầu vào không hợp lệ", errors));
    }

    // 2. CATCHING AUTHENTICATION ERROR
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        // CLEAN CODE
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "Tài khoản hoặc mật khẩu không chính xác!"));
    }

    // 3. CATCHING CUSTOM BUSINESS EXCEPTION
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode(); // Lấy ErrorCode từ Exception

        // Lấy đúng mã code từ Enum (ví dụ: 404, 400, 401) động theo cấu hình
        return ResponseEntity.status(errorCode.getCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // 4. CATCH-ALL: Caught any unwanted system errors (NPE, DB Error...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // Ghi log lỗi vào file/console để Dev check, không trả chi tiết cho Client tránh lộ bảo mật
        log.error("Lỗi hệ thống nghiêm trọng: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau!"));
    }
}