import org.davidfabio.game.Score;
import org.davidfabio.utils.JSONFileManagement;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class JSONFileManagementTest {
    private static final String TEST_WRITE_SCORES_FILENAME = "src/main/resources/scores.testwrite.json";
    private static final String TEST_READ_SCORES_FILENAME = "src/main/resources/scores.testread.json";

    @BeforeEach
    public void resetFiles() {
        File file = new File(TEST_WRITE_SCORES_FILENAME);
        if (file.exists())
            file.delete();

        file = new File(TEST_READ_SCORES_FILENAME);
        if (file.exists())
            file.delete();
    }

    @Nested
    @DisplayName("Test empty Scores Write & Read")
    public class ScoresWriteReadEmptyTest {
        @Test
        @DisplayName("Write empty Scores to File")
        public void writeEmptyScoresToFile() {
            File file = new File(TEST_WRITE_SCORES_FILENAME);
            ArrayList<Score> scores = new ArrayList<>();
            JSONFileManagement.writeScoresToFile(file,scores);

            // Test Scores List was not modified
            assertEquals(0,scores.size());

            // Read File contents
            assertEquals(true,file.exists());

            String fileContents = getFileContentAsString(file);
            assertEquals("[]",fileContents);
        }

        @Test
        @DisplayName("Read empty Scores from File")
        public void readEmptyScoresFromFile() {
            File file = new File(TEST_READ_SCORES_FILENAME);
            ArrayList<Score> scores = new ArrayList<>();
            JSONFileManagement.writeScoresToFile(file,scores);

            ArrayList<Score> readScores = JSONFileManagement.initScoresFromFile(file);
            assertEquals(0,readScores.size());
        }
    }

    @Nested
    @DisplayName("Test some Scores Write & Read")
    public class ScoresWriteReadSomeTest {
        private static ArrayList<Score> scores;
        @BeforeAll
        public static void initScoresList() {
            scores = new ArrayList<>();
            Score score1 = new Score();
            score1.end(25);
            scores.add(score1);
            Score score2 = new Score();
            score2.end(11);
            scores.add(score2);
            Score score3 = new Score();
            score3.end(0);
            scores.add(score3);
        }

        @Test
        @DisplayName("Write some Scores to File")
        public void writeSomeScoresToFile() {
            File file = new File(TEST_WRITE_SCORES_FILENAME);
            JSONFileManagement.writeScoresToFile(file,scores);

            // Test Scores List was not modified
            assertEquals(3,scores.size());

            // Read File contents
            assertEquals(true,file.exists());

            String fileContents = getFileContentAsString(file);
            assertNotEquals("[]",fileContents);
            assertEquals(true,fileContents.contains("\"pickups\":25"));
            assertEquals(true,fileContents.contains("\"pickups\":11"));
            assertEquals(true,fileContents.contains("\"pickups\":0"));
        }

        @Test
        @DisplayName("Read some Scores from File")
        public void readSomeScoresFromFile() {
            File file = new File(TEST_READ_SCORES_FILENAME);
            JSONFileManagement.writeScoresToFile(file,scores);

            ArrayList<Score> readScores = JSONFileManagement.initScoresFromFile(file);
            assertEquals(scores.size(),readScores.size());
            assertEquals(scores.get(0).getPickups(),readScores.get(0).getPickups());
            assertEquals(scores.get(1).getUsername(),readScores.get(1).getUsername());
            assertEquals(scores.get(2).getDuration(),readScores.get(2).getDuration());
        }
    }

    private static String getFileContentAsString(File file) {
        String fileContents = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();

            while (line != null) {
                fileContents += line;
                line = reader.readLine();
                if (line != null)
                    fileContents += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContents;
    }
}
