package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GameRecordTest {
    private IGame jogo;
    private GameRecord gameRecord;


    @BeforeEach
    void configurar() {
        IFleet frota = Fleet.createRandom();
        jogo = new Game(frota);
        gameRecord = new GameRecord("2026-04-17 10:00:00", 5, 3, 2, "VITÓRIA");
    }

    @AfterEach
    void limpar() {
        jogo = null;
        gameRecord = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] construtor sem argumentos – todos os campos têm valores predefinidos")
    void construtorSemArgumentos() {
        GameRecord registo = new GameRecord();
        assertAll(
                () -> assertNull(registo.getDateTime(),
                        "Erro: dateTime deve ser null com o construtor vazio."),
                () -> assertEquals(0, registo.getTotalMoves(),
                        "Erro: totalMoves deve ser 0 com o construtor vazio."),
                () -> assertEquals(0, registo.getTotalHits(),
                        "Erro: totalHits deve ser 0 com o construtor vazio."),
                () -> assertEquals(0, registo.getSunkShips(),
                        "Erro: sunkShips deve ser 0 com o construtor vazio."),
                () -> assertNull(registo.getResult(),
                        "Erro: result deve ser null com o construtor vazio.")
        );
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 2
     */

    @Test
    @DisplayName("[GameRecord] construtor com jogo e vitória – resultado é 'VITÓRIA'")
    void construtorComJogoVitoria() {
        GameRecord registo = new GameRecord(jogo, true);
        assertAll(
                () -> assertEquals("VITÓRIA", registo.getResult(),
                        "Erro: quando won=true, o resultado deve ser 'VITÓRIA'."),
                () -> assertNotNull(registo.getDateTime(),
                        "Erro: dateTime não deve ser null após criação com jogo."),
                () -> assertFalse(registo.getDateTime().isBlank(),
                        "Erro: dateTime não deve ser uma string vazia."),
                () -> assertTrue(registo.getTotalMoves() >= 0,
                        "Erro: totalMoves deve ser >= 0.")
        );
    }

    @Test
    @DisplayName("[GameRecord] construtor com jogo e derrota – resultado é 'DERROTA'")
    void construtorComJogoDerrota() {
        GameRecord registo = new GameRecord(jogo, false);
        assertEquals("DERROTA", registo.getResult(),
                "Erro: quando won=false, o resultado deve ser 'DERROTA'.");
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */

    @Test
    @DisplayName("[GameRecord] construtor Jackson – todos os campos são definidos correctamente")
    void construtorJackson() {
        assertAll(
                () -> assertEquals("2026-04-17 10:00:00", gameRecord.getDateTime(),
                        "Erro: dateTime não coincide com o valor fornecido."),
                () -> assertEquals(5, gameRecord.getTotalMoves(),
                        "Erro: totalMoves não coincide com o valor fornecido."),
                () -> assertEquals(3, gameRecord.getTotalHits(),
                        "Erro: totalHits não coincide com o valor fornecido."),
                () -> assertEquals(2, gameRecord.getSunkShips(),
                        "Erro: sunkShips não coincide com o valor fornecido."),
                () -> assertEquals("VITÓRIA", gameRecord.getResult(),
                        "Erro: result não coincide com o valor fornecido.")
        );
    }

    /**
     * Test for the getDateTime method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] getDateTime – devolve exactamente a string fornecida")
    void testGetDateTime() {
        assertEquals("2026-04-17 10:00:00", gameRecord.getDateTime(),
                "Erro: getDateTime() deve devolver a string exacta '2026-04-17 10:00:00'.");
    }

    /**
     * Test for the getTotalMoves method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] getTotalMoves – devolve o número de jogadas correcto")
    void testGetTotalMoves() {
        assertEquals(5, gameRecord.getTotalMoves(),
                "Erro: getTotalMoves() deve devolver 5. Obtido: " + gameRecord.getTotalMoves());
    }

    /**
     * Test for the getTotalHits method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] getTotalHits – devolve o número de acertos correcto")
    void testGetTotalHits() {
        assertEquals(3, gameRecord.getTotalHits(),
                "Erro: getTotalHits() deve devolver 3. Obtido: " + gameRecord.getTotalHits());
    }

    /**
     * Test for the getSunkShips method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] getSunkShips – devolve o número de navios afundados correcto")
    void testGetSunkShips() {
        assertEquals(2, gameRecord.getSunkShips(),
                "Erro: getSunkShips() deve devolver 2. Obtido: " + gameRecord.getSunkShips());
    }

    /**
     * Test for the getResult method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] getResult – devolve o resultado correcto")
    void testGetResult() {
        assertEquals("VITÓRIA", gameRecord.getResult(),
                "Erro: getResult() deve devolver 'VITÓRIA'. Obtido: " + gameRecord.getResult());
    }

    /**
     * Test for the toStringContemCamposEssenciais method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[GameRecord] toString – contém data/hora, número de jogadas e resultado")
    void testToStringContemCamposEssenciais() {
        String texto = gameRecord.toString();
        assertAll(
                () -> assertTrue(texto.contains("2026-04-17 10:00:00"),
                        "Erro: toString() deve conter a data/hora '2026-04-17 10:00:00'."),
                () -> assertTrue(texto.contains("5"),
                        "Erro: toString() deve conter o número de jogadas '5'."),
                () -> assertTrue(texto.contains("VITÓRIA"),
                        "Erro: toString() deve conter o resultado 'VITÓRIA'.")
        );
    }


}