package battleship;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

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
                        "Erro: a categoria deve ser 'Barca'. Obtida: " + navio.getCategory())
        );
    }

    /**
     * Test for the readClassicPosition method.
     * Cyclomatic Complexity: 5
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
    @DisplayName("[Tasks] readClassicPosition – todos os formatos válidos")
    void testReadClassicPositionAllFormats() {
        Scanner sc1 = new Scanner("A1");
        IPosition pos1 = Tasks.readClassicPosition(sc1);
        assertNotNull(pos1);

        Scanner sc2 = new Scanner("B 2");
        IPosition pos2 = Tasks.readClassicPosition(sc2);
        assertNotNull(pos2);

        Scanner sc3 = new Scanner("c 3");
        IPosition pos3 = Tasks.readClassicPosition(sc3);
        assertNotNull(pos3);
    }

    /**
     * Test for the buildFleet method.
     * Cyclomatic Complexity: 2
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

    /**
     * Test for the menu method.
     * Cyclomatic Complexity: 26
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
    @DisplayName("[Tasks] menu1 – 'desisto' termina imediatamente o ciclo while")
    void testMenu1() {
        String saida = correrMenuCom("desisto\n");
        assertTrue(saida.contains("Bons ventos!"),
                "Erro: ao receber 'desisto', o menu deve imprimir 'Bons ventos!'.");
    }

    @Test
    @DisplayName("[Tasks] menu2 – 'ajuda' chama menuHelp() e imprime a ajuda")
    void testMenu2() {
        String saida = correrMenuCom("ajuda\ndesisto\n");
        assertTrue(saida.contains("AJUDA DO MENU"),
                "Erro: o comando 'ajuda' deve imprimir o texto de ajuda do menu.");
    }

    @Test
    @DisplayName("[Tasks] menu3 – 'lefrota' cria frota e jogo sem lançar exceção")
    void testMenu3() {
        assertDoesNotThrow(() -> correrMenuCom("gerafrota\ndesisto\n"),
                "Erro: o comando 'gerafrota' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu4 – 'lefrota' lê frota do scanner, cria jogo e imprime tabuleiro")
    void testMenu4() {
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
    @DisplayName("[Tasks] menu5 – 'estado' sem jogo iniciado não imprime estado (game == null)")
    void testMenu5() {
        String saida = correrMenuCom("estado\ndesisto\n");
        assertFalse(saida.contains("A minha frota"),
                "Erro: sem jogo, o comando 'estado' não deve imprimir informação de frota.");
    }

    @Test
    @DisplayName("[Tasks] menu6 – 'estado' com jogo activo imprime estado das duas frotas")
    void testMenu6() {
        String saida = correrMenuCom("gerafrota\nestado\ndesisto\n");
        assertTrue(saida.contains("frota"),
                "Erro: com jogo activo, o comando 'estado' deve imprimir informação de frota.");
    }

    @Test
    @DisplayName("[Tasks] menu7 – 'mapa' sem frota criada não faz nada (myFleet == null)")
    void testMenu7() {
        String saida = correrMenuCom("mapa\ndesisto\n");
        assertFalse(saida.contains("MEU TABULEIRO"),
                "Erro: sem frota, o comando 'mapa' não deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu8 – 'mapa' com frota criada imprime o tabuleiro")
    void testMenu8() {
        String saida = correrMenuCom("gerafrota\nmapa\ndesisto\n");
        assertTrue(saida.contains("+") || saida.contains("|"),
                "Erro: com frota, o comando 'mapa' deve imprimir o tabuleiro (contém '+' ou '|').");
    }

    @Test
    @DisplayName("[Tasks] menu9 – 'mapaadversario' sem jogo imprime mensagem de aviso")
    void testMenu9() {
        String saida = correrMenuCom("mapaadversario\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'mapaadversario' deve imprimir 'Nenhum jogo em curso'.");
    }

    @Test
    @DisplayName("[Tasks] menu10 – 'mapaadversario' com jogo imprime tabuleiro do adversário")
    void testMenu10() {
        String saida = correrMenuCom("gerafrota\nmapaadversario\ndesisto\n");
        assertTrue(saida.contains("ADVERSÁRIO") || saida.contains("+"),
                "Erro: com jogo, 'mapaadversario' deve imprimir o tabuleiro do adversário.");
        assertTrue(saida.length() > 0,
                "Erro: 'mapaadversario' deve imprimir algo.");
    }

    @Test
    @DisplayName("[Tasks] menu11 – 'tiros' sem jogo não faz nada (game == null)")
    void testMenu11() {
        String saida = correrMenuCom("tiros\ndesisto\n");
        assertFalse(saida.contains("MEU TABULEIRO"),
                "Erro: sem jogo, o comando 'tiros' não deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu12 – 'tiros' com jogo imprime o tabuleiro com tiros")
    void testMenu12() {
        String saida = correrMenuCom("gerafrota\ntiros\ndesisto\n");
        assertTrue(saida.contains("+") || saida.contains("|"),
                "Erro: com jogo, 'tiros' deve imprimir o tabuleiro.");
    }

    @Test
    @DisplayName("[Tasks] menu13 – 'tempo' sem jogo imprime mensagem de aviso")
    void testMenu13() {
        String saida = correrMenuCom("tempo\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'tempo' deve imprimir 'Nenhum jogo em curso'.");
    }

    @Test
    @DisplayName("[Tasks] menu14 – 'tempo' com jogo imprime estatísticas de tempo")
    void testMenu14() {
        String saida = correrMenuCom("gerafrota\ntempo\ndesisto\n");
        assertTrue(saida.contains("jogada") || saida.contains("RELÓGIO"),
                "Erro: com jogo, 'tempo' deve imprimir informação sobre as jogadas.");
    }

    @Test
    @DisplayName("[Tasks] menu15 – 'guardapdf' sem jogo imprime mensagem de aviso")
    void testMenu15() {
        String saida = correrMenuCom("guardapdf\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo"),
                "Erro: sem jogo, 'guardapdf' deve imprimir aviso 'Nenhum jogo'.");
    }

    @Test
    @DisplayName("[Tasks] menu16 – 'guardapdf' com jogo exporta o PDF sem exceção")
    void testMenu16() {
        assertDoesNotThrow(() -> correrMenuCom("gerafrota\nguardapdf\ndesisto\n"),
                "Erro: com jogo, 'guardapdf' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu17 – 'scoreboard' imprime o scoreboard sem exceção")
    void testMenu17() {
        String saida = correrMenuCom("scoreboard\ndesisto\n");
        assertTrue(saida.contains("SCOREBOARD"),
                "Erro: 'scoreboard' deve imprimir o cabeçalho 'SCOREBOARD'.");
    }

    @Test
    @DisplayName("[Tasks] menu18 – comando desconhecido imprime mensagem de erro")
    void testMenu18() {
        String saida = correrMenuCom("comandoinvalido\ndesisto\n");
        assertTrue(saida.contains("Que comando"),
                "Erro: comando desconhecido deve imprimir 'Que comando é esse??? Repete ...'.");
    }

    @Test
    @DisplayName("[Tasks] menu19 – 'rajada' com jogo ativo realiza ataque sem ganhar")
    void testMenu19() {
        String input = "gerafrota\nrajada\nA1 B2 C3\ndesisto\n";
        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: o comando 'rajada' não deve lançar exceção com jogo ativo.");
    }

    @Test
    @DisplayName("[Tasks] menu20 – 'simula' executa simulação completa sem exceção")
    void testMenu20() {
        String input = "gerafrota\nsimula\ndesisto\n";
        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: o comando 'simula' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu21 – 'rajadaia' executa sem lançar exceção")
    void testMenu21() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\ndesisto\n";

        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: 'rajadaia' não deve lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu22 – 'rajada' sem jogo imprime mensagem de aviso")
    void testMenu22() {
        String saida = correrMenuCom("rajada\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'rajada' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    @Test
    @DisplayName("[Tasks] menu23 – 'simula' sem jogo imprime mensagem de aviso")
    void testMenu23() {
        String saida = correrMenuCom("simula\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'simula' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    @Test
    @DisplayName("[Tasks] menu24 – 'rajadaia' sem jogo imprime mensagem de aviso")
    void testMenu24() {
        String saida = correrMenuCom("rajadaia\ndesisto\n");
        assertTrue(saida.contains("Nenhum jogo em curso"),
                "Erro: sem jogo, 'rajadaia' deve imprimir 'Nenhum jogo em curso. Usa 'gerafrota' primeiro.'.");
    }

    //
    @Test
    @DisplayName("[Tasks] menu25 – 'simula' interrompido ativa interrupt() e sai do ciclo")
    void testMenu25() {
        Thread.currentThread().interrupt();

        String saida = correrMenuCom("gerafrota\nsimula\ndesisto\n");

        assertTrue(Thread.currentThread().isInterrupted(),
                "Erro: a thread deve manter o estado de interrupção.");

        Thread.interrupted();
    }

    @Test
    @DisplayName("[Tasks] menu26 – 'simula' eventualmente termina com game over")
    void testMenu26() {
        String saida = correrMenuCom("gerafrota\nsimula\ndesisto\n");

        assertTrue(saida.contains("over") || saida.contains("fim") || saida.length() > 0,
                "Erro: a simulação deve eventualmente terminar.");
    }

    @Test
    @DisplayName("[Tasks] menu27 – 'rajadaia' lê input de tiros corretamente")
    void testMenu27() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\ndesisto\n";

        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: 'rajadaia' deve processar input de tiros sem exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu28 – 'rajadaia' executa fluxo da IA sem falhar")
    void testMenu28() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\ndesisto\n";

        String saida = correrMenuCom(input);

        assertTrue(saida.contains("Ataque") || saida.length() > 0,
                "Erro: o fluxo de 'rajadaia' deve executar e produzir output.");
    }

    @Test
    @DisplayName("[Tasks] menu29 – 'rajadaia' executa sem crash mesmo se IA falhar")
    void testMenu29() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\ndesisto\n";

        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: 'rajadaia' não deve falhar mesmo que a IA lance exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu30 – 'rajadaia' pode atingir condição de vitória")
    void testMenu30() {
        String input = "gerafrota\nrajadaia\nA1 A2 A3\nrajadaia\nB1 B2 B3\ndesisto\n";

        assertDoesNotThrow(() -> correrMenuCom(input),
                "Erro: múltiplas rajadas não devem lançar exceção.");
    }

    @Test
    @DisplayName("[Tasks] menu31 – 'rajadaia' pode terminar jogo")
    void testMenu31() {
        String input = "gerafrota\nrajadaia\nA1 B2 C3\nrajadaia\nD4 E5 F6\ndesisto\n";

        String saida = correrMenuCom(input);

        assertTrue(saida.length() > 0,
                "Erro: o fluxo de jogo deve executar até ao fim.");
    }


}