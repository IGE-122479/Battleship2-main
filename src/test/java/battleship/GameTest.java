package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for the Game class")
class GameTest {

    private Game game;
    private IFleet myFleet;

    @BeforeEach
    void setUp() {
        myFleet = Fleet.createRandom();
        game = new Game(myFleet);
    }

    // ── Constructor / getters ─────────────────────────────────────────────────

    @Test
    @DisplayName("getMyFleet returns the fleet passed to the constructor")
    void getMyFleetReturnsCorrectFleet() {
        assertSame(myFleet, game.getMyFleet());
    }

    @Test
    @DisplayName("getAlienFleet returns a non-null fleet")
    void getAlienFleetNotNull() {
        assertNotNull(game.getAlienFleet());
    }

    @Test
    @DisplayName("getMyMoves starts empty")
    void getMyMovesStartsEmpty() {
        assertTrue(game.getMyMoves().isEmpty());
    }

    @Test
    @DisplayName("getAlienMoves starts empty")
    void getAlienMovesStartsEmpty() {
        assertTrue(game.getAlienMoves().isEmpty());
    }

    @Test
    @DisplayName("initial repeated shots count is zero")
    void initialRepeatedShotsIsZero() {
        assertEquals(0, game.getRepeatedShots());
    }

    @Test
    @DisplayName("initial invalid shots count is zero")
    void initialInvalidShotsIsZero() {
        assertEquals(0, game.getInvalidShots());
    }

    @Test
    @DisplayName("initial hits count is zero")
    void initialHitsIsZero() {
        assertEquals(0, game.getHits());
    }

    @Test
    @DisplayName("initial sunk ships count is zero")
    void initialSunkShipsIsZero() {
        assertEquals(0, game.getSunkShips());
    }

    @Test
    @DisplayName("getRemainingShips returns 11 at game start")
    void getRemainingShipsInitial() {
        assertEquals(11, game.getRemainingShips());
    }

    @Test
    @DisplayName("getAlienRemainingShips returns 11 at game start")
    void getAlienRemainingShipsInitial() {
        assertEquals(11, game.getAlienRemainingShips());
    }

    // ── fireSingleShot ────────────────────────────────────────────────────────

    @Test
    @DisplayName("fireSingleShot on invalid position returns invalid result")
    void fireSingleShotInvalidPosition() {
        IGame.ShotResult result = game.fireSingleShot(new Position(-1, -1), false);
        assertFalse(result.valid());
    }

    @Test
    @DisplayName("fireSingleShot repeated returns repeated result")
    void fireSingleShotRepeated() {
        IGame.ShotResult result = game.fireSingleShot(new Position(0, 0), true);
        assertTrue(result.repeated());
    }

    @Test
    @DisplayName("fireSingleShot on water returns valid, non-repeated, no ship")
    void fireSingleShotOnWater() {
        // Garante tiro numa posição sem navio — percorre até encontrar água
        Position waterPos = null;
        for (int r = 0; r < Game.BOARD_SIZE; r++) {
            for (int c = 0; c < Game.BOARD_SIZE; c++) {
                Position p = new Position(r, c);
                if (myFleet.shipAt(p) == null) {
                    waterPos = p;
                    break;
                }
            }
            if (waterPos != null) break;
        }
        assertNotNull(waterPos);
        IGame.ShotResult result = game.fireSingleShot(waterPos, false);
        assertTrue(result.valid());
        assertFalse(result.repeated());
        assertNull(result.ship());
    }

    // ── repeatedShot ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("repeatedShot returns false when no moves have been made")
    void repeatedShotFalseInitially() {
        assertFalse(game.repeatedShot(new Position(0, 0)));
    }

    // ── myRepeatedShot ────────────────────────────────────────────────────────

    @Test
    @DisplayName("myRepeatedShot returns false when no my moves have been made")
    void myRepeatedShotFalseInitially() {
        assertFalse(game.myRepeatedShot(new Position(0, 0)));
    }

    // ── jsonShots ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("jsonShots returns a valid JSON string")
    void jsonShotsReturnsJson() {
        List<IPosition> shots = List.of(
                new Position('A', 1),
                new Position('B', 2),
                new Position('C', 3)
        );
        String json = Game.jsonShots(shots);
        assertNotNull(json);
        assertTrue(json.contains("row"));
        assertTrue(json.contains("column"));
    }

    @Test
    @DisplayName("jsonShots with empty list returns empty JSON array")
    void jsonShotsEmptyList() {
        String json = Game.jsonShots(List.of());
        assertNotNull(json);
        assertTrue(json.contains("[]") || json.contains("[ ]"));
    }

    // ── fireShots ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fireShots with wrong number of shots throws IllegalArgumentException")
    void fireShotsWrongCountThrows() {
        List<IPosition> twoShots = List.of(new Position('A', 1), new Position('B', 2));
        assertThrows(IllegalArgumentException.class, () -> game.fireShots(twoShots));
    }

    @Test
    @DisplayName("fireShots with correct number of shots does not throw")
    void fireShotsCorrectCount() {
        List<IPosition> threeShots = List.of(
                new Position('A', 1),
                new Position('B', 2),
                new Position('C', 3)
        );
        assertDoesNotThrow(() -> game.fireShots(threeShots));
    }

    // ── readEnemyFire ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("readEnemyFire with valid JSON returns a JSON result")
    void readEnemyFireValidJson() {
        String json = "[{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";
        String result = game.readEnemyFire(json);
        assertNotNull(result);
        assertTrue(result.contains("validShots"));
    }

    @Test
    @DisplayName("readEnemyFire with invalid JSON throws RuntimeException")
    void readEnemyFireInvalidJsonThrows() {
        assertThrows(RuntimeException.class, () -> game.readEnemyFire("not json"));
    }

    // ── sendMyShotsJson ───────────────────────────────────────────────────────

    @Test
    @DisplayName("sendMyShotsJson with valid JSON returns a result")
    void sendMyShotsJsonValid() {
        String json = "[{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";
        String result = game.sendMyShotsJson(json);
        assertNotNull(result);
        assertTrue(result.contains("validShots"));
    }

    @Test
    @DisplayName("sendMyShotsJson with wrong number of shots throws IllegalArgumentException")
    void sendMyShotsJsonWrongCountThrows() {
        String json = "[{\"row\":\"A\",\"column\":1}]";
        assertThrows(IllegalArgumentException.class, () -> game.sendMyShotsJson(json));
    }

    // ── printBoard ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("printBoard without shots and without legend does not throw")
    void printBoardNoShotsNoLegend() {
        assertDoesNotThrow(() -> Game.printBoard(myFleet, List.of(), false, false));
    }

    @Test
    @DisplayName("printBoard with shots and with legend does not throw")
    void printBoardWithShotsAndLegend() {
        assertDoesNotThrow(() -> Game.printBoard(myFleet, List.of(), true, true));
    }

    // ── printMyBoard / printAlienBoard ────────────────────────────────────────

    @Test
    @DisplayName("printMyBoard does not throw")
    void printMyBoardDoesNotThrow() {
        assertDoesNotThrow(() -> game.printMyBoard(true, true));
    }

    @Test
    @DisplayName("printAlienBoard does not throw")
    void printAlienBoardDoesNotThrow() {
        assertDoesNotThrow(() -> game.printAlienBoard(true, true));
    }

    // ── randomEnemyFire ───────────────────────────────────────────────────────

    @Test
    @DisplayName("randomEnemyFire returns a JSON string")
    void randomEnemyFireReturnsJson() {
        String result = game.randomEnemyFire();
        assertNotNull(result);
        assertTrue(result.contains("validShots"));
    }

    @Test
    @DisplayName("randomEnemyFire adds a move to alienMoves")
    void randomEnemyFireAddsMove() {
        game.randomEnemyFire();
        assertEquals(1, game.getAlienMoves().size());
    }

    // ── printTimingStats ──────────────────────────────────────────────────────

    @Test
    @DisplayName("printTimingStats with no moves does not throw")
    void printTimingStatsNoMoves() {
        assertDoesNotThrow(() -> game.printTimingStats());
    }

    @Test
    @DisplayName("printTimingStats after moves does not throw")
    void printTimingStatsWithMoves() {
        String json = "[{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";
        game.sendMyShotsJson(json);
        assertDoesNotThrow(() -> game.printTimingStats());
    }
}