package com.epam.esm.utils.openfeign;

import com.epam.esm.utils.exceptions.RestApiClientException;
import com.epam.esm.utils.exceptions.RestApiServerException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static com.epam.esm.utils.AuthConstants.*;

@Import(JsonFormWriter.class)
@RequiredArgsConstructor
public class CustomFeignClientConfiguration {
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String requestUrl = response.request().url();
            HttpStatus responseStatus = HttpStatus.valueOf(response.status());
            if (responseStatus.is5xxServerError()) {
                return new RestApiServerException(AN_INTERNAL_SERVER_ERROR_OCCURRED_WHILE_PROCESSING_THE_REQUEST);
            } else if (responseStatus.is4xxClientError()) {
                return new RestApiClientException(ERROR_WHILE_MAKING_API_CALL_TO + requestUrl);
            } else {
                return new Exception(GENERIC_EXCEPTION);
            }
        };
    }

}