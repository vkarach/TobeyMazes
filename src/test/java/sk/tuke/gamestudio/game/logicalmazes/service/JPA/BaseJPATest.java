package sk.tuke.gamestudio.game.logicalmazes.service.JPA;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("server")
@Transactional
public abstract class BaseJPATest {
}
