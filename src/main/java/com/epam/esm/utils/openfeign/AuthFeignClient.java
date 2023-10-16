package com.epam.esm.utils.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", configuration = CustomFeignClientConfiguration.class)
public interface AuthFeignClient {
    @GetMapping("api/v1/auth/role")
    String getRole(@RequestHeader(value = "Authorization") String authorizationHeader);
}