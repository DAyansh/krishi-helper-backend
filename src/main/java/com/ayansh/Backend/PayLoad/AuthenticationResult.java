package com.ayansh.Backend.PayLoad;

import com.ayansh.Backend.Security.Response.UserInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseCookie;

    @Data
    @AllArgsConstructor
    public class AuthenticationResult {
        private final UserInfoResponse response;
        private final ResponseCookie jwtCookie; ;

    }
