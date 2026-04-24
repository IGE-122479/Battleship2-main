package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class MoveTimerTest {

    private MoveTimer timer;

    @BeforeEach
    void configurar() {
        timer = new MoveTimer();
    }

    @AfterEach
    void limpar() {
        timer = null;
    }

    /**
     * Test for the construtor.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[MoveTimer] construtor – cronómetro criado e não está a correr")
    void moveTimerConstrutor() {
        MoveTimer novoTimer = new MoveTimer();
        assertFalse(novoTimer.isRunning(),
                "Erro: o cronómetro deve estar parado imediatamente após a criação.");
    }

    /**
     * Test for the start method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[MoveTimer] start – cronómetro fica a correr após start()")
    void testStart() {
        timer.start();
        assertTrue(timer.isRunning(),
                "Erro: isRunning() deve ser verdadeiro após chamar start().");
    }

    /**
     * Test for the stop method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("[MoveTimer] stop1 – para cronómetro iniciado e devolve valor >= 0")
    void testStop1() throws InterruptedException {
        timer.start();
        Thread.sleep(10);
        long resultado = timer.stop();
        assertAll(
                () -> assertFalse(timer.isRunning(),
                        "Erro: o cronómetro deve estar parado após stop()."),
                () -> assertTrue(resultado >= 0,
                        "Erro: o tempo devolvido deve ser não-negativo.")
        );
    }

    @Test
    @DisplayName("[MoveTimer] stop2 – stop() sem start() prévio devolve 0 e não lança excepção")
    void testStop2() {
        assertDoesNotThrow(() -> {
            long resultado = timer.stop();
            assertEquals(0L, resultado,
                    "Erro: sem start(), stop() deve devolver 0 ms.");
        }, "Erro: stop() sem start() não deve lançar excepção.");
    }

    /**
     * Test for the elapsed method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[MoveTimer] elapsed – devolve tempo decorrido enquanto a correr")
    void testElapsed() throws InterruptedException {
        timer.start();
        Thread.sleep(20);
        long decorrido = timer.elapsed();
        assertTrue(decorrido >= 0,
                "Erro: elapsed() deve devolver um valor não-negativo.");
    }

    /**
     * Test for the isRunning method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("[MoveTimer] isRunning – falso antes de start, verdadeiro depois")
    void testIsRunning() {
        assertFalse(timer.isRunning(),
                "Erro: isRunning() deve ser falso antes de chamar start().");
        timer.start();
        assertTrue(timer.isRunning(),
                "Erro: isRunning() deve ser verdadeiro após start().");
    }

    /**
     * Test for the format method.
     * Cyclomatic Complexity: 3
     */
    @Test
    @DisplayName("[MoveTimer] format1 – valor com minutos (ex.: '1m 5s 500ms')")
    void testFormat1() {
        // 1 minuto + 5 segundos + 500 ms = 65 500 ms
        String resultado = MoveTimer.format(65_500L);
        assertTrue(resultado.contains("m") && resultado.contains("s"),
                "Erro: com minutos, o resultado deve conter 'm' e 's'. Obtido: " + resultado);
    }

    @Test
    @DisplayName("[MoveTimer] format2 – valor com segundos mas sem minutos (ex.: '3s 200ms')")
    void testFormat2() {
        // 3 segundos + 200 ms = 3 200 ms
        String resultado = MoveTimer.format(3_200L);
        assertAll(
                () -> assertFalse(resultado.contains("m "),
                        "Erro: sem minutos, o resultado não deve conter 'm '. Obtido: " + resultado),
                () -> assertTrue(resultado.contains("s"),
                        "Erro: com segundos, o resultado deve conter 's'. Obtido: " + resultado)
        );
    }

    @Test
    @DisplayName("[MoveTimer] format3 – apenas milissegundos (ex.: '750ms')")
    void testFormat3() {
        String resultado = MoveTimer.format(750L);
        assertAll(
                () -> assertTrue(resultado.endsWith("ms"),
                        "Erro: apenas ms, o resultado deve terminar em 'ms'. Obtido: " + resultado),
                () -> assertFalse(resultado.contains("s "),
                        "Erro: sem segundos completos, não deve conter 's '. Obtido: " + resultado)
        );
    }
}