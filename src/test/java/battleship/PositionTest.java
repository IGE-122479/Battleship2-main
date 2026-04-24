

package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class PositionTest {

	private Position position;

	@BeforeEach
	public void setUp() {
		// Initialize with a standard valid position (B3 -> row 1, col 2)
		position = new Position(1, 2);
	}

	@AfterEach
	public void tearDown() {
		position = null;
	}

	@Test
	public void randomPosition() {
		Position rand = Position.randomPosition();
		assertNotNull(rand, "Error: randomPosition should not return null");
	}

	@Test
	public void constructorCharInt() {
		Position p = new Position('B', 3);
		assertAll("Constructor with char/int should initialize correctly",
				() -> assertEquals(1, p.getRow(), "Error: row should be 1 for 'B'"),
				() -> assertEquals(2, p.getColumn(), "Error: column should be 2 for 3")
		);
	}

	@Test
	public void constructorIntInt() {
		Position p = new Position(5, 5);
		assertAll("Constructor with int/int should initialize correctly",
				() -> assertEquals(5, p.getRow(), "Error: row should be 5"),
				() -> assertEquals(5, p.getColumn(), "Error: column should be 5")
		);
	}

	@Test
	public void getRow() {
		assertEquals(1, position.getRow(), "Error: expected row 1");
	}

	@Test
	public void getColumn() {
		assertEquals(2, position.getColumn(), "Error: expected column 2");
	}

	@Test
	public void getClassicRow() {
		assertEquals('B', position.getClassicRow(), "Error: expected classic row 'B' for index 1");
	}

	@Test
	public void getClassicColumn() {
		assertEquals(3, position.getClassicColumn(), "Error: expected classic column 3 for index 2");
	}
	/**
	 * CC: 5 (Cyclomatic Complexity for the compound condition row >= 0 && column >= 0 && row < Game.BOARD_SIZE && column < Game.BOARD_SIZE)
	 * Path 1: row < 0 (First condition fails, short-circuit)
	 */
	@Test
	void isInside1() {
		Position p = new Position(-1, 0);
		assertFalse(p.isInside(), "Error: row -1 should be outside (row >= 0 is false)");
	}

	/**
	 * CC: 5. Path 2: row >= 0 is true, but column < 0 (Second condition fails)
	 */
	@Test
	void isInside2() {
		Position p = new Position(0, -1);
		assertFalse(p.isInside(), "Error: column -1 should be outside (column >= 0 is false)");
	}

	/**
	 * CC: 5. Path 3: row >= 0 and column >= 0 are true, but row >= BOARD_SIZE (Third condition fails)
	 */
	@Test
	void isInside3() {
		// Assuming Game.BOARD_SIZE is 10
		Position p = new Position(10, 0);
		assertFalse(p.isInside(), "Error: row 10 should be outside (row < Game.BOARD_SIZE is false)");
	}

	/**
	 * CC: 5. Path 4: First three conditions are true, but column >= BOARD_SIZE (Fourth condition fails)
	 */
	@Test
	void isInside4() {
		Position p = new Position(0, 10);
		assertFalse(p.isInside(), "Error: column 10 should be outside (column < Game.BOARD_SIZE is false)");
	}

	/**
	 * CC: 5. Path 5: All conditions are true (Full success)
	 */
	@Test
	void isInside5() {
		Position p = new Position(5, 5);
		assertTrue(p.isInside(), "Error: Position (5, 5) should be inside the board");
	}
	/**
	 * CC: 2. Path 1: Positions are adjacent
	 */
	/**
	 * CC: 3 (Based on compound condition A && B)
	 * Path 1: Math.abs(this.row - other.getRow()) > 1
	 * The first condition fails, so the second is never evaluated (Short-circuit).
	 */
	@Test
	void isAdjacentTo1() {
		Position other = new Position(position.getRow() + 2, position.getColumn());
		assertFalse(position.isAdjacentTo(other),
				"Error: Positions with row difference > 1 should not be adjacent.");
	}

	/**
	 * CC: 3. Path 2: Math.abs(this.row - other.getRow()) <= 1 is TRUE,
	 * but Math.abs(this.column - other.getColumn()) > 1 is FALSE.
	 */
	@Test
	void isAdjacentTo2() {
		Position other = new Position(position.getRow(), position.getColumn() + 2);
		assertFalse(position.isAdjacentTo(other),
				"Error: Positions with column difference > 1 should not be adjacent.");
	}

	/**
	 * CC: 3. Path 3: Both conditions are TRUE.
	 * This covers horizontal, vertical, and diagonal adjacency.
	 */
	@Test
	void isAdjacentTo3() {
		// Test diagonal adjacency (Row diff = 1, Col diff = 1)
		Position diagonal = new Position(position.getRow() + 1, position.getColumn() + 1);

		// Test same position (Row diff = 0, Col diff = 0)
		Position same = new Position(position.getRow(), position.getColumn());

		assertAll("Verifying adjacent and overlapping positions",
				() -> assertTrue(position.isAdjacentTo(diagonal),
						"Error: Diagonal positions should be adjacent."),
				() -> assertTrue(position.isAdjacentTo(same),
						"Error: The same position is technically adjacent (diff <= 1).")
		);
	}

	/**
	 * CC: 2. Path 1: Loop processes directions and identifies inside positions
	 */
	@Test
	public void adjacentPositions1() {
		List<IPosition> adjs = position.adjacentPositions();
		assertFalse(adjs.isEmpty(), "Error: adjacent positions list should not be empty for a central position");
	}

	/**
	 * CC: 2. Path 2: Check boundary behavior (where some directions result in isInside() being false)
	 */
	@Test
	public void adjacentPositions2() {
		Position corner = new Position(0, 0);
		List<IPosition> adjs = corner.adjacentPositions();
		// At (0,0), only East, South, and Southeast are inside (3 positions)
		assertEquals(3, adjs.size(), "Error: corner position (0,0) should only have 3 valid adjacent positions");
	}

	@Test
	public void isOccupied() {
		assertFalse(position.isOccupied(), "Error: new position should not be occupied");
	}

	@Test
	public void isHit() {
		assertFalse(position.isHit(), "Error: new position should not be hit");
	}

	@Test
	public void occupy() {
		position.occupy();
		assertTrue(position.isOccupied(), "Error: position should be occupied after calling occupy()");
	}

	@Test
	public void shoot() {
		position.shoot();
		assertTrue(position.isHit(), "Error: position should be hit after calling shoot()");
	}

	/**
	 * CC: 5. Path 1: Identity comparison (this == otherPosition)
	 * Verifies the first 'if' branch returns true immediately.
	 */
	@Test
	void equals1() {
		assertTrue(position.equals(position),
				"Error: A position must be equal to itself (reference equality).");
	}

	/**
	 * CC: 5. Path 2: Not an instance of IPosition
	 * Verifies the 'instanceof' check fails and returns the final false.
	 */
	@Test
	void equals2() {
		assertFalse(position.equals("B3"),
				"Error: A Position should not be equal to a String or other types.");
	}

	/**
	 * CC: 5. Path 3: Instance of IPosition but row differs (this.row == other.getRow() is false)
	 * Tests the first part of the compound condition with short-circuiting.
	 */
	@Test
	void equals3() {
		Position differentRow = new Position(position.getRow() + 1, position.getColumn());
		assertFalse(position.equals(differentRow),
				"Error: Positions with different rows should not be equal.");
	}

	/**
	 * CC: 5. Path 4: Row is the same, but column differs (this.column == other.getColumn() is false)
	 * Tests the second part of the compound condition.
	 */
	@Test
	void equals4() {
		Position differentCol = new Position(position.getRow(), position.getColumn() + 1);
		assertFalse(position.equals(differentCol),
				"Error: Positions with different columns should not be equal.");
	}

	/**
	 * CC: 5. Path 5: Coordinates match (this.row == other.getRow() && this.column == other.getColumn() is true)
	 * Verifies logical equality for different objects with the same data.
	 */
	@Test
	void equals5() {
		Position identical = new Position(position.getRow(), position.getColumn());
		assertTrue(position.equals(identical),
				"Error: Different objects with the same coordinates should be equal.");
	}
	@Test
	public void testHashCode() {
		Position p2 = new Position(1, 2);
		assertEquals(position.hashCode(), p2.hashCode(), "Error: equal positions should have same hash code");
	}

	@Test
	public void testToString() {
		assertEquals("B3", position.toString(), "Error: expected string 'B3' for position (1, 2)");
	}
}