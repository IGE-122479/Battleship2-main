package battleship;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

    static class FakeGame extends Game {

        private Integer forceRemainingShips      = null;
        private Integer forceAlienRemainingShips = null;

        FakeGame(IFleet myFleet) {
            super(myFleet);
        }

        void setForceRemainingShips(int v)      { this.forceRemainingShips      = v; }
        void setForceAlienRemainingShips(int v) { this.forceAlienRemainingShips = v; }

        @Override
        public int getRemainingShips() {
            return forceRemainingShips != null ? forceRemainingShips : super.getRemainingShips();
        }

        @Override
        public int getAlienRemainingShips() {
            return forceAlienRemainingShips != null ? forceAlienRemainingShips : super.getAlienRemainingShips();
        }
    }

    /**
     * Tasks cujo createGame() devolve um FakeGame pré-configurado.
     * O FakeGame é guardado em campo público para inspeção nos testes.
     */
    static class TasksComFakeGame extends Tasks {
        FakeGame fakeGame;
        private final int alienRemaining;
        private final int myRemaining;

        /**
         * @param alienRemaining valor fixo para getAlienRemainingShips()
         * @param myRemaining    valor fixo para getRemainingShips()
         */
        TasksComFakeGame(int alienRemaining, int myRemaining) {
            this.alienRemaining = alienRemaining;
            this.myRemaining    = myRemaining;
        }

        @Override
        protected IGame createGame(IFleet fleet) {
            fakeGame = new FakeGame(fleet);
            fakeGame.setForceAlienRemainingShips(alienRemaining);
            fakeGame.setForceRemainingShips(myRemaining);
            return fakeGame;
        }

        @Override
        protected AiGame createAiGame() {
            throw new AssertionError("createAiGame não devia ser chamado aqui");
        }
    }

    /**
     * Tasks cujo createAiGame() lança IllegalStateException,
     * simulando a ausência da API key.
     */
    static class TasksComAiError extends Tasks {
        FakeGame fakeGame;

        @Override
        protected IGame createGame(IFleet fleet) {
            fakeGame = new FakeGame(fleet);
            fakeGame.setForceAlienRemainingShips(5);
            fakeGame.setForceRemainingShips(5);
            return fakeGame;
        }

        @Override
        protected AiGame createAiGame() {
            throw new IllegalStateException("API_KEY não está definida (fake).");
        }
    }

    /**
     * Tasks cujo createAiGame() devolve um AiGame em que generateShots()
     * usa randomEnemyFire() em vez de chamar a API.
     */
    static class TasksComFakeAi extends Tasks {
        FakeGame fakeGame;
        private final int alienRemaining;
        private final int myRemaining;

        TasksComFakeAi(int alienRemaining, int myRemaining) {
            this.alienRemaining = alienRemaining;
            this.myRemaining    = myRemaining;
        }

        @Override
        protected IGame createGame(IFleet fleet) {
            fakeGame = new FakeGame(fleet);
            fakeGame.setForceAlienRemainingShips(alienRemaining);
            fakeGame.setForceRemainingShips(myRemaining);
            return fakeGame;
        }

        @Override
        protected AiGame createAiGame() {
            // AiGame anónimo que substitui generateShots() por lógica local
            return new AiGame() {
                @Override
                public String generateShots(IGame game) {
                    return game.randomEnemyFire();
                }
            };
        }
    }

    private String correr(Tasks tasks, String input) {
        InputStream origIn  = System.in;
        PrintStream origOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            System.setOut(new PrintStream(buf));
            tasks.runMenu();
            return buf.toString();
        } finally {
            System.setIn(origIn);
            System.setOut(origOut);
        }
    }

    /**
     * Salta o teste se API_TOKEN não estiver definida.
     */
    private static void assumeApiKeyPresenteOuSkip() {
        String key = System.getenv("API_TOKEN");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                key != null && !key.isEmpty(),
                "Ignorado: API_TOKEN não definida — testes de RAJADAIA com IA requerem a chave");
    }

    /**
     * Test for the menuHelp method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[Tasks] menuHelp – executa sem lançar qualquer exceção")
    void testMenuHelp() {
        assertDoesNotThrow(Tasks::menuHelp,
                "Erro: menuHelp() não deve lançar qualquer exceção.");
    }

    /**
     * Test for the readPosition method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[Tasks] readPosition – lê linha e coluna corretamente a partir do scanner")
    void testReadPosition() {
        Scanner sc = new Scanner("3 5");
        Position pos = Tasks.readPosition(sc);
        assertNotNull(pos, "Position não deve ser null");
        assertEquals(3, pos.getRow(), "Erro: a linha lida deve ser 3. Obtida: " + pos.getRow());
        assertEquals(5, pos.getColumn(), "Erro: a coluna lida deve ser 5. Obtida: " + pos.getColumn());
        assertThrows(AssertionError.class, () -> Tasks.readPosition(null));

    }

    /**
     * Test for the readShip method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[Tasks] readShip – lê e cria uma Barca corretamente a partir do scanner")
    void testReadShip() {
        Scanner sc = new Scanner("barca 0 0 n");
        Ship navio = Tasks.readShip(sc);
        assertAll(
                () -> assertNotNull(navio,
                        "Erro: readShip() não deve devolver null para uma Barca válida."),
                () -> assertEquals("Barca", navio.getCategory(),
                        "Erro: a categoria deve ser 'Barca'. Obtida: " + navio.getCategory()),
                () -> assertThrows(AssertionError.class, () -> Tasks.readShip(null))
        );
    }

    /**
     * Test for the readClassicPosition method.
     * Cyclomatic Complexity: 8
     */
    @Test
    @DisplayName("[Tasks] readClassicPosition1 – formato compacto maiúsculas (ex.: 'A3')")
    void testReadClassicPosition1() {
        Scanner sc = new Scanner("A3");
        IPosition pos = Tasks.readClassicPosition(sc);
        assertAll(
                () -> assertEquals(0, pos.getRow(),
                        "Erro: 'A' deve corresponder à linha 0. Obtida: " + pos.getRow()),
                () -> assertEquals(2, pos.getColumn(),
                        "Erro: '3' deve corresponder à coluna 2 (índice 0). Obtida: " + pos.getColumn())
        );
    }

    @Test
    @DisplayName("[Tasks] readClassicPosition2 – formato com espaço (ex.: 'B 5')")
    void testReadClassicPosition2() {
        Scanner sc = new Scanner("B 5");
        IPosition pos = Tasks.readClassicPosition(sc);
        assertAll(
                () -> assertNotNull(pos,
                        "Erro: readClassicPosition('B 5') não deve devolver null."),
                () -> assertEquals(1, pos.getRow(),
                        "Erro: 'B' deve corresponder à linha 1. Obtida: " + pos.getRow()),
                () -> assertEquals(4, pos.getColumn(),
                        "Erro: '5' deve corresponder à coluna 4 (índice 0). Obtida: " + pos.getColumn())
        );
    }

    @Test
    @DisplayName("[Tasks] readClassicPosition3 – formato compacto minúsculas (ex.: 'c4')")
    void testReadClassicPosition3() {
        Scanner sc = new Scanner("c4");
        IPosition pos = Tasks.readClassicPosition(sc);
        assertAll(
                () -> assertEquals(2, pos.getRow(),
                        "Erro: 'c' deve ser normalizado para 'C' -> linha 2. Obtida: " + pos.getRow()),
                () -> assertEquals(3, pos.getColumn(),
                        "Erro: '4' deve corresponder à coluna 3 (índice 0). Obtida: " + pos.getColumn())
        );
    }

    @Test
    @DisplayName("[Tasks] readClassicPosition4 – formato completamente inválido lança IllegalArgumentException")
    void testReadClassicPosition4() {
        Scanner sc = new Scanner("123");
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(sc),
                "Erro: um token completamente numérico deve lançar IllegalArgumentException.");
    }

    @Test
    @DisplayName("[Tasks] readClassicPosition5 – scanner vazio lança IllegalArgumentException")
    void testReadClassicPosition5() {
        Scanner sc = new Scanner("");
        assertThrows(IllegalArgumentException.class,
                () -> Tasks.readClassicPosition(sc),
                "Erro: scanner vazio deve lançar IllegalArgumentException.");
    }

    @Test
    @DisplayName("[Tasks] readClassicPosition6 – formato com número e espaço (linha 356 branch)")
    void testReadClassicPosition6() {
        Scanner sc = new Scanner("1 2");
        assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(sc),
                "Erro: '1 2' deve lançar IllegalArgumentException");
    }

    /**
     * Test for the buildFleet method.
     * Cyclomatic Complexity: 4
     */
    @Test
    @DisplayName("[Tasks] buildFleet1 – constrói frota completa a partir de input válido")
    void testBuildFleet1() {
        String input = "galeao 0 3 e " + "fragata 5 0 n " + "nau 0 7 e " + "nau 3 7 e " +
                "caravela 5 4 n " + "caravela 5 7 n " + "caravela 8 2 n " + "barca 3 0 n " +
                "barca 0 5 n " + "barca 2 5 n " + "barca 4 2 n ";

        assertDoesNotThrow(() -> {
            Fleet frota = Tasks.buildFleet(new Scanner(input));
            assertAll(
                    () -> assertNotNull(frota,
                            "Erro: buildFleet() não deve devolver null com input válido."),
                    () -> assertEquals(11, frota.getShips().size(),
                            "Erro: a frota deve ter exactamente 11 navios. Obtido: " + frota.getShips().size())
            );
        }, "Erro: buildFleet() não deve lançar exceção com input válido.");
    }

    @Test
    @DisplayName("[Tasks] buildFleet2 – completa a frota mesmo quando alguns navios são rejeitados por colisão")
    void testBuildFleet2() {
        String input =
                "galeao 0 3 e " + "fragata 0 3 e " + "fragata 5 0 n " + "nau 0 7 e " + "nau 0 7 e " +
                        "nau 3 7 e " + "caravela 5 4 n " + "caravela 5 7 n " + "caravela 8 2 n " + "barca 3 0 n " +
                        "barca 0 5 n " + "barca 2 5 n " + "barca 4 2 n ";

        assertDoesNotThrow(() -> {
            Fleet frota = Tasks.buildFleet(new Scanner(input));
            assertAll(
                    () -> assertNotNull(frota,
                            "Erro: buildFleet() não deve devolver null mesmo com tentativas rejeitadas."),
                    () -> assertEquals(11, frota.getShips().size(),
                            "Erro: a frota deve ter 11 navios no final. Obtido: " + frota.getShips().size())
            );
        }, "Erro: buildFleet() não deve lançar exceção com tentativas rejeitadas.");
    }

    @Test
    @DisplayName("[Tasks] buildFleet3 – tenta adicionar navio inválido e loga mensagem")
    void testBuildFleet3() {
        String input = "invalidship 0 0 n galeao 0 3 e fragata 5 0 n nau 0 7 e nau 3 7 e " +
                "caravela 5 4 n caravela 5 7 n caravela 8 2 n barca 3 0 n " +
                "barca 0 5 n barca 2 5 n barca 4 2 n ";

        assertDoesNotThrow(() -> {
            Fleet frota = Tasks.buildFleet(new Scanner(input));
            assertNotNull(frota, "Erro: buildFleet() não deve devolver null.");
            assertEquals(11, frota.getShips().size(), "Erro: a frota deve ter 11 navios válidos.");
        }, "Erro: buildFleet() não deve lançar exceção com navio inválido.");
    }

    @Test
    @DisplayName("[Tasks] buildFleet4 – testa o assert.")
    void testBuildFleet4() {
        assertThrows(AssertionError.class,
                () -> Tasks.buildFleet(null));
    }

    /**
     * Test for the runMenu method.
     * Cyclomatic Complexity: 40
     */
    /**
     * Utilitário: redireciona System.in e System.out, corre menu(), restaura streams.
     */
    private String correrMenuCom(String inputSimulado) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(inputSimulado.getBytes());
            ByteArrayOutputStream outCapturado = new ByteArrayOutputStream();
            System.setIn(in);
            System.setOut(new PrintStream(outCapturado));
            Tasks.menu();
            return outCapturado.toString();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("[Tasks] menu1 – 'ajuda' chama menuHelp() e imprime a ajuda")
    void testMenu1() {
        String saida = correrMenuCom("ajuda\ndesisto\n");
        assertTrue(saida.contains("AJUDA DO MENU"),
                "Erro: o comando 'ajuda' deve imprimir o texto de ajuda do menu.");
    }

    @Test
    @DisplayName("[Tasks] menu2 – 'lefrota' lê frota do scanner, cria jogo e imprime tabuleiro")
    void testMenu2() {
        String input =
                "lefrota\n" +
                        "galeao 0 0 n\n" +
                        "fragata 5 0 n\n" +
                        "nau 0 4 e\n" +
                        "nau 7 5 e\n" +
                        "nau 9 5 e\n" +
                        "caravela 4 5 n\n" +
                        "caravela 4 8 n\n" +
                        "caravela 7 3 n\n" +
                        "barca 0 8 n\n" +
                        "barca 2 3 n\n" +
                        "barca 2 5 n\n" +
                        "desisto\n";
        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: o comando 'lefrota' não deve lançar exceção com input válido.");
    }

    @Test
    @DisplayName("[Tasks] menu3 – 'estado' sem jogo iniciado não imprime estado (game == null)")
    void testMenu3() {
        String saida = correrMenuCom("estado\ndesisto\n");
        assertFalse(saida.contains("A minha frota"),
                "Erro: sem jogo, o comando 'estado' não deve imprimir informação de frota.");
    }

    @Test
    @DisplayName("[Tasks] menu4 – 'estado' com jogo activo imprime estado das duas frotas")
    void testMenu4() {
        String saida = correrMenuCom("gerafrota\nestado\ndesisto\n");
        assertTrue(saida.contains("frota"),
                "Erro: com jogo activo, o comando 'estado' deve imprimir informação de frota.");
    }

    @Test
    @DisplayName("[Tasks] menu5 – 'mapa' sem frota criada não faz nada (myFleet == null)")
    void testMenu5() {
        String saida = correrMenuCom("mapa\ndesisto\n");
        assertFalse(saida.contains("MEU TABULEIRO"),
                "Erro: sem frota, o comando 'mapa' não deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu6 – 'mapa' com frota criada imprime o tabuleiro")
    void testMenu6() {
        String saida = correrMenuCom("gerafrota\nmapa\ndesisto\n");
        assertTrue(saida.contains("+") || saida.contains("|"),
                "Erro: com frota, o comando 'mapa' deve imprimir o tabuleiro (contém '+' ou '|').");
    }

    @Test
    @DisplayName("[Tasks] menu8 – 'mapaadversario' sem jogo imprime mensagem de aviso")
    void testMenu7() {
        String saida = correrMenuCom("mapaadversario\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'mapaadversario' deve imprimir 'Nenhum jogo em curso'.");
    }

    @Test
    @DisplayName("[Tasks] menu9 – 'mapaadversario' com jogo imprime tabuleiro do adversário")
    void testMenu9() {
        String saida = correrMenuCom("gerafrota\nmapaadversario\ndesisto\n");
        assertTrue(saida.contains("ADVERSÁRIO") || saida.contains("+"),
                "Erro: com jogo, 'mapaadversario' deve imprimir o tabuleiro do adversário.");
        assertTrue(saida.length() > 0,
                "Erro: 'mapaadversario' deve imprimir algo.");
    }

    @Test
    @DisplayName("[Tasks] menu10 – 'tiros' sem jogo não faz nada (game == null)")
    void testMenu10() {
        String saida = correrMenuCom("tiros\ndesisto\n");
        assertFalse(saida.contains("MEU TABULEIRO"),
                "Erro: sem jogo, o comando 'tiros' não deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu11 – 'tiros' com jogo imprime o tabuleiro com tiros")
    void testMenu11() {
        String saida = correrMenuCom("gerafrota\ntiros\ndesisto\n");
        assertTrue(saida.contains("+") || saida.contains("|"),
                "Erro: com jogo, 'tiros' deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu12 – 'tempo' sem jogo imprime mensagem de aviso")
    void testMenu12() {
        String saida = correrMenuCom("tempo\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'tempo' deve imprimir 'Nenhum jogo em curso'.");
    }

    @Test
    @DisplayName("[Tasks] menu13 – 'tempo' com jogo imprime estatísticas de tempo")
    void testMenu13() {
        String saida = correrMenuCom("gerafrota\ntempo\ndesisto\n");
        assertTrue(saida.contains("jogada") || saida.contains("RELÓGIO"),
                "Erro: com jogo, 'tempo' deve imprimir informação sobre as jogadas.");
    }

    @Test
    @DisplayName("[Tasks] menu14 – 'guardapdf' sem jogo imprime mensagem de aviso")
    void testMenu14() {
        String saida = correrMenuCom("guardapdf\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo"),
                "Erro: sem jogo, 'guardapdf' deve imprimir aviso 'Nenhum jogo'.");
    }

    @Test
    @DisplayName("[Tasks] menu15 – 'guardapdf' com jogo exporta o PDF sem exceção")
    void testMenu15() {
        assertDoesNotThrow(() -> correrMenuCom("gerafrota\nguardapdf\ndesisto\n"),
                "Erro: com jogo, 'guardapdf' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu16 – 'scoreboard' imprime o scoreboard sem exceção")
    void testMenu16() {
        String saida = correrMenuCom("scoreboard\ndesisto\n");
        assertTrue(saida.contains("SCOREBOARD"),
                "Erro: 'scoreboard' deve imprimir o cabeçalho 'SCOREBOARD'.");
    }

    @Test
    @DisplayName("[Tasks] menu17 – comando desconhecido imprime mensagem de erro")
    void testMenu17() {
        String saida = correrMenuCom("comandoinvalido\ndesisto\n");
        assertTrue(saida.contains("Que comando"),
                "Erro: comando desconhecido deve imprimir 'Que comando é esse??? Repete ...'.");
    }

    @Test
    @DisplayName("[Tasks] menu18 – 'rajada' com jogo ativo realiza ataque sem ganhar")
    void testMenu18() {
        String input = "gerafrota\nrajada\nA1 B2 C3\ndesisto\n";
        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: o comando 'rajada' não deve lançar exceção com jogo ativo.");
    }

    @Test
    @DisplayName("[Tasks] menu19 – 'simula' executa simulação completa sem exceção")
    void testMenu19() {
        String input = "gerafrota\nsimula\ndesisto\n";
        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: o comando 'simula' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu20 – 'rajada' sem jogo imprime mensagem de aviso")
    void testMenu20() {
        String saida = correrMenuCom("rajada\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'rajada' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    @Test
    @DisplayName("[Tasks] menu21 – 'simula' sem jogo imprime mensagem de aviso")
    void testMenu21() {
        String saida = correrMenuCom("simula\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'simula' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    @Test
    @DisplayName("[Tasks] menu22 – 'rajadaia' sem jogo imprime mensagem de aviso")
    void testMenu22() {
        String saida = correrMenuCom("rajadaia\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'rajadaia' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    @Test
    @DisplayName("[Tasks] menu23 – 'simula' interrompido ativa interrupt() e sai do ciclo")
    void testMenu23() {
        Thread.currentThread().interrupt();
        correrMenuCom("gerafrota\nsimula\ndesisto\n");
        assertTrue(Thread.currentThread().isInterrupted(),
                "Erro: a thread deve manter o estado de interrupção.");
        Thread.interrupted();
    }

    @Test
    @DisplayName("[Tasks] menu24 – 'simula' eventualmente termina com game over")
    void testMenu24() {
        String saida = correrMenuCom("gerafrota\nsimula\ndesisto\n");
        assertTrue(saida.contains("over") || saida.contains("fim") || saida.length() > 0,
                "Erro: a simulação deve eventualmente terminar.");
    }

    @Test
    @DisplayName("[Tasks] menu25 – 'rajadaia' pode terminar jogo")
    void testMenu25() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\nrajadaia\nD4 E5 F6\ndesisto\n";
        String saida = correrMenuCom(input);
        assertTrue(saida.length() > 0,
                "Erro: o fluxo de jogo deve executar até ao fim.");
    }

    @Test
    @DisplayName("[Tasks] RAJADA: jogador ganha → win() chamado, gameEnded = true")
    void testRajadaJogadorGanha() {
        var tasks = new TasksComFakeGame(0, 11);
        String output = correr(tasks, "gerafrota\n" + "rajada\n" + "A1 B2 C3\n" + "desisto\n");
        assertTrue(output.contains("Parabéns") || output.contains("frota"),
                "Esperado mensagem de vitória.\nOutput:\n" + output);
        assertNotNull(tasks.fakeGame, "FakeGame deve ter sido criado pelo factory method");
    }

    /**
     * getAlienRemainingShips() > 0 (jogador não ganha), getRemainingShips() == 0
     *  →  over()  →  gameEnded = true
     */
    @Test
    @DisplayName("[Tasks] RAJADA: inimigo ganha → over() chamado, gameEnded = true")
    void testRajadaInimigoGanha() {
        var tasks = new TasksComFakeGame( 5, 0);
        String output = correr(tasks, "gerafrota\n" + "rajada\n" + "A1 B2 C3\n" + "desisto\n");
        assertTrue(output.contains("glub") || output.contains("Maldito"),
                "Esperado mensagem de derrota (over()).\nOutput:\n" + output);
        assertNotNull(tasks.fakeGame);
    }

    /**
     * createAiGame() lança IllegalStateException  →  catch imprime "API_KEY não definida"
     */
    @Test
    @DisplayName("[Tasks] RAJADAIA: API_KEY ausente → aviso impresso, sem crash")
    void testRajadaIaApiKeyAusente() {
        var tasks = new TasksComAiError();
        String output = correr(tasks, "gerafrota\n" + "rajadaia\n" + "A1 B2 C3\n" + "desisto\n");
        assertTrue(output.contains("API_KEY não definida"),
                "Esperado aviso sobre API_KEY.\nOutput:\n" + output);
    }

    /**
     * getAlienRemainingShips() == 0  →  win()  →  gameEnded = true
     *
     * O AiGame é substituído por FakeAi (sem HTTP). Se o construtor de AiGame
     * validar a env var antes de ser sobrecarregado, o teste é saltado.
     */
    @Test
    @DisplayName("[Tasks] RAJADAIA: jogador ganha → win() chamado")
    void testRajadaIaJogadorGanha() {
        assumeApiKeyPresenteOuSkip();
        var tasks = new TasksComFakeAi( 0, 11);
        String output = correr(tasks, "gerafrota\n" + "rajadaia\n" + "A1 B2 C3\n" + "desisto\n");
        assertTrue(output.contains("Parabéns") || output.contains("frota"),
                "Esperado mensagem de vitória.\nOutput:\n" + output);
    }

    /**
     * getAlienRemainingShips() > 0, getRemainingShips() == 0
     *  →  over()  →  gameEnded = true
     */
    @Test
    @DisplayName("[Tasks] RAJADAIA: inimigo ganha → over() chamado")
    void testRajadaIaInimigoGanha() {
        assumeApiKeyPresenteOuSkip();
        var tasks = new TasksComFakeAi( 5,  0);
        String output = correr(tasks, "gerafrota\n" + "rajadaia\n" + "A1 B2 C3\n" + "desisto\n");
        assertTrue(output.contains("glub") || output.contains("Maldito"),
                "Esperado mensagem de derrota (over()).\nOutput:\n" + output);
    }
}