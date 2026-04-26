package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Ship.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
public class ShipTest {

    private Ship ship;
    private Ship shipBorder;
    private Ship shipCaravel;


    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
        shipCaravel = new Caravel(Compass.NORTH, new Position(3, 3));
    }

    @AfterEach
    void tearDown() {
        ship = null;
        shipBorder = null;
        shipCaravel = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @Test
    void testShoot2() {
        Position target = new Position(0, 0);
        ship.shoot(target); // No exception expected
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit for an invalid target.");
    }

    /**
     * Test for the shoot method  (test assert).
     */
    @Test
    void testShoot3() {
        Ship ship = Ship.buildShip("barca", Compass.NORTH, new Position(0,0));
        IPosition outside = new Position(-1, 50);
        assertThrows(AssertionError.class, () -> ship.shoot(outside), "Error: Ship should be marked as hit.");
        assertThrows(AssertionError.class, () -> ship.shoot(null), "Error: shoot() should throw an AssertionError when given a null position.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @Test
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the occupies method (test assert).
     */
    @Test
    void testOccupies3() {
        assertThrows(AssertionError.class, () -> ship.occupies(null), "Error: occupies() should throw an AssertionError when given a null position.");
    }


    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @Test
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (test assert).
     */
    @Test
    void testTooCloseToShip3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null), "Error: occupies() should throw an AssertionError when given a null position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @Test
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (test assert).
     */
    @Test
    void testTooCloseToPosition3() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null), "Error: occupies() should throw an AssertionError when given a null position.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetTopMostPos1() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    @Test
    @DisplayName("[Ship] getTopMostPos2 – posições em ordem DECRESCENTE de linha: o if é acionado e top é atualizado")
    void testGetTopMostPos2() {
        Ship navioOrdemInversa = new Ship("Teste", Compass.NORTH, new Position(5, 0), 3) {};
        navioOrdemInversa.getPositions().add(new Position(5, 0));
        navioOrdemInversa.getPositions().add(new Position(3, 0));
        navioOrdemInversa.getPositions().add(new Position(1, 0));

        assertEquals(1, navioOrdemInversa.getTopMostPos(),
                "Erro: com posições [linha 5, 3, 1], getTopMostPos() deve devolver 1 " +
                        "(o ramo if deve ser acionado e top actualizado de 5 → 3 → 1).");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getAdjacentPositions method.
     * Cyclomatic Complexity: 5
     */
    @Test
    @DisplayName("testGetAdjacentPositions1 – contem a quantidade certas de posições adjacentes.")
    void testGetAdjacentPositions1() {
        assertEquals(8, ship.getAdjacentPositions().size(), "Erro: getAdjacentePositions() deve devolver 3 posições e devolveu " + ship.getAdjacentPositions().size() + " .");
        assertEquals(3, shipBorder.getAdjacentPositions().size(), "Erro: getAdjacentePositions() deve devolver 3 posições e devolveu " + shipBorder.getAdjacentPositions().size() + " .");
    }

    @Test
    @DisplayName("testGetAdjacentPositions2 – posição do próprio navio não é adicionada.")
    void testGetAdjacentPositions2() {
        List<IPosition> adjacentes = shipBorder.getAdjacentPositions();
        assertFalse(adjacentes.contains(shipBorder.getPosition()),
                "Erro: a posição (0,0) pertence ao navio e NÃO deve constar na lista de adjacentes.");
    }

    @Test
    @DisplayName("testGetAdjacentPositions3 – adjacente já existente não é duplicado.")
    void testGetAdjacentPositions3() {
        List<IPosition> adjacentes = shipCaravel.getAdjacentPositions();
        long contagem = adjacentes.stream()
                .filter(p -> p.getRow() == 2 && p.getColumn() == 3)
                .count();
        assertEquals(1, contagem,
                "Erro: a posição (2,3) é partilhada por ambas as células mas deve aparecer exactamente 1 vez.");
    }

    /**
     * Test for the getPosition method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("testGetPosition – devolve a posição inicial correcta")
    void testGetPosition() {
        IPosition pos = new Position(0, 0);
        assertEquals(pos, shipBorder.getPosition(), "Erro: getPosition() deve devolver " + pos + " e devolveu " + shipBorder.getPosition() + " .");
    }

    /**
     * Test for the toString method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[Ship] testToString – toString devolve uma string com as informações do ship.")
    void testToString() {
        String string = "[" + ship.getCategory() + " " + ship.getBearing() + " " + ship.getPosition() + "]";
        assertEquals(string, ship.toString(), "Erro: toString() deve devolver " + string + " e devolveu " + shipBorder.toString() + " .");
    }

    /**
     * Test for the buildShip method.
     * Cyclomatic Complexity: 7
     */
    @Test
    @DisplayName("[buildShip] buildShip1 – 'barca' devolve uma instância de Barge")
    void buildShip1() {
        Ship s = Ship.buildShip("barca", Compass.NORTH, new Position(0, 0));
        assertAll(
                () -> assertNotNull(s,
                        "Erro: buildShip('barca') não deve devolver null."),
                () -> assertInstanceOf(Barge.class, s,
                        "Erro: buildShip('barca') deve devolver uma instância de Barge.")
        );
    }

    @Test
    @DisplayName("[buildShip] buildShip2 – 'caravela' devolve uma instância de Caravel")
    void buildShip2() {
        Ship s = Ship.buildShip("caravela", Compass.NORTH, new Position(0, 0));
        assertAll(
                () -> assertNotNull(s,
                        "Erro: buildShip('caravela') não deve devolver null."),
                () -> assertInstanceOf(Caravel.class, s,
                        "Erro: buildShip('caravela') deve devolver uma instância de Caravel.")
        );
    }

    @Test
    @DisplayName("[buildShip] buildShip3 – 'nau' devolve uma instância de Carrack")
    void buildShip3() {
        Ship s = Ship.buildShip("nau", Compass.NORTH, new Position(0, 0));
        assertAll(
                () -> assertNotNull(s,
                        "Erro: buildShip('nau') não deve devolver null."),
                () -> assertInstanceOf(Carrack.class, s,
                        "Erro: buildShip('nau') deve devolver uma instância de Carrack.")
        );
    }

    @Test
    @DisplayName("[buildShip] buildShip4 – 'fragata' devolve uma instância de Frigate")
    void buildShip4() {
        Ship s = Ship.buildShip("fragata", Compass.NORTH, new Position(0, 0));
        assertAll(
                () -> assertNotNull(s,
                        "Erro: buildShip('fragata') não deve devolver null."),
                () -> assertInstanceOf(Frigate.class, s,
                        "Erro: buildShip('fragata') deve devolver uma instância de Frigate.")
        );
    }

    @Test
    @DisplayName("[buildShip] buildShip5 – 'galeao' devolve uma instância de Galleon")
    void buildShip5() {
        Ship s = Ship.buildShip("galeao", Compass.NORTH, new Position(0, 0));
        assertAll(
                () -> assertNotNull(s,
                        "Erro: buildShip('galeao') não deve devolver null."),
                () -> assertInstanceOf(Galleon.class, s,
                        "Erro: buildShip('galeao') deve devolver uma instância de Galleon.")
        );
    }

    @Test
    @DisplayName("[buildShip] buildShip6 – tipo desconhecido percorre o ramo default e devolve null")
    void buildShip6() {
        Ship s = Ship.buildShip("submarino", Compass.NORTH, new Position(0, 0));
        assertNull(s,
                "Erro: buildShip() com tipo desconhecido deve devolver null (ramo default).");
    }

    @Test
    @DisplayName("[buildShip] buildShip7 – testa os asserts")
    void buildShip7() {
        assertThrows(AssertionError.class, () -> Ship.buildShip(null, null, null), "Erro: buildShip() deve lançar um AssertionError quando os argumentos são null.");
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, new Position(0,0)), "Erro: buildShip() deve lançar um AssertionError quando o bearing é null.");
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null), "Erro: buildShip() deve lançar um AssertionError quando a posição é null.");

    }

}