package sk.tuke.gamestudio.server.webservice;

import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.service.LevelService;

@RestController
@RequestMapping("/api/levels")
public class LevelServiceRest {
    private final LevelService levelService;

    public LevelServiceRest(LevelService levelService) {
        this.levelService = levelService;
    }

    @PutMapping("/{levelId}")
    public void addOrUpdateLevel(@PathVariable int levelId, @RequestParam String levelName) {
        levelService.addOrUpdateLevel(levelId, levelName);
    }
}
