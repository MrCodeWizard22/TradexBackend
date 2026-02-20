package com.piyush.tradex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDTO {
    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String message;
}
