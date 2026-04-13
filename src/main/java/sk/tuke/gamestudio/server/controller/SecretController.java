package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/secret")
public class SecretController {
    @RequestMapping
    public String secret() {
        return "secret";
    }
}
