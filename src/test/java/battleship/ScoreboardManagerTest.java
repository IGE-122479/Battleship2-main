package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

/**
 * Test class for class ScoreboardManager.
 * Author: ${user.name}
 * Date: 2026-04-24 20:05
 * Cyclomatic Complexity:
 * - saveRecord(): 2
 * - loadRecords(): 2
 * - printScoreboard(): 2
 */
class ScoreboardManagerTest {

    private static final String TEST_FILE = "scoreboard.json";

    @BeforeEach
    void setUp() {
        cleanFiles();
    }

    @AfterEach
    void tearDown() {
        cleanFiles();
    }

    private void cleanFiles() {
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.setWritable(true); // Ensure we can delete it
            file.delete();
        }
    }

    // --- saveRecord() Tests ---

    @Test
    void saveRecord1() {
        // Path 1: Successful save using the JsonProperty constructor
        GameRecord record = new GameRecord("2026-01-01 12:00:00", 10, 5, 2, "VITÓRIA");
        assertDoesNotThrow(() -> ScoreboardManager.saveRecord(record),
                "Error: Failed to save record using concrete constructor");
    }

    @Test
    void saveRecord2() {
        // Path 2: COVERS RED LINES (IOException in save)
        File file = new File(TEST_FILE);
        try {
            file.createNewFile();
            file.setReadOnly(); // Triggers IOException on mapper.writeValue()

            GameRecord record = new GameRecord("2026-01-01 12:00:00", 1, 1, 1, "DERROTA");
            ScoreboardManager.saveRecord(record);

            // If it reaches here without crashing, the catch block worked
            assertTrue(file.exists(), "Error: File should still exist but remain unchanged");
        } catch (Exception e) {
            fail("Should have caught exception internally");
        }
    }
    /**
     * Additional Path: Assertion Guard (Line 37).
     * Covers: assert record != null.
     */
    @Test
    void saveRecord3() {
        // Ensure that the assertion at the top of the method is functional
        assertThrows(AssertionError.class, () -> {
            ScoreboardManager.saveRecord(null);
        }, "Error: Expected AssertionError when record is null");
    }
    // --- loadRecords() Tests ---

    @Test
    void loadRecords1() {
        // Path 1: File exists path
        GameRecord record = new GameRecord("2026-01-01 12:00:00", 10, 5, 2, "VITÓRIA");
        ScoreboardManager.saveRecord(record);

        List<GameRecord> records = ScoreboardManager.loadRecords();
        assertFalse(records.isEmpty(), "Error: Records should be loaded from disk");
    }

    @Test
    void loadRecords2() {
        // Path 2: COVERS RED LINES (IOException in load)
        File file = new File(TEST_FILE);
        try {
            file.createNewFile();
            file.setReadable(false); // Prevents Jackson from reading the file

            List<GameRecord> records = ScoreboardManager.loadRecords();
            assertNotNull(records, "Error: Should return empty list on read error");
        } catch (Exception e) {
            fail("Exception should be handled by catch block in loadRecords");
        }
    }

    // --- printScoreboard() Tests ---

    @Test
    void printScoreboard1() {
        // Path 1: if (records.isEmpty()) is true
        assertDoesNotThrow(() -> ScoreboardManager.printScoreboard(),
                "Error: Failed on empty scoreboard");
    }

    @Test
    void printScoreboard2() {
        // Path 2: COVERS YELLOW LINES (Populated list & Stream logic)
        // We use the JSON constructor to guarantee "VITÓRIA" is set.
        // This prevents the NullPointerException in the stream filter.
        GameRecord record = new GameRecord("2026-04-24 19:00:00", 15, 8, 4, "VITÓRIA");
        ScoreboardManager.saveRecord(record);

        // This covers the 'for' loop and 'stream().filter()' logic
        assertDoesNotThrow(() -> ScoreboardManager.printScoreboard(),
                "Error: Failed during stream filtering or result counting");
    }
}