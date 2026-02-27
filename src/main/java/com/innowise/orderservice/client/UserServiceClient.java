package com.innowise.orderservice.client;

import com.innowise.orderservice.config.FeignConfig;
import com.innowise.orderservice.model.dto.response.UserInfoResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url = "${userservice.base-url}",
        configuration = FeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/by-email")
    UserInfoResponse getUserByEmail(@RequestParam("email") String email);

    @PostMapping("/api/users/batch")
    List<UserInfoResponse> getUsersByEmails(@RequestBody List<String> emails);
}
