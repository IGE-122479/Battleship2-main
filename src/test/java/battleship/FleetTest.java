package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for the Fleet class")
class FleetTest {

    private Fleet fleet;
    private IShip barge;
    private IShip frigate;

    @BeforeEach
    void setUp() {
        fleet = new Fleet();
        // Barca (size 1) colocada numa posição segura
        barge = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        frigate = Ship.buildShip("fragata", Compass.NORTH, new Position(5, 5));
    }

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("New fleet starts empty")
    void newFleetIsEmpty() {
        assertTrue(fleet.getShips().isEmpty());
    }

    // ── addShip ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addShip returns true for a valid ship")
    void addShipValidReturnsTrue() {
        assertTrue(fleet.addShip(barge));
    }

    @Test
    @DisplayName("addShip adds the ship to the list")
    void addShipAddsToList() {
        fleet.addShip(barge);
        assertEquals(1, fleet.getShips().size());
    }

    @Test
    @DisplayName("addShip returns false for a ship outside the board")
    void addShipOutsideBoardReturnsFalse() {
        IShip outOfBounds = Ship.buildShip("barca", Compass.NORTH, new Position(-1, -1));
        assertFalse(fleet.addShip(outOfBounds));
    }

    @Test
    @DisplayName("addShip returns false when collision risk exists")
    void addShipCollisionReturnsFalse() {
        fleet.addShip(barge);
        // Mesmo sítio → colisão
        IShip duplicate = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        assertFalse(fleet.addShip(duplicate));
    }

    // ── getShips ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getShips returns all added ships")
    void getShipsReturnsAll() {
        fleet.addShip(barge);
        fleet.addShip(frigate);
        assertEquals(2, fleet.getShips().size());
    }

    // ── getShipsLike ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getShipsLike returns ships of the correct category")
    void getShipsLikeCorrectCategory() {
        fleet.addShip(barge);
        fleet.addShip(frigate);
        List<IShip> barges = fleet.getShipsLike("Barca");
        assertEquals(1, barges.size());
        assertEquals("Barca", barges.get(0).getCategory());
    }

    @Test
    @DisplayName("getShipsLike returns empty list for unknown category")
    void getShipsLikeUnknownCategory() {
        fleet.addShip(barge);
        assertTrue(fleet.getShipsLike("Galeao").isEmpty());
    }

    // ── getFloatingShips ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getFloatingShips returns all ships when none are sunk")
    void getFloatingShipsAllFloating() {
        fleet.addShip(barge);
        fleet.addShip(frigate);
        assertEquals(2, fleet.getFloatingShips().size());
    }

    @Test
    @DisplayName("getFloatingShips excludes sunk ships")
    void getFloatingShipsExcludesSunk() {
        fleet.addShip(barge);
        // Afundar a barca (size 1 → 1 tiro)
        barge.shoot(new Position(0, 0));
        assertEquals(0, fleet.getFloatingShips().size());
    }

    // ── getSunkShips ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSunkShips returns empty list when no ships are sunk")
    void getSunkShipsEmptyWhenNoneSunk() {
        fleet.addShip(barge);
        assertTrue(fleet.getSunkShips().isEmpty());
    }

    @Test
    @DisplayName("getSunkShips returns sunk ships")
    void getSunkShipsReturnsSunk() {
        fleet.addShip(barge);
        barge.shoot(new Position(0, 0));
        assertEquals(1, fleet.getSunkShips().size());
    }

    // ── shipAt ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("shipAt returns the ship occupying that position")
    void shipAtReturnsCorrectShip() {
        fleet.addShip(barge);
        IShip found = fleet.shipAt(new Position(0, 0));
        assertNotNull(found);
        assertEquals("Barca", found.getCategory());
    }

    @Test
    @DisplayName("shipAt returns null when no ship is at the position")
    void shipAtReturnsNullWhenEmpty() {
        fleet.addShip(barge);
        assertNull(fleet.shipAt(new Position(9, 9)));
    }

    // ── printStatus / printShips (smoke tests) ────────────────────────────────

    @Test
    @DisplayName("printStatus does not throw")
    void printStatusDoesNotThrow() {
        fleet.addShip(barge);
        assertDoesNotThrow(() -> fleet.printStatus());
    }

    @Test
    @DisplayName("printShipsByCategory does not throw")
    void printShipsByCategoryDoesNotThrow() {
        fleet.addShip(barge);
        assertDoesNotThrow(() -> fleet.printShipsByCategory("Barca"));
    }

    @Test
    @DisplayName("printFloatingShips does not throw")
    void printFloatingShipsDoesNotThrow() {
        fleet.addShip(barge);
        assertDoesNotThrow(() -> fleet.printFloatingShips());
    }

    @Test
    @DisplayName("printAllShips does not throw")
    void printAllShipsDoesNotThrow() {
        fleet.addShip(barge);
        assertDoesNotThrow(() -> fleet.printAllShips());
    }

    // ── createRandom ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createRandom returns a non-null fleet")
    void createRandomNotNull() {
        assertNotNull(Fleet.createRandom());
    }

    @Test
    @DisplayName("createRandom returns a fleet with 11 ships")
    void createRandomHas11Ships() {
        IFleet randomFleet = Fleet.createRandom();
        assertEquals(11, randomFleet.getShips().size());
    }
}
