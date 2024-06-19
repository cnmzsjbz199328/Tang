package com.tang0488;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import java.util.ArrayList;

@Controller
public class UserController {

    @Autowired
    private UserPool userPool;

    @GetMapping("/perform_login")
    public String login(HttpSession session) {
        String username = "user_" + UUID.randomUUID().toString();
        User user = new User(username);
        userPool.addUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("username", username);

        return "redirect:/game";
    }
}
