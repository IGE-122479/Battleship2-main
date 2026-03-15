package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

public class Game implements IGame
{
	/**
	 * Prints the game board by representing the positions of ships, adjacent tiles,
	 * shots, and other game elements onto the console. The method also optionally
	 * displays shot positions and a legend explaining the symbols used on the board.
	 *
	 * @param fleet       the fleet of ships to be displayed on the board. Ships are marked
	 *                    and their positions are shown according to their placement.
	 * @param moves       the list of moves containing shots. If shot positions are shown,
	 *                    they will be rendered based on their outcome (hit, miss, etc.).
	 * @param show_shots  if true, displays the shots taken during the game and marks
	 *                    their result (hit or miss) on the board.
	 * @param showLegend  if true, displays an explanatory legend of the symbols used
	 *                    to represent various elements such as ships, misses, hits, etc.
	 */
	public static void printBoard(IFleet fleet, List<IMove> moves, boolean show_shots, boolean showLegend) {

		assert fleet != null;
		assert moves != null;

		char[][] map = new char[BOARD_SIZE][BOARD_SIZE];

		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				map[r][c] = EMPTY_MARKER;

		for (IShip ship : fleet.getShips()) {
			for (IPosition ship_pos : ship.getPositions())
				map[ship_pos.getRow()][ship_pos.getColumn()] = SHIP_MARKER;
			if (!ship.stillFloating())
				for (IPosition adjacent_pos : ship.getAdjacentPositions())
					map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = SHIP_ADJACENT_MARKER;
		}

		if (show_shots)
			for (IMove move : moves)
				for (IPosition shot : move.getShots()) {
					if (shot.isInside()){
						int row = shot.getRow();
						int col = shot.getColumn();
						if (map[row][col] == SHIP_MARKER)
							map[row][col] = SHOT_SHIP_MARKER;
						if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER)
							map[row][col] = SHOT_WATER_MARKER;
					}
				}

		System.out.println();
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();

		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");

		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++)
				System.out.print(" " + map[row][col]);
			System.out.println(" |");
		}

		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++)
			System.out.print("--");
		System.out.println("-+");

		if (showLegend) {
			System.out.println("          LEGENDA");
			System.out.println("'" + SHIP_MARKER + "'->navio, '" + SHIP_ADJACENT_MARKER + "'->adjacente a navio, '" + EMPTY_MARKER + "'->água");
			System.out.println("'" + SHOT_SHIP_MARKER + "'->Tiro certeiro, '" + SHOT_WATER_MARKER + "'->Tiro na água");
		}
		System.out.println();
	}

	/**
	 * Serializes a list of shot positions into a JSON string. Each shot is represented
	 * with its classic row and column values. The method uses the Jackson library for
	 * JSON serialization.
	 *
	 * @param shots a list of shot positions to be serialized. Each position is represented
	 *              by an implementation of the {@code IPosition} interface. The list must
	 *              not be null.
	 * @return a formatted JSON string containing the shot positions. Each shot includes
	 *         its classic row and column.
	 * @throws RuntimeException if an error occurs during JSON serialization.
	 */
	public static String jsonShots(List<IPosition> shots) {

		assert shots != null;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// 1. Create a simplified list containing only the desired data
		List<Map<String, Object>> simplifiedShots = new ArrayList<>();
		for (IPosition shot : shots) {
			Map<String, Object> simplePos = new LinkedHashMap<>();
			// We use getClassicRow() and getClassicColumn() based on your current JSON output
			simplePos.put("row", String.valueOf(shot.getClassicRow()));
			simplePos.put("column", shot.getClassicColumn());
			simplifiedShots.add(simplePos);
		}

		String jsonString = null;
		try {
			// 2. Serialize the simplified list instead of the raw 'shots' list
			jsonString = objectMapper.writeValueAsString(simplifiedShots);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON", e);
		}

//		System.out.println(jsonString);
//		System.out.println();

		// Retornar o JSON
		return jsonString;
	}

	//------------------------------------------------------------------
	public static final int BOARD_SIZE = 10;
	public static final int NUMBER_SHOTS = 3;

	private static final char EMPTY_MARKER = '.';
	private static final char SHIP_MARKER = '#';
	private static final char SHOT_SHIP_MARKER = '*';
	private static final char SHOT_WATER_MARKER = 'o';
	private static final char SHIP_ADJACENT_MARKER = '-';

	//------------------------------------------------------------------
	private final IFleet myFleet;
	private final List<IMove> alienMoves;

	private final IFleet alienFleet;
	private final List<IMove> myMoves;

	private Integer countInvalidShots;
	private Integer countRepeatedShots;
	private Integer countHits;
	private Integer countSinks;
	private int moveNumber;
	private int myMoveNumber;   // contador das minhas jogadas sobre o adversário

	/** Cronómetro para medir o tempo de cada jogada. */
	private final MoveTimer moveTimer;

	//------------------------------------------------------------------
	public Game(IFleet myFleet)
	{

		this.myMoveNumber = 1;
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<IMove>();
		this.myMoves = new ArrayList<IMove>();

		this.alienFleet = Fleet.createRandom();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;

		this.moveTimer = new MoveTimer();
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return alienFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}



	/**
	 * Gera uma rajada de tiros aleatórios inteligentes por parte do inimigo.
	 * O método evita posições adjacentes a navios já afundados e posições já atacadas.
	 * Após gerar os tiros, processa-os contra a frota do jogador e devolve os resultados.
	 *
	 * @return String JSON com os resultados da rajada (hits, misses, sinks).
	 */
	public String randomEnemyFire() {

		// Iniciar o cronómetro antes de gerar os tiros
		moveTimer.start();

		// Criar uma instância de Random com uma seed baseada no timestamp atual
		Random random = new Random(System.currentTimeMillis());

		Set<IPosition> usablePositions = new HashSet<IPosition>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				usablePositions.add(new Position(r, c));

		this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move ->  usablePositions.removeAll(move.getShots()));

		List<IPosition> candidateShots = new ArrayList<>(usablePositions);

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<IPosition>();

		System.out.println();
		// Gerar coordenadas únicas até atingir o número definido por NUMBER_SHOTS

		IPosition newShot = null;
		if (candidateShots.size() >= Game.NUMBER_SHOTS)
			while (shots.size() < Game.NUMBER_SHOTS) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
		else {
			while (shots.size() < candidateShots.size()) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
			while (shots.size() < Game.NUMBER_SHOTS)
				shots.add(newShot);
		}

		System.out.print("rajada ");
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();

		// Serializar os tiros em JSON e delegar no método readEnemyFire
		String shotsJson = Game.jsonShots(shots);
		return readEnemyFire(shotsJson);
	}

	/**
	 * Recebe os meus tiros sobre a frota do adversário em formato JSON,
	 * processa-os e devolve um JSON com os resultados.
	 *
	 * Formato de entrada esperado:
	 * [ {"row":"A","column":3}, {"row":"B","column":7}, {"row":"C","column":1} ]
	 *
	 * @param json   string JSON com a lista dos meus tiros
	 * @return string JSON com os resultados (via Move.processEnemyFire)
	 */
	public String sendMyShotsJson(String json) {

		assert json != null;

		ObjectMapper mapper = new ObjectMapper();
		List<IPosition> shots = new ArrayList<>();

		try {
			List<Map<String, Object>> parsed = mapper.readValue(json,
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

			for (Map<String, Object> entry : parsed) {
				char row    = ((String) entry.get("row")).charAt(0);
				int  column = (Integer) entry.get("column");
				shots.add(new Position(row, column));
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao interpretar JSON dos meus tiros: " + e.getMessage(), e);
		}

		if (shots.size() != NUMBER_SHOTS)
			throw new IllegalArgumentException("O JSON deve conter exactamente " + NUMBER_SHOTS + " tiros.");

		List<ShotResult> shotResults = new ArrayList<>();
		List<IPosition> alreadyShot  = new ArrayList<>();
		for (IPosition pos : shots) {
			shotResults.add(fireMyShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		// Parar o cronómetro e guardar o tempo na jogada
		long elapsed = moveTimer.stop();

		Move move = new Move(myMoveNumber, shots, shotResults);
		move.setDuration(elapsed);
		move.processEnemyFire(true);
		myMoves.add(move);
		myMoveNumber++;

		// Devolver JSON com os resultados da jogada
		return move.processEnemyFire(false);
	}

	/**
	 * Processa os tiros disparados pelo inimigo recebidos em formato JSON.
	 * O método desserializa o JSON, valida as posições e aplica o tiro à frota do jogador,
	 * registando os resultados (acerto, água, ou navio afundado).
	 *
	 * @param json String JSON contendo a lista de tiros (ex: [{"row":"A","column":1}, ...])
	 * @return Uma String JSON com o relatório detalhado dos resultados de cada tiro.
	 * @throws RuntimeException se ocorrer um erro na interpretação do JSON.
	 */
	public String readEnemyFire(String json) {

		assert json != null;

		ObjectMapper mapper = new ObjectMapper();
		List<IPosition> shots = new ArrayList<>();

		try {
			List<Map<String, Object>> parsed = mapper.readValue(json,
					mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

			for (Map<String, Object> entry : parsed) {
				char row    = ((String) entry.get("row")).charAt(0);
				int  column = (Integer) entry.get("column");
				shots.add(new Position(row, column));
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao interpretar JSON dos tiros do inimigo: " + e.getMessage(), e);
		}

		List<ShotResult> shotResults = new ArrayList<>();
		List<IPosition> alreadyShot  = new ArrayList<>();
		for (IPosition pos : shots) {
			shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		long elapsed = moveTimer.stop();

		Move move = new Move(moveNumber, shots, shotResults);
		move.setDuration(elapsed);
		move.processEnemyFire(true);
		alienMoves.add(move);
		moveNumber++;

		// Devolver JSON com os resultados da jogada
		return move.processEnemyFire(false);
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		//List<ShotResult> shotResults = new ArrayList<ShotResult>();
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Must fire exactly " + NUMBER_SHOTS + " shots per move.");
		}
		// Delegar em receiveEnemyShotsJson para evitar lógica duplicada
		readEnemyFire(Game.jsonShots(shots));

	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else
		{
			ship.shoot(pos);
			countHits++;
			if (!ship.stillFloating()) {
				countSinks++;
			}
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	// ==================== OS MEUS TIROS SOBRE O ADVERSÁRIO ====================

	/**
	 * Lê a rajada do jogador sobre a frota do adversário a partir do scanner.
	 *
	 * @param in o scanner para ler as posições dos tiros
	 * @return string JSON com as posições dos tiros efectuados
	 */
	public String readMyFire(Scanner in) {

		assert in != null;

		// Iniciar o cronómetro
		moveTimer.start();

		String input = in.nextLine().trim();

		// Se a linha lida ficou vazia (o utilizador carregou Enter após "rajada"),
		// aguardamos a linha seguinte com os tiros — é aqui que está a demora real.
		if (input.isEmpty()) {
			input = in.nextLine().trim();
		}

		List<IPosition> shots = new ArrayList<>();

		Scanner inputScanner = new Scanner(input);
		while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
			String token = inputScanner.next();

			if (token.matches("[A-Za-z]")) {
				if (inputScanner.hasNextInt()) {
					int row = inputScanner.nextInt();
					shots.add(new Position(token.toUpperCase().charAt(0), row));
				} else {
					throw new IllegalArgumentException("Posição incompleta! A coluna '" + token + "' não é seguida por uma linha.");
				}
			} else {
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Deve inserir exactamente " + NUMBER_SHOTS + " posições!");
		}

		// Serializar para JSON e delegar no método sendMyShotsJson
		String shotsJson = Game.jsonShots(shots);
		return sendMyShotsJson(shotsJson);

	}

	/**
	 * Efectua um único tiro do jogador sobre a frota do adversário.
	 *
	 * @param pos        a posição onde efectuar o tiro
	 * @param isRepeated true se o tiro for uma repetição
	 * @return o resultado do tiro
	 */
	public ShotResult fireMyShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside())
			return new ShotResult(false, false, null, false);

		if (isRepeated || myRepeatedShot(pos))
			return new ShotResult(true, true, null, false);

		IShip ship = alienFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else {
			ship.shoot(pos);
			if (!ship.stillFloating())
				System.out.println("*** Afundaste um(a) " + ship.getCategory() + "! ***");
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	/**
	 * Verifica se a posição já foi alvo de um tiro meu anteriormente.
	 *
	 * @param pos a posição a verificar
	 * @return true se a posição já foi atacada por mim
	 */
	public boolean myRepeatedShot(IPosition pos) {
		assert pos != null;

		for (IMove move : myMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	/**
	 * Imprime o tabuleiro do adversário de forma "escondida", mostrando apenas
	 * onde o jogador já disparou e o número da jogada correspondente.
	 * * @param showLegend se true, exibe a legenda dos símbolos no final.
	 */
	private void printAlienBoardHidden(boolean showLegend) {
		// Usamos String para suportar números de jogadas no mapa
		String[][] map = new String[BOARD_SIZE][BOARD_SIZE];

		// Inicializar o tabuleiro com o marcador de água
		for (int r = 0; r < BOARD_SIZE; r++) {
			for (int c = 0; c < BOARD_SIZE; c++) {
				map[r][c] = String.valueOf(EMPTY_MARKER);
			}
		}

		// Preencher o tabuleiro com o número da jogada (usamos o resto da divisão por 10
		// para garantir que o tabuleiro mantém o alinhamento visual com apenas 1 caractere)
		for (IMove move : myMoves) {
			for (IPosition shot : move.getShots()) {
				if (shot.isInside()) {
					map[shot.getRow()][shot.getColumn()] = String.valueOf(move.getNumber() % 10);
				}
			}
		}

		// Impressão do cabeçalho das colunas (1 2 3...)
		System.out.println();
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();

		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");

		// Impressão das linhas (A, B, C...) e conteúdo do mapa
		for (int row = 0; row < BOARD_SIZE; row++) {
			char rowLabel = (char) ('A' + row);
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++) {
				System.out.print(" " + map[row][col]);
			}
			System.out.println(" |");
		}

		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("-+");

		if (showLegend) {
			System.out.println("          LEGENDA");
			System.out.println("'" + EMPTY_MARKER + "'->água, 'N'->tiro efetuado na jogada nº N (mostra apenas último dígito)");
		}
		System.out.println();

		// Chamar a visualização da frota logo abaixo do mapa
		printAlienFleetHealth();
	}

	/**
	 * Mostra o estado de saúde da frota adversária.
	 * Revela coordenadas e jogadas apenas para partes já atingidas.
	 */
	private void printAlienFleetHealth() {
		System.out.println("=== ESTADO DE DANOS DA FROTA ADVERSÁRIA ===");

		// Percorre todos os navios da frota inimiga
		for (IShip ship : alienFleet.getShips()) {
			System.out.printf("%-15s ", ship.getCategory());

			// Primeiro: mostrar as células atingidas (na ordem em que foram atingidas)
			// Segundo: mostrar [.] para as células ainda não atingidas
			// Desta forma o jogador vê os hits à esquerda mas não sabe a posição real do navio

			List<String> hitCells = new ArrayList<>();
			int unknownCount = 0;

			// Percorre cada célula individual do navio
			for (IPosition pos : ship.getPositions()) {
				IMove hittingMove = null;

				// Verifica se este ponto específico foi atingido por algum tiro meu
				for (IMove move : myMoves) {
					if (move.getShots().contains(pos)) {
						hittingMove = move;
						break;
					}
				}

				if (hittingMove != null) {
					// Se foi atingido: Mostra a coordenada (ex: A5) e o número da jogada
					String coord = pos.getClassicRow() + "" + pos.getClassicColumn();
					hitCells.add(String.format("[%s:%d]", coord, hittingMove.getNumber()));
				} else {
					unknownCount++;
				}
			}

			// Mostrar hits primeiro
			for (String cell : hitCells)
				System.out.print(cell + " ");

			// Depois as células desconhecidas
			for (int i = 0; i < unknownCount; i++)
				System.out.print("[.] ");

			if (!ship.stillFloating()) {
				System.out.print(" -> AFUNDADO!");
			}
			System.out.println();
		}
		System.out.println("===========================================");
	}

	/**
	 * Devolve o número de navios do adversário ainda a flutuar.
	 *
	 * @return número de navios do adversário por afundar
	 */
	public int getAlienRemainingShips() {
		return alienFleet.getFloatingShips().size();
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		System.out.println("=== O MEU TABULEIRO ===");
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend)
	{
		System.out.println("=== TABULEIRO DO ADVERSÁRIO ===");
		printAlienBoardHidden(show_legend);
	}

	/**
	 * Imprime uma tabela com o tempo gasto em cada jogada,
	 * incluindo o total, a média, a jogada mais rápida e a mais lenta.
	 */
	public void printTimingStats() {
		if (myMoves.isEmpty()) {
			System.out.println("Nenhuma jogada registada.");
			return;
		}

		System.out.println();
		System.out.println("+----------- RELÓGIO DAS JOGADAS -----------+");
		System.out.printf("| %-10s | %-28s |%n", "Jogada nº", "Tempo gasto");
		System.out.println("+------------+-----------------------------+");

		long total = 0L;
		long fastest = Long.MAX_VALUE;
		long slowest = Long.MIN_VALUE;
		int fastestMove = 0, slowestMove = 0;

		for (IMove iMove : myMoves) {
			if (iMove instanceof Move move) {
				long d = move.getDuration();
				total += d;

				if (d < fastest) { fastest = d; fastestMove = move.getNumber(); }
				if (d > slowest) { slowest = d; slowestMove = move.getNumber(); }

				System.out.printf("| %-10d | %-28s |%n",
						move.getNumber(), MoveTimer.format(d));
			}
		}

		System.out.println("+------------+-----------------------------+");

		long count = myMoves.size();
		long avg = count > 0 ? total / count : 0L;

		System.out.printf("| %-10s | %-28s |%n", "TOTAL", MoveTimer.format(total));
		System.out.printf("| %-10s | %-28s |%n", "MÉDIA", MoveTimer.format(avg));
		System.out.printf("| %-10s | %-28s |%n", "MAIS RÁPIDA (nº" + fastestMove + ")", MoveTimer.format(fastest));
		System.out.printf("| %-10s | %-28s |%n", "MAIS LENTA (nº"  + slowestMove + ")", MoveTimer.format(slowest));
		System.out.println("+------------+-----------------------------+");
		System.out.println();
	}


	public void over() {
			// Mostrar o relógio das jogadas antes da mensagem de fim de jogo
			printTimingStats();
			PdfExporter.exportGameToPdf(this);
			System.out.println();
			System.out.println("+--------------------------------------------------------------+");
			System.out.println("| Maldito sejas, Java Sparrow, eu voltarei, glub glub glub ... |");
			System.out.println("+--------------------------------------------------------------+");
	}

	/**
	 * Mensagem de vitória do jogador.
	 */
	public void win() {
		printTimingStats();
		PdfExporter.exportGameToPdf(this);
		System.out.println();
		System.out.println("+--------------------------------------------------------------+");
		System.out.println("|       Parabéns! Afundaste toda a frota do adversário!        |");
		System.out.println("+--------------------------------------------------------------+");
	}
}
