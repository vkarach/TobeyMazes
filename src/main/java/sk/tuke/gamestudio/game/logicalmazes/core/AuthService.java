package sk.tuke.gamestudio.game.logicalmazes.core;

import org.jline.utils.AttributedStyle;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.game.logicalmazes.console.Console;
import sk.tuke.gamestudio.game.logicalmazes.console.TextRenderer;
import sk.tuke.gamestudio.service.UserServiceJDBC;
import java.nio.file.NoSuchFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthService {
    private final Console console;
    private final UserServiceJDBC userService;

    public AuthService(Console console) {
        this.console = console;
        this.userService = new UserServiceJDBC();
    }

    private boolean correctName(String name) {
        String blocked = "'\";:\\/|<>,.?*&%$#!@()[]{}=+~`^";

        for (char c : name.toCharArray()) {
            if (blocked.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }

    private void showNameError(String name, int x, int y) {
        console.print(
            String.format("name '%s' is incorrect!", name),
            x, y,
            AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
        );

        try {
            Thread.sleep(1500);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        console.setCursorPosition(x, y);
        console.print(" ".repeat(50));
    }

    private void saveSession(String sessionToken) {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");
        try {
            Files.createDirectories(sessionDir);
            Path sessionFile = sessionDir.resolve("session.token");
            Files.writeString(sessionFile, sessionToken);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User loadUserSession() {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");

        try {
            Files.createDirectories(sessionDir);
            Path sessionFile = sessionDir.resolve("session.token");
            String token = Files.readString(sessionFile);

            int userId = userService.getUserIdBySessionToken(token);
            String userName = userService.getUserNameByUserId(userId);

            return new User(userId, userName);
        }
        catch (NoSuchFileException e) {
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User startLogin() {
        console.clear();

        console.exitRawMode();

        new TextRenderer(console).renderFromFile("uiTexts/login.txt", 0, 0);

        int x = 30;
        int y = 20;
        String prompt = "enter your name: ";
        console.setCursorPosition(x, y);

        String name;
        do {
            console.setCursorPosition(x, y);
            console.print(" ".repeat(50));
            console.setCursorPosition(x, y);

            name = console.readLine(prompt);
            if (!correctName(name)) {
                showNameError(name, x, y + 2);
            }
        }
        while (!correctName(name));

        int userId;
        if (userService.userExists(name)) {
            userId = userService.getUserIdByUserName(name);
            console.print(name + ", love to see ya again :)", x , y + 2);
        }
        else {
            userId = userService.createUser(name);
            console.print(name + " now you registered!", x, y + 2);
        }

        User user = new User(userId, name);

        String sessionToken = userService.getSessionTokenByUserId(userId);
        if (sessionToken == null) {
             sessionToken = userService.generateSession(user.getId());
        }
        saveSession(sessionToken);

        console.print("▶ Refresh",
                x, y + 5,
                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
        );

        while (true) {
            InputType input = console.readAction();
            if (input == InputType.ENTER || input == InputType.QUIT) {
                return user;
            }
        }
    }

    public void deleteSession() {
        String baseDir = System.getProperty("user.home");
        Path sessionDir = Paths.get(baseDir, ".logicalmaze");
        Path sessionFile = sessionDir.resolve("session.token");
        try {
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
