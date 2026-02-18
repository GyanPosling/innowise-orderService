package com.innowise.orderservice.model.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserInfoResponse {

    private Integer id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private String email;

    private boolean active;
}
