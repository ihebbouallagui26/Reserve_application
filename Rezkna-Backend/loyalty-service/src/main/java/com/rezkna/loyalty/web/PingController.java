package com.rezkna.loyalty.web;

import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PingController {

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("pong");
    }

    @GetMapping("/ping/not-found")
    public ApiResponse<Void> notFoundDemo() {
        throw new ResourceNotFoundException("Demo resource not found");
    }

    @GetMapping("/ping/validate")
    public ApiResponse<String> validateDemo(@RequestParam @NotBlank(message = "must not be blank") String name) {
        return ApiResponse.success("Hello, " + name);
    }
}
