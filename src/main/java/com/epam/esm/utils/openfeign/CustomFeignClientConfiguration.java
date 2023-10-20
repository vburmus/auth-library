package com.epam.esm.utils.openfeign;

import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.ContentType;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import feign.form.spring.SpringManyMultipartFilesWriter;
import feign.form.spring.SpringSingleMultipartFileWriter;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static com.epam.esm.utils.AuthConstants.*;

@Import(JsonFormWriter.class)
public class CustomFeignClientConfiguration {


    @Bean
    Encoder feignEncoder(JsonFormWriter jsonFormWriter) {
        return new SpringFormEncoder() {{
            var processor = (MultipartFormContentProcessor) getContentProcessor(ContentType.MULTIPART);
            processor.addFirstWriter(jsonFormWriter);
            processor.addFirstWriter(new SpringSingleMultipartFileWriter());
            processor.addFirstWriter(new SpringManyMultipartFilesWriter());
        }};
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            HttpStatus responseStatus = HttpStatus.valueOf(response.status());
            if (responseStatus.is5xxServerError()) {
                return new HttpServerErrorException(responseStatus,
                        AN_INTERNAL_SERVER_ERROR_OCCURRED_WHILE_PROCESSING_THE_REQUEST);
            } else if (responseStatus.is4xxClientError()) {
                return new HttpClientErrorException(responseStatus, response.body().toString());
            } else {
                return new Exception(GENERIC_EXCEPTION);
            }
        };
    }
}