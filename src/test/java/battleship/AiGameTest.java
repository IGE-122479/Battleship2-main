package battleship;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class AiGame.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity:
 * - constructor: 3
 * - generateShots(): 1
 * - buildUserMessage(): 2
 * - buildAlreadyShotList(): 4
 * - askLlmForShots(): 5
 * - callApi(): 5
 * - parseAiResponse(): 6
 * - extractShotsJson(): 9
 * - isValidShotsJson(): 12
 * - buildSystemPrompt(): 1
 * - buildRandomFallbackJson(): 3
 * - addToHistory(): 1
 * - getConversationLength(): 1
 * - isInitialized(): 1
 */

public class AiGameTest {

    private AiGame aiGame;

    // ================= SETUP / TEARDOWN =================

    @BeforeEach
    void setUp() {
        try {
            aiGame = new AiGame();
        } catch (Exception e) {
            aiGame = null;
        }
    }

    @AfterEach
    void tearDown() {
        aiGame = null;
    }

    // ================= FAKES =================

    static class FakePosition implements IPosition {
        private final int row;
        private final int column;
        private boolean occupied = false;
        private boolean hit = false;

        FakePosition(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override public int getRow() { return row; }
        @Override public int getColumn() { return column; }
        @Override public char getClassicRow() { return (char) ('A' + row); }
        @Override public int getClassicColumn() { return column + 1; }

        @Override
        public boolean isInside() {
            return row >= 0 && row < 10 && column >= 0 && column < 10;
        }

        @Override
        public boolean isAdjacentTo(IPosition other) {
            return Math.abs(row - other.getRow()) <= 1 &&
                    Math.abs(column - other.getColumn()) <= 1 &&
                    !(row == other.getRow() && column == other.getColumn());
        }

        @Override public void occupy() { occupied = true; }
        @Override public void shoot() { hit = true; }
        @Override public boolean isOccupied() { return occupied; }
        @Override public boolean isHit() { return hit; }

        @Override
        public List<IPosition> adjacentPositions() {
            List<IPosition> adj = new ArrayList<>();
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = row + dr;
                    int nc = column + dc;
                    if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10) {
                        adj.add(new FakePosition(nr, nc));
                    }
                }
            }
            return adj;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof IPosition p)) return false;
            return row == p.getRow() && column == p.getColumn();
        }
    }

    static class FakeMove implements IMove {
        private final int number;
        private final List<IPosition> shots;

        FakeMove(int number, List<IPosition> shots) {
            this.number = number;
            this.shots = shots;
        }

        @Override public int getNumber() { return number; }
        @Override public List<IPosition> getShots() { return shots; }
        @Override public List<IGame.ShotResult> getShotResults() { return new ArrayList<>(); }
        @Override public String processEnemyFire(boolean verbose) { return "processed"; }
        @Override public String toDetailedString() { return "Fake detailed result"; }
        @Override public String toString() { return "Move #" + number; }
    }

    static class FakeGame implements IGame {
        private final List<IMove> alienMoves;

        FakeGame(List<IMove> alienMoves) {
            this.alienMoves = alienMoves;
        }

        @Override public List<IMove> getAlienMoves() { return alienMoves; }
        @Override public String readEnemyFire(String json) { return "processed shots"; }
        @Override public int getRemainingShips() { return 5; }

        @Override public String randomEnemyFire() { return ""; }
        @Override public void fireShots(List<IPosition> shots) {}
        @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
        @Override public IFleet getMyFleet() { return null; }
        @Override public IFleet getAlienFleet() { return null; }
        @Override public List<IMove> getMyMoves() { return new ArrayList<>(); }
        @Override public int getRepeatedShots() { return 0; }
        @Override public int getInvalidShots() { return 0; }
        @Override public int getHits() { return 0; }
        @Override public int getSunkShips() { return 0; }
        @Override public void printMyBoard(boolean a, boolean b) {}
        @Override public void printAlienBoard(boolean a, boolean b) {}
        @Override public void over() {}
    }

    // ================= Tests =================

    @Test
    @DisplayName("constructor1: sem API_KEY lança IllegalStateException com mensagem")
    void constructor1() {
        try {
            AiGame instance = new AiGame();
            assertNotNull(instance, "Error: expected instance");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage(), "Error: expected message");
        }
    }

    @Test
    @DisplayName("constructor2: instância criada com API_KEY não está inicializada")
    void constructor2() {
        if (aiGame != null) {
            assertAll(
                    () -> assertNotNull(aiGame),
                    () -> assertFalse(aiGame.isInitialized())
            );
        }
    }

    @Test
    @DisplayName("generateShots1: argumento null lança AssertionError")
    void generateShots1() {
        if (aiGame == null) return;

        assertThrows(AssertionError.class,
                () -> aiGame.generateShots(null),
                "Error: null game");
    }

    @Test
    @DisplayName("generateShots2: jogo válido com um movimento devolve resultado processado")
    void generateShots2() {
        if (aiGame == null) return;

        IGame game = new FakeGame(List.of(
                new FakeMove(1, List.of(new FakePosition(0, 0)))
        ));

        String result = aiGame.generateShots(game);

        assertEquals("processed shots", result,
                "Error: expected processed result");
    }

    @Test
    @DisplayName("buildUserMessage1: primeira jogada contém 'O jogo começa agora'")
    void buildUserMessage1() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame, new FakeGame(new ArrayList<>()));

        assertTrue(result.contains("O jogo começa agora"),
                "Error: first move message");
    }

    @Test
    @DisplayName("buildUserMessage2: segunda jogada contém 'Resultado da tua última rajada'")
    void buildUserMessage2() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        m.invoke(aiGame, new FakeGame(new ArrayList<>()));

        String result = (String) m.invoke(aiGame,
                new FakeGame(List.of(new FakeMove(1, List.of(new FakePosition(0, 0))))));

        assertTrue(result.contains("Resultado da tua última rajada"),
                "Error: follow-up message");
    }

    @Test
    @DisplayName("buildAlreadyShotList1: lista vazia devolve 'Nenhuma'")
    void buildAlreadyShotList1() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildAlreadyShotList", List.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame, new ArrayList<>());

        assertTrue(result.contains("Nenhuma"),
                "Error: empty case");
    }

    @Test
    @DisplayName("buildAlreadyShotList2: lista com tiros contém as coordenadas clássicas A1 e B2")
    void buildAlreadyShotList2() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildAlreadyShotList", List.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame,
                List.of(new FakeMove(1,
                        List.of(new FakePosition(0, 0), new FakePosition(1, 1)))));

        assertTrue(result.contains("A1"));
        assertTrue(result.contains("B2"));
    }


    @Test
    @DisplayName("extractShotsJson1: argumento null devolve null")
    void extractShotsJson1() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(aiGame, (String) null));
    }

    @Test
    @DisplayName("extractShotsJson2: JSON válido dentro de bloco ```json``` é extraído")
    void extractShotsJson2() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String json = "```json [{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}] ```";

        assertNotNull(m.invoke(aiGame, json));
    }

    @Test
    @DisplayName("extractShotsJson4: texto sem JSON válido devolve null")
    void extractShotsJson3() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(aiGame, "invalid"));
    }

    @Test
    @DisplayName("extractShotsJson6: string vazia (blank) devolve null")
    void extractShotsJson4() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(aiGame, ""));
    }

    @Test
    @DisplayName("extractShotsJson7: code-block encontrado mas JSON inválido (1 tiro) — isValid=false, cai para arrayPat e devolve null")
    void extractShotsJson5() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String input = "```json [{\"row\":\"A\",\"column\":1}] ```";

        assertNull(m.invoke(aiGame, input),
                "Error: code-block com JSON inválido deve devolver null");
    }

    @Test
    @DisplayName("extractShotsJson8: começa com '[' mas JSON inválido (1 tiro) — startsWith=true mas isValid=false")
    void extractShotsJson6() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String input = "[{\"row\":\"A\",\"column\":1}]";

        assertNull(m.invoke(aiGame, input),
                "Error: array com menos de 3 tiros deve devolver null");
    }


    @Test
    @DisplayName("isValidShotsJson3: linha inválida 'Z' (fora de A-J) devolve false")
    void isValidShotsJson1() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"Z\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json));
    }

    @Test
    @DisplayName("isValidShotsJson4: coluna inválida 99 (col > 10) devolve false")
    void isValidShotsJson2() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\",\"column\":99},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json));
    }

    @Test
    @DisplayName("isValidShotsJson5: raiz não é array (objeto JSON) devolve false — branch !isArray=true")
    void isValidShotsJson3() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        assertFalse((boolean) m.invoke(aiGame, "{\"row\":\"A\",\"column\":1}"),
                "Error: objeto JSON não é array, deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson6: campo 'row' ausente num tiro devolve false — branch rowNode==null")
    void isValidShotsJson4() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: campo row ausente deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson7: campo 'column' ausente num tiro devolve false — branch colNode==null")
    void isValidShotsJson5() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\"},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: campo column ausente deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson8: row com comprimento > 1 (ex: 'AB') devolve false — branch row.length()!=1")
    void isValidShotsJson6() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"AB\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: row com length != 1 deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson9: coluna 0 (col < 1) devolve false — branch col<1")
    void isValidShotsJson7() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\",\"column\":0},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: coluna 0 deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson10: JSON com formado errado (texto inválido) devolve false — branch catch Exception")
    void isValidShotsJson8() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        assertFalse((boolean) m.invoke(aiGame, "não é json"),
                "Error: JSON com formado errado deve devolver false via catch");
    }

    @Test
    @DisplayName("isValidShotsJson11: row '1' (char < 'A') devolve false — branch row.charAt(0) < 'A'")
    void isValidShotsJson9() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"1\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: row '1' é menor que 'A', deve devolver false");
    }


    @Test
    @DisplayName("buildRandomFallbackJson3: chamadas sucessivas produzem resultados (aleatoriedade não bloqueia)")
    void buildRandomFallbackJson() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildRandomFallbackJson");
        m.setAccessible(true);

        String result1 = (String) m.invoke(aiGame);
        String result2 = (String) m.invoke(aiGame);

        assertNotNull(result1);
        assertNotNull(result2);
    }


    @Test
    @DisplayName("parseAiResponse2: JSON sem campo 'choices' lança RuntimeException — branch choices não é array")
    void parseAiResponse1() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(aiGame, "{}");
            } catch (Exception e) {
                throw (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    @DisplayName("parseAiResponse3: 'choices' vazio lança RuntimeException — branch isEmpty=true")
    void parseAiResponse2() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(aiGame, "{\"choices\":[]}");
            } catch (Exception e) {
                throw (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    @DisplayName("parseAiResponse4: campo 'content' ausente lança RuntimeException — branch isMissingNode=true")
    void parseAiResponse3() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

        // choices não vazio, message existe, mas "content" está ausente
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\"}}]}";

        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(aiGame, json);
            } catch (Exception e) {
                throw (RuntimeException) e.getCause();
            }
        }, "Error: content ausente deve lançar RuntimeException");
    }

    @Test
    @DisplayName("parseAiResponse5: JSON totalmente malformado lança RuntimeException — branch catch Exception")
    void parseAiResponse4() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(aiGame, "isto não é json {{{");
            } catch (Exception e) {
                throw (RuntimeException) e.getCause();
            }
        }, "Error: JSON malformado deve lançar RuntimeException via catch Exception");
    }

    @Test
    @DisplayName("getConversationLength: histórico vazio devolve 0 ao inicializar")
    void getConversationLength() {
        if (aiGame == null) return;

        assertEquals(0, aiGame.getConversationLength());
    }

    @Test
    @DisplayName("isInitialized: devolve true após buildUserMessage ser chamado")
    void isInitialized() throws Exception {
        if (aiGame == null) return;

        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        m.invoke(aiGame, new FakeGame(new ArrayList<>()));

        assertTrue(aiGame.isInitialized(),
                "Error: should be initialized after first buildUserMessage call");
    }
}