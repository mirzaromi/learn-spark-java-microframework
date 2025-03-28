package org.mirza.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.jetty.http.HttpStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private Integer status;
    private String message;
    private T data;

    public static <T> BaseResponse<T> generateSuccessResponse(String messaage, T data) {
        return new BaseResponse<>(HttpStatus.OK_200, messaage, data);
    }

    public static <T> BaseResponse<T> generateResponse(Integer status, String messaage, T data) {
        return new BaseResponse<>(status, messaage, data);
    }
}
