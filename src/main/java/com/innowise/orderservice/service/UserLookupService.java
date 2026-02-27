package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserServiceClient;
import com.innowise.orderservice.exception.ServiceUnavailableException;
import com.innowise.orderservice.model.dto.response.UserInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserInfoResponse getUserByEmail(String email) {
        return userServiceClient.getUserByEmail(email);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUsersByEmailsFallback")
    public List<UserInfoResponse> getUsersByEmails(List<String> emails) {
        return userServiceClient.getUsersByEmails(emails);
    }

    @SuppressWarnings("unused")
    private UserInfoResponse getUserByEmailFallback(String email, Throwable ex) {
        throw new ServiceUnavailableException("User service is unavailable for email: " + email, ex);
    }

    @SuppressWarnings("unused")
    private List<UserInfoResponse> getUsersByEmailsFallback(List<String> emails, Throwable ex) {
        throw new ServiceUnavailableException("User service is unavailable for batch request", ex);
    }
}
