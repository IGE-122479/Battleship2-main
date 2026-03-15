package battleship;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameGui {
    private static GameGui instance;
    private Stage stage;
    private IGame game;
    private BoardView myBoard;
    private BoardView radarBoard;

    public static void show(IGame game) {
        if (instance == null) {
            instance = new GameGui(game);
        } else {
            instance.game = game;
            instance.refresh();
        }
    }

    public static void update() {
        if (instance != null) {
            Platform.runLater(() -> instance.refresh());
        }
    }

    private GameGui(IGame game) {
        this.game = game;
        this.stage = new Stage();
        initContent();
    }

    private void initContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        HBox boardsLayout = new HBox(40);
        boardsLayout.setAlignment(Pos.CENTER);

        // Criar as views
        myBoard = new BoardView(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard = new BoardView(null, game.getMyMoves(), true);

        VBox leftBox = createBoardContainer("A MINHA FROTA", myBoard);
        VBox rightBox = createBoardContainer("RADAR DE ATAQUE", radarBoard);

        boardsLayout.getChildren().addAll(leftBox, rightBox);

        // Adicionar Legenda
        HBox legend = createLegend();

        mainLayout.getChildren().addAll(boardsLayout, legend);

        Scene scene = new Scene(mainLayout);
        stage.setTitle("Batalha Naval - Monitor de Jogo");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createBoardContainer(String title, BoardView board) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Text t = new Text(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        box.getChildren().addAll(t, board);
        return box;
    }

    private HBox createLegend() {
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(
                createLegendItem(Color.GRAY, "Navio"),
                createLegendItem(Color.RED, "Tiro Certeiro (*)"),
                createLegendItem(Color.WHITE, "Água (o)"),
                createLegendItem(Color.LIGHTBLUE, "Desconhecido")
        );
        return hBox;
    }

    private HBox createLegendItem(Color color, String label) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Rectangle r = new Rectangle(15, 15, color);
        r.setStroke(Color.BLACK);
        item.getChildren().addAll(r, new Text(label));
        return item;
    }

    private void refresh() {
        myBoard.renderBoard(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard.renderBoard(null, game.getMyMoves(), true);
    }
}