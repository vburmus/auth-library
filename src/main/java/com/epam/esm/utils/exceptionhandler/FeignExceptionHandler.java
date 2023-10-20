package com.epam.esm.utils.exceptionhandler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import static com.epam.esm.utils.AuthConstants.API_CALL_ERROR;
import static com.epam.esm.utils.AuthConstants.INTERNAL_SERVER_ERROR;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class FeignExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Problem> handleRestApiClientException(HttpClientErrorException e) {
        Problem problem = buildProblem(Status.valueOf(e.getStatusCode().value()), API_CALL_ERROR, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Problem> handleRestApiServerException(HttpServerErrorException e) {
        Problem problem = buildProblem(Status.valueOf(e.getStatusCode().value()), INTERNAL_SERVER_ERROR,
                e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private Problem buildProblem(Status status, String title, String detail) {
        return Problem.builder()
                .withStatus(status)
                .withTitle(title)
                .withDetail(detail)
                .build();
    }
}