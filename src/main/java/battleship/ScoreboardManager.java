package battleship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game scoreboard by saving and loading {@link GameRecord} objects
 * to/from a JSON file, and displaying the scoreboard in the console.
 *
 * <p>Usage example at the end of a game:
 * <pre>
 *     ScoreboardManager.saveRecord(new GameRecord(game, true));
 *     ScoreboardManager.printScoreboard();
 * </pre>
 */
public class ScoreboardManager {

    /** Path to the JSON file where game records are persisted. */
    private static final String SCOREBOARD_FILE = "scoreboard.json";

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Saves a new {@link GameRecord} to the scoreboard JSON file.
     * If the file does not exist, it is created automatically.
     *
     * @param record the game record to save; must not be null
     */
    public static void saveRecord(GameRecord record) {
        assert record != null;

        List<GameRecord> records = loadRecords();
        records.add(record);

        try {
            mapper.writeValue(new File(SCOREBOARD_FILE), records);
            System.out.println("[Scoreboard] Jogo guardado com sucesso.");
        } catch (IOException e) {
            System.err.println("[Scoreboard] Erro ao guardar o jogo: " + e.getMessage());
        }
    }

    /**
     * Loads all game records from the JSON file.
     * Returns an empty list if the file does not exist or cannot be read.
     *
     * @return a list of {@link GameRecord} objects, never null
     */
    public static List<GameRecord> loadRecords() {
        File file = new File(SCOREBOARD_FILE);
        if (!file.exists()) return new ArrayList<>();

        try {
            return mapper.readValue(file, new TypeReference<List<GameRecord>>() {});
        } catch (IOException e) {
            System.err.println("[Scoreboard] Erro ao ler o scoreboard: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Prints the full scoreboard to the console, showing all past games
     * ordered from oldest to most recent (order of insertion).
     * If no games have been recorded yet, a message is shown instead.
     */
    public static void printScoreboard() {
        List<GameRecord> records = loadRecords();

        System.out.println();
        System.out.println("+=====================================================================+");
        System.out.println("|                     SCOREBOARD - JOGOS PASSADOS                    |");
        System.out.println("+=====================================================================+");

        if (records.isEmpty()) {
            System.out.println("|              Ainda não existem jogos registados.                   |");
            System.out.println("+=====================================================================+");
            return;
        }

        System.out.println("| Data/Hora            | Jogadas | Acertos    | Navios Afund.| Resultado|");
        System.out.println("+---------------------+---------+------------+--------------+----------+");

        for (GameRecord r : records)
            System.out.println(r);

        System.out.println("+---------------------+---------+------------+--------------+----------+");

        long wins   = records.stream().filter(r -> r.getResult().equals("VITÓRIA")).count();
        long losses = records.stream().filter(r -> r.getResult().equals("DERROTA")).count();
        System.out.printf("|  Total de jogos: %-3d    Vitórias: %-3d    Derrotas: %-3d              |%n",
                records.size(), wins, losses);
        System.out.println("+=====================================================================+");
        System.out.println();
    }
}
