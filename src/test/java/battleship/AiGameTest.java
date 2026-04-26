package battleship;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class AiGame.
 * Author: 99845
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity:
 * - constructor1: 3
 * - getApiToken(): 2
 * - constructor2: 3
 * - generateShots(): 1
 * - buildUserMessage(): 2
 * - buildAlreadyShotList(): 4
 * - askLlmForShots(): 4
 * - callApi(): 6
 * - executeHttpCall(): 2
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

    private static final String VALID_JSON =
            "[{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

    //===============================================================================\\

    @BeforeEach
    void setUp() {
        AiGame.apiResponseOverride = null;
        AiGame.envTokenOverride = null;
        AiGame.httpOverride = null;
        aiGame = new AiGame("fake-test-key");
    }

    @AfterEach
    void tearDown() {
        AiGame.apiResponseOverride = null;
        AiGame.envTokenOverride = null;
        AiGame.httpOverride = null;
        aiGame = null;
    }

    //===============================================================================\\

    @SuppressWarnings("unchecked")
    private static java.net.http.HttpResponse<String> fakeResponse(int statusCode, String body) {
        return new java.net.http.HttpResponse<>() {
            @Override public int statusCode() { return statusCode; }
            @Override public String body() { return body; }
            @Override public java.net.http.HttpRequest request() { return null; }
            @Override public java.util.Optional<java.net.http.HttpResponse<String>> previousResponse() { return java.util.Optional.empty(); }
            @Override public java.net.http.HttpHeaders headers() { return null; }
            @Override public java.net.http.HttpClient.Version version() { return java.net.http.HttpClient.Version.HTTP_1_1; }
            @Override public java.net.URI uri() { return null; }
            @Override public java.util.Optional<javax.net.ssl.SSLSession> sslSession() { return java.util.Optional.empty(); }
        };
    }

    //===============================================================================\\

    @Test
    @DisplayName("constructor1: sem API_KEY lança IllegalStateException com mensagem")
    void constructor1() {
        assertThrows(IllegalStateException.class, () -> new AiGame(""));
    }

    @Test
    @DisplayName("constructor2: envTokenOverride vazio lança IllegalStateException")
    void constructor2() {
        AiGame.envTokenOverride = "";
        assertThrows(IllegalStateException.class, AiGame::new);
    }

    @Test
    @DisplayName("constructor3: null lança IllegalStateException")
    void constructor3() {
        assertThrows(IllegalStateException.class, () -> new AiGame((String) null));
    }

    @Test
    @DisplayName("constructor4: envTokenOverride válido cria instância com sucesso")
    void constructor4() {
        AiGame.envTokenOverride = "valid-token";
        AiGame instance = new AiGame();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("constructor5: getApiToken devolve null — cobre if(token==null) ramo true no construtor público")
    void constructor5() {
        AiGame.envTokenOverride = null;
        assertThrows(IllegalStateException.class, () -> new AiGame() {
            @Override
            String getApiToken() { return null; }
        });
    }

    //===============================================================================\\

    @Test
    @DisplayName("getApiToken1: envTokenOverride null — cobre ternário ramo == null (System.getenv)")
    void getApiToken1() throws Exception {
        AiGame.envTokenOverride = null;
        Method m = AiGame.class.getDeclaredMethod("getApiToken");
        m.setAccessible(true);

        Object result = m.invoke(aiGame);

        assertTrue(result == null || result instanceof String);
    }

    //===============================================================================\\

    @Test
    @DisplayName("generateShots1: argumento null lança AssertionError")
    void generateShots1() {
        assertThrows(AssertionError.class, () -> aiGame.generateShots(null), "Error: null game");
    }

    @Test
    @DisplayName("generateShots2: jogo válido com um movimento devolve resultado processado")
    void generateShots2() {
        AiGame.apiResponseOverride = () -> VALID_JSON;

        IGame game = new Game(new Fleet());
        game.getAlienMoves().add(new Move(1, List.of(new Position(0, 0)), new ArrayList<>()));

        assertNotNull(aiGame.generateShots(game));
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildUserMessage1: primeira jogada contém 'O jogo começa agora'")
    void buildUserMessage1() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame, new Game(new Fleet()));

        assertTrue(result.contains("O jogo começa agora"), "Error: first move message");
    }

    @Test
    @DisplayName("buildUserMessage2: segunda jogada contém 'Resultado da tua última rajada'")
    void buildUserMessage2() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        IGame game = new Game(new Fleet());
        m.invoke(aiGame, game);

        game.getAlienMoves().add(new Move(1, List.of(new Position(0, 0)), new ArrayList<>()));
        String result = (String) m.invoke(aiGame, game);

        assertTrue(result.contains("Resultado da tua última rajada"), "Error: follow-up message");
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildAlreadyShotList: lista vazia devolve 'Nenhuma ainda'")
    void buildAlreadyShotList() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildAlreadyShotList", List.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame, new ArrayList<>());
        assertTrue(result.contains("Nenhuma ainda"), "Error: empty case");
    }

    //===============================================================================\\

    @Test
    @DisplayName("askLlmForShots: resposta inválida repetida activa fallback aleatório")
    void askLlmForShots() throws Exception {
        AiGame.apiResponseOverride = () -> "invalid response";

        Method m = AiGame.class.getDeclaredMethod("askLlmForShots", String.class);
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame, "test message");
        assertNotNull(result);
        assertTrue(result.startsWith("["));
    }

    //===============================================================================\\

    @Test
    @DisplayName("callApi1: httpOverride devolve status 200")
    void callApi1() throws Exception {
        AiGame.apiResponseOverride = null;
        String envelope = "{\"choices\":[{\"message\":{\"content\":\"" + VALID_JSON.replace("\"", "\\\"") + "\"}}]}";
        AiGame.httpOverride = req -> fakeResponse(200, envelope);

        Method m = AiGame.class.getDeclaredMethod("callApi");
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame);
        assertEquals(VALID_JSON, result);
    }

    @Test
    @DisplayName("callApi2: httpOverride lança InterruptedException")
    void callApi2() throws Exception {
        AiGame.apiResponseOverride = null;
        AiGame.httpOverride = req -> { throw new InterruptedException("interrupted"); };

        Method m = AiGame.class.getDeclaredMethod("callApi");
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try { m.invoke(aiGame); }
            catch (Exception e) { throw (RuntimeException) e.getCause(); }
        });

        Thread.interrupted();
    }

    @Test
    @DisplayName("callApi3: sem override e sem rede lança RuntimeException")
    void callApi3() throws Exception {
        AiGame.apiResponseOverride = null;
        AiGame.httpOverride = null;

        Method m = AiGame.class.getDeclaredMethod("callApi");
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try { m.invoke(aiGame); }
            catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
            }
        });
    }

    @Test
    @DisplayName("callApi4: httpOverride lança Exception genérica")
    void callApi4() throws Exception {
        AiGame.apiResponseOverride = null;
        AiGame.httpOverride = req -> { throw new Exception("generic error"); };

        Method m = AiGame.class.getDeclaredMethod("callApi");
        m.setAccessible(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(aiGame);
            } catch (Exception e) {
                throw (RuntimeException) e.getCause();
            }
        });

        assertTrue(ex.getMessage().contains("Erro na API"));
    }

    //===============================================================================\\

    @Test
    @DisplayName("extractShotsJson1: argumento null devolve null")
    void extractShotsJson1() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(aiGame, (String) null));
    }

    @Test
    @DisplayName("extractShotsJson2: string vazia (blank) devolve null")
    void extractShotsJson2() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        assertNull(m.invoke(aiGame, ""));
    }

    @Test
    @DisplayName("extractShotsJson3: bloco código com JSON inválido cai para arrayPat válido")
    void extractShotsJson3() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String invalid2 = "[{\"row\":\"A\",\"column\":1},{\"row\":\"B\",\"column\":2}]";
        String input = "```json\n" + invalid2 + "\n```\nDepois: " + VALID_JSON;
        assertEquals(VALID_JSON, m.invoke(aiGame, input));
    }

    @Test
    @DisplayName("extractShotsJson4: começa com '[' mas JSON inválido (1 tiro)")
    void extractShotsJson4() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String input = "[{\"row\":\"A\",\"column\":1}]";

        assertNull(m.invoke(aiGame, input), "Error: array com menos de 3 tiros deve devolver null");
    }

    @Test
    @DisplayName("extractShotsJson5: JSON dentro de bloco ``` sem 'json' é extraído")
    void extractShotsJson5() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("extractShotsJson", String.class);
        m.setAccessible(true);

        String input = "```\n" + VALID_JSON + "\n```";
        assertEquals(VALID_JSON, m.invoke(aiGame, input));
    }

    //===============================================================================\\

    @Test
    @DisplayName("isValidShotsJson1: linha inválida 'Z' (fora de A-J) devolve false")
    void isValidShotsJson1() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"Z\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json));
    }

    @Test
    @DisplayName("isValidShotsJson2: coluna inválida 99 (col > 10) devolve false")
    void isValidShotsJson2() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\",\"column\":99},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json));
    }

    @Test
    @DisplayName("isValidShotsJson3: raiz não é array (objeto JSON) devolve false")
    void isValidShotsJson3() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        assertFalse((boolean) m.invoke(aiGame, "{\"row\":\"A\",\"column\":1}"),
                "Error: objeto JSON não é array, deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson4: campo 'row' ausente num tiro devolve false")
    void isValidShotsJson4() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: campo row ausente deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson5: campo 'column' ausente num tiro devolve false")
    void isValidShotsJson5() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\"},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: campo column ausente deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson6: row com comprimento > 1 (ex: 'AB') devolve false")
    void isValidShotsJson6() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"AB\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: row com length != 1 deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson7: coluna 0 (col < 1) devolve false")
    void isValidShotsJson7() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"A\",\"column\":0},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: coluna 0 deve devolver false");
    }

    @Test
    @DisplayName("isValidShotsJson8: JSON com formado errado (texto inválido) devolve false")
    void isValidShotsJson8() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        assertFalse((boolean) m.invoke(aiGame, "não é json"),
                "Error: JSON com formado errado deve devolver false via catch");
    }

    @Test
    @DisplayName("isValidShotsJson9: row '1' (char < 'A') devolve false")
    void isValidShotsJson9() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("isValidShotsJson", String.class);
        m.setAccessible(true);

        String json = "[{\"row\":\"1\",\"column\":1},{\"row\":\"B\",\"column\":2},{\"row\":\"C\",\"column\":3}]";

        assertFalse((boolean) m.invoke(aiGame, json),
                "Error: row '1' é menor que 'A', deve devolver false");
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildSystemPrompt: devolve string não nula com conteúdo esperado")
    void buildSystemPrompt() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildSystemPrompt");
        m.setAccessible(true);

        String result = (String) m.invoke(aiGame);
        assertNotNull(result);
        assertTrue(result.contains("Batalha Naval"));
    }

    //===============================================================================\\

    @Test
    @DisplayName("buildRandomFallbackJson: chamadas sucessivas produzem resultados (aleatoriedade não bloqueia)")
    void buildRandomFallbackJson() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildRandomFallbackJson");
        m.setAccessible(true);

        String result1 = (String) m.invoke(aiGame);
        String result2 = (String) m.invoke(aiGame);

        assertNotNull(result1);
        assertNotNull(result2);
    }

    //===============================================================================\\

    @Test
    @DisplayName("parseAiResponse1: 'choices' vazio lança RuntimeException")
    void parseAiResponse1() throws Exception {
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
    @DisplayName("parseAiResponse2: campo 'content' ausente lança RuntimeException")
    void parseAiResponse2() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

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
    @DisplayName("parseAiResponse3: JSON totalmente malformado lança RuntimeException")
    void parseAiResponse3() throws Exception {
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
    @DisplayName("parseAiResponse4: choices não é array lança RuntimeException")
    void parseAiResponse4() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("parseAiResponse", String.class);
        m.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try { m.invoke(aiGame, "{\"choices\":\"not-array\"}"); }
            catch (Exception e) { throw (RuntimeException) e.getCause(); }
        });
    }

    //===============================================================================\\

    @Test
    @DisplayName("getConversationLength: histórico vazio devolve 0 ao inicializar")
    void getConversationLength() {
        assertEquals(0, aiGame.getConversationLength());
    }

    //===============================================================================\\

    @Test
    @DisplayName("isInitialized: devolve true após buildUserMessage ser chamado")
    void isInitialized() throws Exception {
        Method m = AiGame.class.getDeclaredMethod("buildUserMessage", IGame.class);
        m.setAccessible(true);

        m.invoke(aiGame, new Game(new Fleet()));

        assertTrue(aiGame.isInitialized(),
                "Error: should be initialized after first buildUserMessage call");
    }
}
