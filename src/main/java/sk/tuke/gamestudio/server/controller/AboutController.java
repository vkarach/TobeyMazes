package sk.tuke.gamestudio.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import sk.tuke.gamestudio.game.logicalmazes.core.Game;

@Controller
@RequestMapping("/about")
public class AboutController {
    @RequestMapping
    public String about(Model model) {
        model.addAttribute("version", Game.version);
        model.addAttribute("author", Game.author);
        return "about";
    }
}
