package org.example.demo1.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.demo1.common.ErrorCode;
import org.example.demo1.pojo.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException ex) {
        return Result.error(ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            IllegalArgumentException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.ServletRequestBindingException.class,
            MissingPathVariableException.class
    })
    public Result handleValidationException(Exception ex) {
        return Result.error("参数错误", ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统错误", ErrorCode.SYSTEM_ERROR);
    }
}
