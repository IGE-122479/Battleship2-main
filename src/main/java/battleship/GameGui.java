package battleship;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador principal da Interface Gráfica (GUI) para o jogo Batalha Naval.
 * Esta classe utiliza o padrão Singleton para garantir que apenas uma janela de monitorização
 * esteja ativa, permitindo atualizações em tempo real a partir da consola ou do simulador.
 *
 * A interface exibe dois tabuleiros lado a lado: a frota do jogador e o radar de ataques,
 * acompanhados por uma legenda explicativa das cores e o estado atual dos navios.
 * No fim do jogo, é exibido o scoreboard com os resultados de todos os jogos anteriores.
 */
public class GameGui {

    /** Instância única da interface (Singleton). */
    private static GameGui instance;

    /** Janela principal do JavaFX. */
    private Stage stage;

    /** Referência para o estado atual do jogo. */
    private IGame game;

    /** Componente visual do tabuleiro do jogador. */
    private BoardView myBoard;

    /** Componente visual do radar de ataques ao adversário. */
    private BoardView radarBoard;

    /** Painel dinâmico da legenda de navios (estado da frota). */
    private VBox shipStatusBox;

    /** Layout principal reutilizável. */
    private VBox mainLayout;

    // -------------------------------------------------------------------------
    // Singleton API
    // -------------------------------------------------------------------------

    /**
     * Exibe a janela da GUI. Se a janela já existir, apenas atualiza os dados do jogo
     * e redesenha os componentes.
     *
     * @param game A instância do jogo {@link IGame} a ser visualizada.
     */
    public static void show(IGame game) {
        if (instance == null) {
            instance = new GameGui(game);
        } else {
            instance.game = game;
            instance.refresh();
        }
    }

    /**
     * Solicita a atualização dos tabuleiros na Thread da aplicação JavaFX.
     * Este método deve ser chamado sempre que houver uma alteração no estado do jogo
     * (ex: após um disparo no comando 'rajada' ou no 'simula').
     */
    public static void update() {
        if (instance != null) {
            Platform.runLater(() -> instance.refresh());
        }
    }

    /**
     * Exibe o painel de fim de jogo com o scoreboard completo.
     * Deve ser chamado quando o jogo terminar (vitória ou derrota).
     */
    public static void showScoreboard() {
        if (instance != null) {
            Platform.runLater(() -> instance.showScoreboardPanel());
        }
    }

    // -------------------------------------------------------------------------
    // Construção da janela
    // -------------------------------------------------------------------------

    /**
     * Construtor privado para inicializar a interface com um jogo específico.
     *
     * @param game Instância do jogo.
     */
    private GameGui(IGame game) {
        this.game = game;
        this.stage = new Stage();
        initContent();
    }

    /**
     * Configura a estrutura principal da janela, incluindo layouts, tabuleiros e legenda.
     */
    private void initContent() {
        mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #1a1a2e;");

        // --- Tabuleiros ---
        HBox boardsLayout = new HBox(40);
        boardsLayout.setAlignment(Pos.CENTER);

        myBoard    = new BoardView(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard = new BoardView(game.getAlienFleet(), game.getMyMoves(), true);

        VBox leftBox  = createBoardContainer("A MINHA FROTA", myBoard);
        VBox rightBox = createBoardContainer("RADAR DE ATAQUE", radarBoard);

        boardsLayout.getChildren().addAll(leftBox, rightBox);

        // --- Legenda de cores ---
        HBox colorLegend = createColorLegend();

        // --- Estado dos navios ---
        shipStatusBox = new VBox(6);
        shipStatusBox.setAlignment(Pos.CENTER);
        renderShipStatus();

        mainLayout.getChildren().addAll(boardsLayout, colorLegend, shipStatusBox);

        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

        Scene scene = new Scene(scroll, 900, 700);
        stage.setTitle("Batalha Naval - Monitor de Jogo");
        stage.setScene(scene);
        stage.show();
    }

    // -------------------------------------------------------------------------
    // Actualização
    // -------------------------------------------------------------------------

    /**
     * Redesenha os tabuleiros e o estado dos navios com os dados mais recentes do jogo.
     */
    private void refresh() {
        myBoard.renderBoard(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard.renderBoard(game.getAlienFleet(), game.getMyMoves(), true);
        renderShipStatus();
    }

    // -------------------------------------------------------------------------
    // Estado dos navios
    // -------------------------------------------------------------------------

    /**
     * Atualiza o painel de estado dos navios, mostrando para cada navio se ainda
     * está a flutuar ou foi afundado.
     */
    private void renderShipStatus() {
        shipStatusBox.getChildren().clear();

        Text title = new Text("ESTADO DA FROTA");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setFill(Color.WHITE);
        shipStatusBox.getChildren().add(title);

        IFleet fleet = game.getMyFleet();
        if (fleet == null || fleet.getShips() == null) return;

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);

        for (IShip ship : fleet.getShips()) {
            VBox shipItem = new VBox(3);
            shipItem.setAlignment(Pos.CENTER);

            // Indicador de cor: laranja = afundado, cinzento = intacto
            Rectangle indicator = new Rectangle(14, 14);
            indicator.setArcWidth(4);
            indicator.setArcHeight(4);
            if (!ship.stillFloating()) {
                indicator.setFill(Color.DARKORANGE);
                indicator.setStroke(Color.ORANGE);
            } else {
                indicator.setFill(Color.GRAY);
                indicator.setStroke(Color.LIGHTGRAY);
            }

            Text shipName = new Text(ship.getClass().getSimpleName());
            shipName.setFont(Font.font("Arial", 10));
            shipName.setFill(!ship.stillFloating() ? Color.DARKORANGE : Color.LIGHTGRAY);

            Text shipStatus = new Text(!ship.stillFloating() ? "✗" : "✓");
            shipStatus.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            shipStatus.setFill(!ship.stillFloating() ? Color.DARKORANGE : Color.LIGHTGREEN);

            shipItem.getChildren().addAll(indicator, shipName, shipStatus);
            row.getChildren().add(shipItem);
        }

        shipStatusBox.getChildren().add(row);

        // Resumo: X a flutuar / Y afundados
        long sunk      = fleet.getShips().stream().filter(s -> !s.stillFloating()).count();
        long floating  = fleet.getShips().size() - sunk;
        Text summary   = new Text(floating + " a flutuar  |  " + sunk + " afundados");
        summary.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        summary.setFill(Color.LIGHTCYAN);
        shipStatusBox.getChildren().add(summary);
    }

    // -------------------------------------------------------------------------
    // Scoreboard
    // -------------------------------------------------------------------------

    /**
     * Adiciona o painel de scoreboard ao layout principal, mostrando todos os jogos
     * registados até ao momento.
     */
    private void showScoreboardPanel() {
        // Remove scoreboard anterior se existir
        mainLayout.getChildren().removeIf(n -> "scoreboard".equals(n.getId()));

        VBox scorePanel = new VBox(10);
        scorePanel.setId("scoreboard");
        scorePanel.setAlignment(Pos.CENTER);
        scorePanel.setPadding(new Insets(16));
        scorePanel.setStyle(
            "-fx-background-color: #16213e;" +
            "-fx-border-color: #e94560;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        scorePanel.setMaxWidth(700);

        Text scoreTitle = new Text("SCOREBOARD — JOGOS PASSADOS");
        scoreTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        scoreTitle.setFill(Color.web("#e94560"));
        scorePanel.getChildren().add(scoreTitle);

        List<GameRecord> records = ScoreboardManager.loadRecords();

        if (records.isEmpty()) {
            Text empty = new Text("Ainda não existem jogos registados.");
            empty.setFont(Font.font("Arial", 12));
            empty.setFill(Color.LIGHTGRAY);
            scorePanel.getChildren().add(empty);
        } else {
            // Cabeçalho
            HBox header = createScoreRow("Data/Hora", "Jogadas", "Acertos", "Afundados", "Resultado", true);
            scorePanel.getChildren().add(header);

            for (GameRecord r : records) {
                boolean won = "VITÓRIA".equals(r.getResult());
                HBox recordRow = createScoreRow(
                        r.getDateTime(),
                        String.valueOf(r.getTotalMoves()),
                        String.valueOf(r.getTotalHits()),
                        String.valueOf(r.getSunkShips()),
                        r.getResult(),
                        false
                );
                // Colorir linha conforme resultado
                recordRow.setStyle("-fx-background-color: " +
                        (won ? "rgba(0,200,100,0.08)" : "rgba(233,69,96,0.08)") + ";");
                scorePanel.getChildren().add(recordRow);
            }

            // Totais
            long wins   = records.stream().filter(r -> "VITÓRIA".equals(r.getResult())).count();
            long losses = records.size() - wins;
            Text totals = new Text(String.format(
                    "Total: %d jogos  |  Vitórias: %d  |  Derrotas: %d",
                    records.size(), wins, losses));
            totals.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            totals.setFill(Color.LIGHTCYAN);
            scorePanel.getChildren().add(totals);
        }

        mainLayout.getChildren().add(scorePanel);
    }

    /**
     * Cria uma linha da tabela do scoreboard com 5 colunas.
     */
    private HBox createScoreRow(String col1, String col2, String col3, String col4, String col5, boolean isHeader) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 8, 4, 8));

        double[] widths = {160, 70, 70, 90, 90};
        String[] values = {col1, col2, col3, col4, col5};

        for (int i = 0; i < values.length; i++) {
            Text t = new Text(values[i]);
            t.setFont(isHeader
                    ? Font.font("Arial", FontWeight.BOLD, 11)
                    : Font.font("Arial", 11));

            if (isHeader) {
                t.setFill(Color.WHITE);
            } else if (i == 4) {
                t.setFill("VITÓRIA".equals(values[i]) ? Color.LIGHTGREEN : Color.web("#e94560"));
            } else {
                t.setFill(Color.LIGHTGRAY);
            }

            StackPane cell = new StackPane(t);
            cell.setPrefWidth(widths[i]);
            cell.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().add(cell);
        }
        return row;
    }

    // -------------------------------------------------------------------------
    // Utilitários de layout
    // -------------------------------------------------------------------------

    /**
     * Cria um contentor vertical para um tabuleiro, incluindo um título estilizado.
     *
     * @param title O título do tabuleiro.
     * @param board A instância de {@link BoardView} a conter.
     * @return Um {@link VBox} formatado.
     */
    private VBox createBoardContainer(String title, BoardView board) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Text t = new Text(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        t.setFill(Color.WHITE);
        box.getChildren().addAll(t, board);
        return box;
    }

    /**
     * Gera a barra de legenda de cores (significado das cores do tabuleiro).
     *
     * @return Um {@link HBox} contendo os itens da legenda.
     */
    private HBox createColorLegend() {
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(
                createLegendItem(Color.GRAY,       "Navio"),
                createLegendItem(Color.RED,        "Tiro Certeiro (*)"),
                createLegendItem(Color.DARKORANGE, "Afundado (-)"),
                createLegendItem(Color.WHITE,      "Água (o)"),
                createLegendItem(Color.LIGHTBLUE,  "Desconhecido")
        );
        return hBox;
    }

    /**
     * Cria um item individual da legenda com uma amostra de cor e um texto descritivo.
     *
     * @param color A cor a exibir no quadrado da legenda.
     * @param label A descrição da cor.
     * @return Um {@link HBox} com o quadrado colorido e o texto.
     */
    private HBox createLegendItem(Color color, String label) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Rectangle r = new Rectangle(15, 15, color);
        r.setStroke(Color.DARKGRAY);
        Text txt = new Text(label);
        txt.setFill(Color.LIGHTGRAY);
        txt.setFont(Font.font("Arial", 11));
        item.getChildren().addAll(r, txt);
        return item;
    }
}
