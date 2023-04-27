package zerobase.bud.oauth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CallbackController {
    @GetMapping("/login/oauth2/code/github")
    public String callback(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           Authentication authentication) {
        return "redirect:http://127.0.0.1:5173/";
    }
}
