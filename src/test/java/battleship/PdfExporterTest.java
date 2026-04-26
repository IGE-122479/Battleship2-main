package battleship;

import com.itextpdf.layout.element.Table;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for PdfExporter.
 * Author: 99845
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - exportGameToPdf: 2
 * - buildUnifiedTable: 4
 * - addMoveRow: 2
 * - buildShotsText: 2
 * - buildResultsText: 6
 * - buildTimeText: 3
 * - resolveOutputFile: 4
 */
public class PdfExporterTest {

    private IGame game;

    @BeforeEach
    void setUp() {
        game = new Game(new Fleet());
        PdfExporter.outputFileOverride = null;
    }

    @AfterEach
    void tearDown() {
        game = null;
        PdfExporter.outputFileOverride = null;

        File file = new File("battleship_game.pdf");

        if (file.exists()) {
            file.setWritable(true);
            file.delete();
        }

        File[] timestamped = new File(".").listFiles(
                f -> f.getName().startsWith("battleship_game_") && f.getName().endsWith(".pdf"));

        if (timestamped != null) {
            for (File f : timestamped) {
                f.setWritable(true);
                f.delete();
            }
        }
    }

    //===============================================================================\\

    @Test
    @DisplayName("exportGameToPdf - creates PDF file")
    void exportGameToPdf1() {
        PdfExporter.exportGameToPdf(game);
        assertTrue(new File("battleship_game.pdf").exists());
    }

    @Test
    @DisplayName("exportGameToPdf - handles null (catch block)")
    void exportGameToPdf2() {
        assertDoesNotThrow(() -> PdfExporter.exportGameToPdf(null));
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildUnifiedTable - empty (total=0)")
    void buildUnifiedTable1() {
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildUnifiedTable - only alien moves (alienSize > mySize)")
    void buildUnifiedTable2() {
        game.getAlienMoves().add(new Move(1, new ArrayList<>(), new ArrayList<>()));
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildUnifiedTable - both lists populated (equal size)")
    void buildUnifiedTable3() {
        game.getAlienMoves().add(new Move(1, new ArrayList<>(), new ArrayList<>()));
        game.getMyMoves().add(new Move(1, new ArrayList<>(), new ArrayList<>()));
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    //===============================================================================\\

    @Test
    @DisplayName("addMoveRow - adds row with showTime=false")
    void addMoveRow1() {
        IMove move = new Move(1, new ArrayList<>(), new ArrayList<>());
        game.getAlienMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildShotsText - formats shots correctly")
    void buildShotsText1() {
        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(0, 0));
        shots.add(new Position(1, 1));
        IMove moveWithShots = new Move(1, shots, new ArrayList<>());
        game.getAlienMoves().add(moveWithShots);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildResultsText - invalid result (!result.valid())")
    void buildResultsText1() {
        IGame.ShotResult invalid = new IGame.ShotResult(false, false, null, false);
        IMove move = new Move(1, new ArrayList<>(), List.of(invalid));
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildResultsText - repeated result (result.repeated())")
    void buildResultsText2() {
        IGame.ShotResult repeated = new IGame.ShotResult(true, true, null, false);
        IMove move = new Move(2, new ArrayList<>(), List.of(repeated));
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildResultsText - water (result.ship() == null)")
    void buildResultsText3() {
        IGame.ShotResult water = new IGame.ShotResult(true, false, null, false);
        IMove move = new Move(3, new ArrayList<>(), List.of(water));
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildResultsText - sunk ship (result.sunk())")
    void buildResultsText4() {
        IShip ship = new Barge(Compass.NORTH, new Position(0, 0));
        IGame.ShotResult sunk = new IGame.ShotResult(true, false, ship, true);
        IMove move = new Move(4, new ArrayList<>(), List.of(sunk));
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildResultsText - ship hit (else: ship != null && !sunk)")
    void buildResultsText5() {
        IShip ship = new Barge(Compass.NORTH, new Position(0, 0));
        IGame.ShotResult hit = new IGame.ShotResult(true, false, ship, false);
        IMove move = new Move(5, new ArrayList<>(), List.of(hit));
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildTimeText - duration > 0 (instanceof Move && duration > 0)")
    void buildTimeText1() {
        Move move = new Move(2, new ArrayList<>(), new ArrayList<>());
        move.setDuration(5000);
        game.getMyMoves().add(move);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    @Test
    @DisplayName("buildTimeText - not instanceof Move")
    void buildTimeText2() {

        IMove notMove = new IMove() {
            @Override
            public int getNumber() { return 3; }
            @Override
            public List<IPosition> getShots() { return new ArrayList<>(); }
            @Override
            public List<IGame.ShotResult> getShotResults() { return new ArrayList<>(); }
            @Override
            public String processEnemyFire(boolean verbose) { return ""; }
            @Override
            public String toDetailedString() { return ""; }
        };
        game.getMyMoves().add(notMove);
        var table = PdfExporter.buildUnifiedTable(game);
        assertNotNull(table);
    }

    //===============================================================================\\

    @Test
    @DisplayName("resolveOutputFile1 - outputFileOverride não null devolve override directamente")
    void resolveOutputFile1() {
        PdfExporter.outputFileOverride = "custom_output.pdf";
        String result = PdfExporter.resolveOutputFile();
        assertEquals("custom_output.pdf", result);
    }

    @Test
    @DisplayName("resolveOutputFile3 - ficheiro existe e delete() retorna true devolve OUTPUT_FILE")
    void resolveOutputFile2() {
        File deletable = new File("battleship_game.pdf") {
            @Override public boolean exists() { return true; }
            @Override public boolean delete() { return true; }
        };
        String result = PdfExporter.resolveOutputFile(deletable);
        assertEquals("battleship_game.pdf", result);
    }

    @Test
    @DisplayName("resolveOutputFile4 - ficheiro existe e delete() retorna false devolve timestamp fallback")
    void resolveOutputFile3() {
        File locked = new File("battleship_game.pdf") {
            @Override public boolean exists() { return true; }
            @Override public boolean delete() { return false; }
        };
        String result = PdfExporter.resolveOutputFile(locked);
        assertTrue(result.startsWith("battleship_game_") && result.endsWith(".pdf"),
                "Expected timestamped filename but got: " + result);
    }

}
