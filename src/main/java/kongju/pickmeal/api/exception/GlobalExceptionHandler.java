package kongju.pickmeal.api.exception;

import kongju.pickmeal.common.ApiResponse.ApiResponse;
import kongju.pickmeal.common.exception.BusinessException;
import kongju.pickmeal.common.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception){
        ErrorCode errorCode = exception.getErrorCode();

        if(exception.getDetailMessage() != null){
            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(ApiResponse.fail(errorCode, exception.getDetailMessage()));
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }
}
