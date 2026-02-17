package com.ayansh.Backend.Security.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
