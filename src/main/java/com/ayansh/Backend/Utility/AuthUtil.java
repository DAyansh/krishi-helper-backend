package com.ayansh.Backend.Utility;


import com.ayansh.Backend.Model.User;
import com.ayansh.Backend.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

    @Component
    public class AuthUtil {

        @Autowired
        UserRepo userRepo  ;

        public String loggedInEmail(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
            User user = userRepo.findByName(auth.getName());
            if(user==null){
                throw new UsernameNotFoundException("Username not found");
            }
            return user.getEmail() ;
        }

        public Long loggedInUserId(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
            User user = userRepo.findByName(auth.getName());
            if(user==null){
                throw new UsernameNotFoundException("Username not found");
            }
            return user.getId() ;
        }

        public User loggedInUser(){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
            User user = userRepo.findByName(auth.getName());
            if(user==null){
                throw new UsernameNotFoundException("Username not found");
            }
            return user ;
        }
    }
