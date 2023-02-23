package com.mfw.atlas.provider.exceptions;



import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.ResponseResult;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseResult<?> MethodArgumentNotValidException(HttpServletResponse response, Exception e) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException(GlobalCodeEnum.GL_FAIL_9995);
    }

    /**
     * 统一处理参数校验错误异常
     *
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public ResponseResult<?> processValidException(HttpServletResponse response, BindException e) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        List<String> errorStringList = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        String errorMessage = String.join("; ", errorStringList);
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);

        return ResponseResult.fail(GlobalCodeEnum.GL_FAIL_9998.getCode(),errorMessage);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseResult<?> processValidException(HttpServletResponse response, BusinessException e) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.fail(e.getCode(), e.getMsg());
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<?> processDefaultException(HttpServletResponse response, Exception e) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        log.error(e.toString() + "_" + e.getMessage(), e);
        return ResponseResult.systemException(GlobalCodeEnum.GL_FAIL_9999);
    }
}
