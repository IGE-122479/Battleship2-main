package battleship;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.List;

public class BoardView extends GridPane {
    private final int SIZE = 10;
    private final double CELL_SIZE = 35.0;

    public BoardView(IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        renderBoard(fleet, moves, isEnemyView);
    }

    public void renderBoard(IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        this.getChildren().clear();
        this.setAlignment(Pos.CENTER);

        // 1. Adicionar números no topo (1 a 10)
        for (int col = 0; col < SIZE; col++) {
            Text label = new Text(String.valueOf(col + 1));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            StackPane container = new StackPane(label);
            container.setPrefSize(CELL_SIZE, CELL_SIZE / 2);
            this.add(container, col + 1, 0); // Coluna + 1 porque a coluna 0 é das letras
        }

        // 2. Adicionar letras na esquerda (A a J) e as células do tabuleiro
        for (int row = 0; row < SIZE; row++) {
            // Letra da linha
            Text label = new Text(String.valueOf((char) ('A' + row)));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            StackPane container = new StackPane(label);
            container.setPrefSize(CELL_SIZE / 2, CELL_SIZE);
            this.add(container, 0, row + 1); // Linha + 1 porque a linha 0 é dos números

            for (int col = 0; col < SIZE; col++) {
                Position currentPos = new Position(row, col);
                this.add(createCell(currentPos, fleet, moves, isEnemyView), col + 1, row + 1);
            }
        }
    }

    private StackPane createCell(Position pos, IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        StackPane pane = new StackPane();
        Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
        rect.setStroke(Color.DARKBLUE);
        rect.setFill(Color.LIGHTBLUE);

        // Lógica de cores (Navios e Tiros)
        if (fleet != null && fleet.getShips() != null) {
            for (IShip ship : fleet.getShips()) {
                if (ship.occupies(pos)) {
                    if (!isEnemyView) rect.setFill(Color.GRAY);
                }
            }
        }

        if (moves != null) {
            for (IMove move : moves) {
                List<IPosition> shots = move.getShots();
                List<IGame.ShotResult> results = move.getShotResults();
                for (int i = 0; i < shots.size(); i++) {
                    if (shots.get(i).equals(pos)) {
                        if (results.get(i).ship() != null) rect.setFill(Color.RED);
                        else rect.setFill(Color.WHITE);
                    }
                }
            }
        }
        pane.getChildren().add(rect);
        return pane;
    }
}