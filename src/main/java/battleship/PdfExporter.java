package battleship;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PdfExporter is a utility class responsible for exporting Battleship game move history
 * to a PDF document. This class provides functionality to generate a formatted PDF report
 * containing detailed information about all moves, shots, and their results throughout
 * a game session.
 *
 * @author 99845
 * @version 2.0
 * @since 2026
 *
 */
public class PdfExporter {

    private static final String OUTPUT_FILE = "battleship_game.pdf";
    static String outputFileOverride = null;

    /**
     * Exports the full game history to a single unified PDF table.
     * Each row represents one move, showing who fired, the shots, results and duration.
     *
     * @param game the game to export, must not be null
     */
    public static void exportGameToPdf(IGame game) {
        String outputFile = resolveOutputFile();

        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Batalha Naval- Histórico de Jogadas").setBold().setFontSize(18));
            document.add(new Paragraph(" "));

            document.add(buildUnifiedTable(game));

            document.close();
            System.out.println("Game exported to " + outputFile);
        } catch (Exception e) {
            System.out.println("Erro ao exportar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds a single table interleaving alien moves and player moves.
     * Columns: Lado | Jogada | Tiros | Resultados | Duração
     *
     * @param game the game containing both move lists
     * @return the unified Table element
     */
    public static Table buildUnifiedTable(IGame game) {

        Table table = new Table(5);

        table.addHeaderCell(new Cell().add(new Paragraph("Lado").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Jogada").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Tiros").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Resultados").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Duração").setBold()));

        int alienSize = game.getAlienMoves().size();
        int mySize = game.getMyMoves().size();
        int total = Math.max(alienSize, mySize);

        for (int i = 0; i < total; i++){
            if(i < alienSize)
                addMoveRow(table, game.getAlienMoves().get(i), "Inimigo", false);
            if(i < mySize)
                addMoveRow(table, game.getMyMoves().get(i), "Jogador", true);
        }
        return table;
    }

    /**
     * Adds a single move row to the table.
     *
     * @param table    the table to add the row to
     * @param move     the move data
     * @param side     label identifying who fired ("Inimigo" or "Jogador")
     * @param showTime if true, fills the Tempo cell with the move duration
     */
    private static void addMoveRow(Table table, IMove move, String side, boolean showTime) {
        table.addCell(new Cell().add(new Paragraph(side)));
        table.addCell(new Cell().add(new Paragraph("Jogada nº" + move.getNumber())));
        table.addCell(new Cell().add(new Paragraph(buildShotsText(move))));
        table.addCell(new Cell().add(new Paragraph(buildResultsText(move))));
        table.addCell(new Cell().add(new Paragraph(showTime ? buildTimeText(move) : "-")));
    }

    /**
     * Builds the shots text for a move.
     *
     * @param move the move to extract shots from
     * @return formatted string of shot positions
     */
    private static String buildShotsText(IMove move) {
        StringBuilder sb = new StringBuilder();
        for(IPosition shot : move.getShots())
            sb.append(shot.toString()).append(", ");
        return sb.toString();
    }

    /**
     * Builds the results text for a move (e.g. "Água, Acerto em Nau, Repetido").
     *
     * @param move the move to extract results from
     * @return formatted string of shot results
     */
    private static String buildResultsText(IMove move) {
        StringBuilder sb = new StringBuilder();
        for(IGame.ShotResult result : move.getShotResults())
            formatShotResult(result, sb);

        return sb.toString();
    }

    /**
     * Formats a shot result and appends the corresponding textual representation
     * to the provided StringBuilder.
     * The method checks the properties of the shot result (validity, repetition, hit, sunk)
     * and appends a descriptive text for each case.
     * The formatted text is appended to the given StringBuilder.
     *
     * @param result the shot result to be formatted (must not be null)
     * @param sb the StringBuilder where the formatted text will be appended (must not be null)
     */

    private static void formatShotResult(IGame.ShotResult result, StringBuilder sb) {
        if(!result.valid())
            sb.append("Tiro inválido, ");
        else if(result.repeated())
            sb.append("Tiro repetido, ");
        else if(result.ship() == null)
            sb.append("Água, ");
        else if(result.sunk())
            sb.append("Acertou, ");
        else
            sb.append("Acertou em ").append(result.ship().getCategory()).append(", ");
    }

    /**
     * Returns the formatted duration of a move, or "-" if not available.
     *
     * @param move the move to extract duration from
     * @return formatted duration string or "-"
     */
    private static String buildTimeText(IMove move) {
        if(move instanceof Move m && m.getDuration() > 0)
            return MoveTimer.format(m.getDuration());
        return "-";
    }

    /**
     * Resolves the output file path.
     * If the default file is locked, returns a timestamped alternative.
     *
     * @return the resolved output file path
     */
    static String resolveOutputFile() {
        return resolveOutputFile(new File(OUTPUT_FILE));
    }

    static String resolveOutputFile(File file) {
        if (outputFileOverride != null) {
            return outputFileOverride;
        }

        if (file.exists()) {
            if (!file.delete()) {
                return generateTimeStamp();
            }
        }
        return OUTPUT_FILE;
    }

    /**
     * Generates a unique filename for the game results using the current timestamp.
     *
     * The filename follows the pattern:
     * "battleship_game_yyyyMMdd_HHmmss.pdf"
     *
     * This method is typically used when the default output file cannot be reused,
     * ensuring that a new file is created without overwriting existing ones.
     *
     * @return a non-null String representing a timestamped PDF filename
     */

    private static @NotNull String generateTimeStamp() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "battleship_game_" + timestamp + ".pdf";
    }

}
