package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.GameMenu;

@SpringBootTest
@Transactional
public abstract class BaseJPATest {
    // Prevent real Console from acquiring a system terminal in non-TTY environment
    @MockBean
    protected Console console;

    // Prevent Game constructor from calling gameMenu.selectLevel() and blocking on input
    @MockBean
    protected GameMenu gameMenu;
}