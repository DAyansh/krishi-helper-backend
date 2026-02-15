package com.ayansh.Backend.Controller;


import com.ayansh.Backend.PayLoad.AuthenticationResult;
import com.ayansh.Backend.Security.Requests.LoginRequest;
import com.ayansh.Backend.Security.Requests.SignUpRequest;
import com.ayansh.Backend.Security.Response.MessageResponse;
import com.ayansh.Backend.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        System.out.println("=== SIGNUP REQUEST DEBUG ===");
        System.out.println("Username: " + signUpRequest.getUsername());
        System.out.println("Email: " + signUpRequest.getEmail());
        System.out.println("Password: " + signUpRequest.getPassword());
        System.out.println("Password is null: " + (signUpRequest.getPassword() == null));
        System.out.println("Password is empty: " + (signUpRequest.getPassword() != null && signUpRequest.getPassword().isEmpty()));
        System.out.println("Password is blank: " + (signUpRequest.getPassword() != null && signUpRequest.getPassword().trim().isEmpty()));
        System.out.println("Roles: " + signUpRequest.getRoles());
        System.out.println("=== END DEBUG ===");
        if(signUpRequest.getPassword() == null || signUpRequest.getPassword().trim().isEmpty()){
            return ResponseEntity.badRequest().body("Password is empty");
        }
        return authService.signUp(signUpRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        AuthenticationResult result = authService.login(loginRequest) ;

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE , result.getJwtCookie().toString()).body(result.getResponse());
    }

        @GetMapping("/username")
        public String getUsername(Authentication authentication) {

            if( authentication != null ){
                return authentication.getName() ;
            }
            return null ;
        }

        @GetMapping("/user")
        public ResponseEntity<?> getUserDetails(Authentication authentication){
            return ResponseEntity.ok().body(authService.getUserDetails(authentication)) ;
        }

        @PostMapping("/signout")
        public ResponseEntity<?> signout(){
            ResponseCookie cookie = authService.logout() ;
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                            cookie.toString())
                    .body(new MessageResponse("You've been signed out!"));
        }

    }

