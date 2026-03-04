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

    private String checkCorrectNameWithErrorMsg(String name) {
        if (name.isEmpty()) {
            return String.format("name '%s' is incorrect", name);
        }
        else if (name.length() < 3) {
            return String.format("name %s is to short (min 3)", name);
        }
        else if (name.length() > 16) {
            return String.format("name %s is to long (max 16)", name);
        }

        String blocked = "'\";:\\/|<>,.?*&%$#!@()[]{}=+~`^";

        for (char c : name.toCharArray()) {
            if (blocked.indexOf(c) >= 0) {
                return String.format("name '%s' can't contain '%c'", name, c);
            }
        }
        return null;
    }

    private void showError(String msg, int x, int y) {
        console.print(
            msg,
            x, y,
            AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)
        );

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        console.setCursorPosition(x, y);
        console.print(" ".repeat(200));
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

            if (userService.sessionTokenExpired(token)) {
                return null;
            }

            userService.updateSessionTokenExpireDate(token);

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

    private void updateSession(int userId) {
        String sessionToken = userService.getSessionTokenByUserId(userId); // todo into sep function
        if (sessionToken == null) {
            sessionToken = userService.generateSession(userId);
        }
        userService.updateSessionTokenExpireDate(sessionToken);
        saveSession(sessionToken);
    }

public User register() {
    console.clear();

    new TextRenderer(console).renderFromFile("uiTexts/register.txt");

    String name = getName();
    if (name == null) { // interrupted by user
        return null;
    }

    if (userService.userExists(name)) {
        // todo: error user with this name already exist
        return null;
    }

    int userId = userService.createUser(name);
    updateSession(userId);

    console.print(name + " now you registered!", 20, 20);

    waitForRefresh(20, 22);

    return new User(userId, name);
}

public User login() {
    console.clear();

    new TextRenderer(console).renderFromFile("uiTexts/login.txt");

    String name = getName();
    if (name == null) { // interrupted by user
        return null;
    }

    if (userService.userExists(name)) {
        int userId = userService.getUserIdByUserName(name);
        updateSession(userId);

        console.print(name + ", love to see ya again :)", 20, 20);

        waitForRefresh(20, 22);

        return new User(userId, name);
    }
    else {
//        todo: error user not exist
        return null;
    }

}

private String getName() {
    int x = 30;
    int y = 20;
    String prompt = "enter your name (Ctrl+D exit): ";
    console.setCursorPosition(x, y);

    console.exitRawMode();

    while (true) {
        console.print(" ".repeat(200), x, y);
        console.setCursorPosition(x, y);

        String name = console.readLine(prompt);
        if (name == null) {
            return null;
        }
        String errorMsg = checkCorrectNameWithErrorMsg(name);
        if (errorMsg != null) {
            showError(errorMsg, x, y + 2);
            continue;
        }

        console.print(" ".repeat(200), x, y);
        console.setCursorPosition(x, y);

        console.enterRawMode();

        return name;
    }
}

private void waitForRefresh(int x, int y) {
    console.print("▶ Refresh",
            x, y,
            AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
    );

    while (true) {
        InputType input = console.readAction();
        if (input == InputType.ENTER || input == InputType.QUIT) {
            return;
        }
    }
}

//    public User startLogin() {
//        console.clear();
//
//        console.exitRawMode();
//
//        new TextRenderer(console).renderFromFile("uiArts/login.txt", 0, 0);
//
//        int x = 30;
//        int y = 20;
//        String prompt = "enter your name (Ctrl+D exit): ";
//        console.setCursorPosition(x, y);
//
//        String name;
//        while (true) {
//            console.setCursorPosition(x, y);
//            console.print(" ".repeat(200));
//            console.setCursorPosition(x, y);
//
//            name = console.readLine(prompt);
//            if (name == null) {
//                return null;
//            }
//            String errorMsg = checkCorrectNameWithErrorMsg(name);
//            if (errorMsg != null) {
//                showError(name, x, y + 2, errorMsg);
//                continue;
//            }
//            break;
//        }
//
//        int userId;
//        if (userService.userExists(name)) {
//            userId = userService.getUserIdByUserName(name);
//            console.print(name + ", love to see ya again :)", x , y + 2);
//        }
//        else {
//            userId = userService.createUser(name);
//            console.print(name + " now you registered!", x, y + 2);
//        }
//
//        User user = new User(userId, name);
//
//        String sessionToken = userService.getSessionTokenByUserId(userId);
//        if (sessionToken == null) {
//             sessionToken = userService.generateSession(user.getId());
//        }
//        userService.updateSessionTokenExpireDate(sessionToken);
//        saveSession(sessionToken);
//
//        console.print("▶ Refresh",
//                x, y + 5,
//                AttributedStyle.DEFAULT.background(AttributedStyle.WHITE).foreground(AttributedStyle.BLACK)
//        );
//
//        while (true) {
//            InputType input = console.readAction();
//            if (input == InputType.ENTER || input == InputType.QUIT) {
//                return user;
//            }
//        }
//    }

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
