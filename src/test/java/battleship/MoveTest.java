package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for the Move class")
class MoveTest {

    private List<IPosition> shots;
    private List<IGame.ShotResult> results;
    private Move move;

    @BeforeEach
    void setUp() {
        shots = new ArrayList<>();
        shots.add(new Position('A', 1));
        shots.add(new Position('B', 2));
        shots.add(new Position('C', 3));

        results = new ArrayList<>();
        // tiro na água
        results.add(new IGame.ShotResult(true, false, null, false));
        // tiro repetido
        results.add(new IGame.ShotResult(true, true, null, false));
        // tiro inválido
        results.add(new IGame.ShotResult(false, false, null, false));

        move = new Move(1, shots, results);
    }

    // ── Constructor / getters ─────────────────────────────────────────────────

    @Test
    @DisplayName("getNumber returns the move number")
    void getNumberReturnsCorrectNumber() {
        assertEquals(1, move.getNumber());
    }

    @Test
    @DisplayName("getShots returns the correct shot list")
    void getShotsReturnsCorrectList() {
        assertEquals(3, move.getShots().size());
    }

    @Test
    @DisplayName("getShotResults returns the correct result list")
    void getShotResultsReturnsCorrectList() {
        assertEquals(3, move.getShotResults().size());
    }

    // ── duration ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getDuration returns 0 by default")
    void getDurationDefaultIsZero() {
        assertEquals(0L, move.getDuration());
    }

    @Test
    @DisplayName("setDuration stores the value correctly")
    void setDurationStoresValue() {
        move.setDuration(1234L);
        assertEquals(1234L, move.getDuration());
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString contains the move number")
    void toStringContainsMoveNumber() {
        assertTrue(move.toString().contains("1"));
    }

    // ── processEnemyFire – verbose = false ───────────────────────────────────

    @Test
    @DisplayName("processEnemyFire returns a JSON string")
    void processEnemyFireReturnsJson() {
        String json = move.processEnemyFire(false);
        assertNotNull(json);
        assertTrue(json.contains("validShots"));
    }

    @Test
    @DisplayName("processEnemyFire counts repeated shots correctly")
    void processEnemyFireCountsRepeated() {
        String json = move.processEnemyFire(false);
        assertTrue(json.contains("\"repeatedShots\" : 1"));
    }

    @Test
    @DisplayName("processEnemyFire counts missed shots correctly")
    void processEnemyFireCountsMissed() {
        String json = move.processEnemyFire(false);
        assertTrue(json.contains("\"missedShots\" : 1"));
    }

    // ── processEnemyFire – verbose = true ────────────────────────────────────

    @Test
    @DisplayName("processEnemyFire with verbose does not throw")
    void processEnemyFireVerboseDoesNotThrow() {
        assertDoesNotThrow(() -> move.processEnemyFire(true));
    }

    @Test
    @DisplayName("processEnemyFire verbose with only repeated shots does not throw")
    void processEnemyFireAllRepeatedVerbose() {
        List<IGame.ShotResult> allRepeated = List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        );
        Move allRepMove = new Move(2, shots, allRepeated);
        assertDoesNotThrow(() -> allRepMove.processEnemyFire(true));
    }

    @Test
    @DisplayName("processEnemyFire with a hit on a ship does not throw")
    void processEnemyFireWithHit() {
        IShip ship = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        List<IGame.ShotResult> hitResults = List.of(
                new IGame.ShotResult(true, false, ship, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );
        Move hitMove = new Move(3, shots, hitResults);
        assertDoesNotThrow(() -> hitMove.processEnemyFire(true));
    }

    @Test
    @DisplayName("processEnemyFire with a sunk ship records it correctly")
    void processEnemyFireWithSunkShip() {
        IShip ship = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        ship.shoot(new Position(0, 0)); // afundar
        List<IGame.ShotResult> sunkResults = List.of(
                new IGame.ShotResult(true, false, ship, true),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );
        Move sunkMove = new Move(4, shots, sunkResults);
        String json = sunkMove.processEnemyFire(false);
        assertTrue(json.contains("sunkBoats"));
    }

    // ── toDetailedString ──────────────────────────────────────────────────────

    @Test
    @DisplayName("toDetailedString returns a non-empty string")
    void toDetailedStringNotEmpty() {
        assertFalse(move.toDetailedString().isEmpty());
    }

    @Test
    @DisplayName("toDetailedString contains summary")
    void toDetailedStringContainsSummary() {
        assertTrue(move.toDetailedString().contains("Resumo"));
    }

    @Test
    @DisplayName("toDetailedString with sunk ship contains AFUNDADA")
    void toDetailedStringWithSunk() {
        IShip ship = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        ship.shoot(new Position(0, 0));
        List<IGame.ShotResult> sunkResults = List.of(
                new IGame.ShotResult(true, false, ship, true),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );
        Move sunkMove = new Move(5, shots, sunkResults);
        assertTrue(sunkMove.toDetailedString().contains("AFUNDADA"));
    }

    @Test
    @DisplayName("toDetailedString with invalid shot contains Inválido")
    void toDetailedStringWithInvalidShot() {
        List<IGame.ShotResult> invalidResults = List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );
        Move invalidMove = new Move(6, shots, invalidResults);
        assertTrue(invalidMove.toDetailedString().contains("Inválido"));
    }
}