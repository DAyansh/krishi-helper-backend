package com.ayansh.Backend.Security.Service;


import com.ayansh.Backend.Model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

 // import com.ayansh.E_Comm.Model.User;


    @Data
    @NoArgsConstructor
    public class UserDetailImpl implements UserDetails {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id ;
        private String username;
        @JsonIgnore
        private String password;
        private String email;
        private Collection<? extends GrantedAuthority> authorities ;

        public UserDetailImpl(Collection<? extends GrantedAuthority> authorities, String email, Long id, String password, String username) {
            this.authorities = authorities;
            this.email = email;
            this.id = id;
            this.password = password;
            this.username = username;

        }

        public static UserDetailImpl build(User user){
            List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                    .map(r->new SimpleGrantedAuthority(r.getRoleName().name()))
                    .collect(Collectors.toList());

            return new UserDetailImpl(
                    grantedAuthorities,
                    user.getEmail(),
                    user.getId(),
                    user.getPassword(),
                    user.getName());

        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities ;
        }

        @Override
        public String getPassword() {
            return password ;
        }

        @Override
        public String getUsername() {
            return username ;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean equals(Object obj){
            if(obj == this)
                return true;
            if(obj == null ||  obj.getClass() != getClass() )
                return false;
            UserDetailImpl user = (UserDetailImpl)obj;
            return Objects.equals(id, user.id);
        }
    }

