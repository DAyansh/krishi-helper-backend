package com.ayansh.Backend.Security.Requests;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
public class SignUpRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String username ;

    @NotBlank
    @Size(min = 1, max = 100)
    @Email
    private String email ;

    @NotBlank
    @Size(min = 1, max = 100)
    private String password ;

    private Set<String> roles ;

    private String language ;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phone ;

    @NotBlank
    private String state ;


}

