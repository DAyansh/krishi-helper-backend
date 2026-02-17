package com.ayansh.Backend.Security.Requests;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String username ;
    private String password ;
}
