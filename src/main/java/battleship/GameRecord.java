package battleship;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single completed game record to be stored in the scoreboard.
 * Each record contains the date/time of the game, the number of moves played,
 * hits, sunk ships, and the final result.
 */
public class GameRecord {

    private String dateTime;
    private int totalMoves;
    private int totalHits;
    private int sunkShips;
    private String result; // "VITÓRIA" or "DERROTA"

    /** No-arg constructor required by Jackson for deserialization. */
    public GameRecord() {}

    /**
     * Creates a GameRecord from a finished game.
     *
     * @param game   the finished game instance
     * @param won    true if the player won, false otherwise
     */
    public GameRecord(IGame game, boolean won) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dateTime  = LocalDateTime.now().format(formatter);
        this.totalMoves = game.getAlienMoves().size();
        this.totalHits  = game.getHits();
        this.sunkShips  = game.getSunkShips();
        this.result     = won ? "VITÓRIA" : "DERROTA";
    }

    @JsonCreator
    public GameRecord(
            @JsonProperty("dateTime")   String dateTime,
            @JsonProperty("totalMoves") int totalMoves,
            @JsonProperty("totalHits")  int totalHits,
            @JsonProperty("sunkShips")  int sunkShips,
            @JsonProperty("result")     String result) {
        this.dateTime   = dateTime;
        this.totalMoves = totalMoves;
        this.totalHits  = totalHits;
        this.sunkShips  = sunkShips;
        this.result     = result;
    }

    // --- Getters (required by Jackson for serialization) ---

    public String getDateTime()   { return dateTime; }
    public int    getTotalMoves() { return totalMoves; }
    public int    getTotalHits()  { return totalHits; }
    public int    getSunkShips()  { return sunkShips; }
    public String getResult()     { return result; }

    @Override
    public String toString() {
        return String.format("| %-19s | %-8d | %-10d | %-12d | %-8s |",
                dateTime, totalMoves, totalHits, sunkShips, result);
    }
}
