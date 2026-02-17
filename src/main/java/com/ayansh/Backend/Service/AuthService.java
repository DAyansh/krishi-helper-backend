package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.*;
import com.ayansh.Backend.PayLoad.AuthenticationResult;
import com.ayansh.Backend.Repository.LanguageRepository;
import com.ayansh.Backend.Repository.RoleRepo;
import com.ayansh.Backend.Repository.UserRepo;
import com.ayansh.Backend.Security.JWT.JwtUtils;
import com.ayansh.Backend.Security.Requests.LoginRequest;
import com.ayansh.Backend.Security.Requests.SignUpRequest;
import com.ayansh.Backend.Security.Response.UserInfoResponse;
import com.ayansh.Backend.Security.Service.UserDetailImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AuthService {


    @Autowired
        UserRepo userRepo;

        @Autowired
        PasswordEncoder passwordEncoder;

        @Autowired
           RoleRepo roleRepo;

           @Autowired
           AuthenticationManager authenticationManager;

           @Autowired
           JwtUtils jwtUtils;


            private ModelMapper modelMapper;

            @Autowired
            private LanguageRepository languageRepository;

            public ResponseEntity<?> signUp (SignUpRequest signUpRequest){

            if (signUpRequest.getPassword() == null || signUpRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Password cannot be null or empty");
            }


            if (userRepo.existsByName(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest().body("Username is already in use");
            }

            if (userRepo.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email is already in use");
            }

            String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
            User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encodedPassword);

            String lang = signUpRequest.getLanguage();

        switch (lang) {
            case "", "ENGLISH" -> {
                Language language = languageRepository.findByLangName(AppLang.ENGLISH).orElseThrow(() -> new RuntimeException("Error: Language not found"));
                user.setLanguage(language);
            }
            case "HINDI" -> {
                Language language = languageRepository.findByLangName(AppLang.HINDI).orElseThrow(() -> new RuntimeException("Error: Language not found"));
                user.setLanguage(language);
            }
            case "PUNJABI" -> {
                Language language = languageRepository.findByLangName(AppLang.PUNJABI).orElseThrow(() -> new RuntimeException("Error: Language not found"));
                user.setLanguage(language);
            }
            case "MARATHI" -> {
                Language language = languageRepository.findByLangName(AppLang.MARATHI).orElseThrow(() -> new RuntimeException("Error: Language not found"));
                user.setLanguage(language);
            }
            default -> throw new RuntimeException("Error: Language not found");
        }

            Set<String> strRoles = signUpRequest.getRoles();
            Set<Roles> roles = new HashSet<>();

            if (strRoles == null) {
                Roles userRole = roleRepo.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
                if (userRole == null) {
                    throw new RuntimeException();
                }
            } else {
                strRoles.forEach(role -> {
                    if (role.equals("admin")) {
                        Roles admin = roleRepo.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(admin);
                    } else {
                        Roles userRole = roleRepo.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    }
                });
            }
            user.setRoles(roles);
        user.setPhone(signUpRequest.getPhone());
        user.setState(signUpRequest.getState());

        userRepo.save(user);
            return ResponseEntity.ok().build();
        }

        public UserInfoResponse getUserDetails (Authentication authentication){
            UserDetailImpl userDetailsImpli = (UserDetailImpl) authentication.getPrincipal();
            List<String> roles = userDetailsImpli.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

            return new UserInfoResponse(userDetailsImpli.getId(),  roles, userDetailsImpli.getUsername(), userDetailsImpli.getEmail());
        }

        public ResponseCookie logout() {
            return jwtUtils.getCleanJwtCookie();
        }

        public AuthenticationResult login(LoginRequest loginRequest){
            org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailImpl userDetailsImpli = (UserDetailImpl) authentication.getPrincipal();
            ResponseCookie cookie = jwtUtils.generateJwtCookie(userDetailsImpli);

            List<String> roles = userDetailsImpli.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            UserInfoResponse response = new UserInfoResponse(userDetailsImpli.getId(),
                    userDetailsImpli.getUsername(), userDetailsImpli.getEmail(), cookie.toString(), roles);

            return new AuthenticationResult(response, cookie);

        }

    }

