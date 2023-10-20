package com.epam.esm.utils.openfeign;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.InputStream;

import static com.epam.esm.utils.AuthConstants.AN_INTERNAL_SERVER_ERROR_OCCURRED_WHILE_PROCESSING_THE_REQUEST;
import static com.epam.esm.utils.AuthConstants.GENERIC_EXCEPTION;

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
            ExceptionMessage message;
            try (InputStream bodyIs = response.body()
                    .asInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                message = mapper.readValue(bodyIs, ExceptionMessage.class);
            } catch (IOException e) {
                return new Exception(e.getMessage());
            }
            HttpStatus responseStatus = HttpStatus.valueOf(response.status());
            if (responseStatus.is5xxServerError()) {
                return new HttpServerErrorException(responseStatus,
                        AN_INTERNAL_SERVER_ERROR_OCCURRED_WHILE_PROCESSING_THE_REQUEST);
            } else if (responseStatus.is4xxClientError()) {
                return new HttpClientErrorException(responseStatus, message.detail());
            } else {
                return new Exception(GENERIC_EXCEPTION);
            }
        };
    }
}