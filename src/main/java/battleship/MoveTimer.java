package battleship;

import org.apache.commons.lang3.time.StopWatch;
import java.util.concurrent.TimeUnit;

/**
 * A classe MoveTimer mede o tempo decorrido em cada jogada (rajada) do jogo.
 * Utiliza o StopWatch da biblioteca Apache Commons Lang 3 para a cronometragem.
 *
 * Exemplo de utilização:
 *   MoveTimer timer = new MoveTimer();
 *   timer.start();
 *   // ... o jogador efectua os tiros ...
 *   long decorrido = timer.stop();
 *
 * Autor: IGE-99328
 */
public class MoveTimer {

    /** Instância do StopWatch da biblioteca Apache Commons Lang 3. */
    private final StopWatch stopWatch;

    /**
     * Cria um novo MoveTimer (ainda não iniciado).
     */
    public MoveTimer() {
        this.stopWatch = new StopWatch();
    }

    /**
     * Inicia (ou reinicia) o cronómetro.
     */
    public void start() {
        stopWatch.reset();
        stopWatch.start();
    }

    /**
     * Para o cronómetro e devolve o tempo decorrido em milissegundos.
     *
     * @return o tempo decorrido em milissegundos, ou 0 se nunca tiver sido iniciado
     */
    public long stop() {
        if (stopWatch.isStarted()) {
            stopWatch.stop();
        }
        return stopWatch.getTime(TimeUnit.MILLISECONDS);
    }

    /**
     * Devolve o tempo decorrido até ao momento em milissegundos (mesmo que ainda esteja a correr).
     *
     * @return o tempo decorrido em milissegundos
     */
    public long elapsed() {
        return stopWatch.getTime(TimeUnit.MILLISECONDS);
    }

    /**
     * Indica se o cronómetro está actualmente em funcionamento.
     *
     * @return true se estiver a correr
     */
    public boolean isRunning() {
        return stopWatch.isStarted();
    }

    /**
     * Formata um tempo em milissegundos numa string legível, por exemplo "2s 347ms".
     *
     * @param millis o tempo em milissegundos a formatar
     * @return string formatada
     */
    public static String format(long millis) {
        long minutes = millis / 60_000;
        long seconds = (millis % 60_000) / 1_000;
        long ms      = millis % 1_000;

        if (minutes > 0) {
            return String.format("%dm %ds %dms", minutes, seconds, ms);
        } else if (seconds > 0) {
            return String.format("%ds %dms", seconds, ms);
        } else {
            return String.format("%dms", ms);
        }
    }
}