package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;

@SpringBootTest
@Transactional
public abstract class BaseJPATest {
    // To prevent Spring from initializing the real Console
    //  (which tries to acquire a system terminal and hangs in a non-TTY environment)
    @MockBean
    protected Console console;
}