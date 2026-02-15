package com.ayansh.Backend.Security.Service;


import com.ayansh.Backend.Model.User;
import com.ayansh.Backend.Repository.UserRepo;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//import com.ayansh.E_Comm.Model.User;
//import com.ayansh.E_Comm.Repository.UserRepo;



    @Service
    public class UserDetailServiceImpl implements UserDetailsService {

        @Autowired
        UserRepo userRepo;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepo.findByName(username) ;
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            return UserDetailImpl.build(user);
        }

    }

