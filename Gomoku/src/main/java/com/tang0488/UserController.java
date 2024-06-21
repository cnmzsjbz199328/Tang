//package com.tang0488;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import jakarta.servlet.http.HttpSession;
//import java.util.UUID;
//import java.util.ArrayList;
//
//@Controller
//public class UserController {
//
//    @Autowired
//    private UserPool userPool;
//
//    @GetMapping("/perform_login")
//    public String login(HttpSession session) {
//        String username = "user_" + UUID.randomUUID().toString();
//        User user = new User(username);
//        userPool.addUser(user);
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        session.setAttribute("username", username);
//
//        return "redirect:/game";
//    }
//}
//package com.tang0488;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class UserController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
//        try {
//            // 尝试使用提供的用户名和密码进行认证
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//            // 认证成功，将认证信息设置到安全上下文
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            return ResponseEntity.ok("Login successful.");
//        } catch (AuthenticationException e) {
//            // 认证失败，返回错误信息
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
//        }
//    }
//}

