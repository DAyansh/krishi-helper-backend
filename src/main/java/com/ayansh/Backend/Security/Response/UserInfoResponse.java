package com.ayansh.Backend.Security.Response;

import lombok.*;

import java.util.List;

@Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserInfoResponse {

        private long id ;
        private String username ;
        private String email ;
        private String jwtToken ;
        private List<String> roles ;

        public UserInfoResponse(long id, List<String> roles, String username, String email) {
            this.id = id;
            this.roles = roles;
            this.username = username;
            this.email = email;
        }
    }
