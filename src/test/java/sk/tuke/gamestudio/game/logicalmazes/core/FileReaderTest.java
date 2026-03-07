package sk.tuke.gamestudio.game.logicalmazes.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class FileReaderTest {
    @Test
    public void fileExistTest() {
        assertFalse(FileReader.checkFileExists("not_exist"));
        assertTrue(FileReader.checkFileExists("maps/test_map.txt"));
    }

    @Test
    public void readFileLinesCheck() {
        List<String> expected = List.of(
                "W=3 H=3",
                "",
                "TILES",
                "..S",
                "!..",
                "..!",
                "",
                "VERT",
                "0001",
                "0000",
                "0000",
                "0000",
                "",
                "HORZ",
                "001",
                "000",
                "000",
                "000"
        );
        List<String> fileLines = FileReader.readFileLines("maps/test_map.txt");
        assertEquals(expected, fileLines);
    }
}
