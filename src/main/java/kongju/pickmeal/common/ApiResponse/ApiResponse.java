package kongju.pickmeal.common.ApiResponse;

import kongju.pickmeal.common.exception.ErrorCode;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null
        );
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(
                true,
                null,
                null
        );
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String detailMessage) {
        return new ApiResponse<>(
                false,
                null,
                ErrorResponse.builder()
                        .message(errorCode.getMessage())
                        .detail(detailMessage)
                        .build()
        );
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                false,
                null,
                ErrorResponse.builder()
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorResponse {
        private String message;
        private String detail;
    }

}
