package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Classe que gerencia o jogo de Batalha Naval contra uma IA baseada em LLM (Large Language Model).
 * A IA comunica com a API Hugging Face para decidir onde disparar os tiros.
 */
public class AiGame {

    private static final String MODEL_ID = "meta-llama/Llama-3.3-70B-Instruct";
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final int MAX_OUTPUT_TOKENS    = 256;
    private static final int HTTP_TIMEOUT_SECONDS = 45;
    private static final int MAX_RETRIES          = 3;

    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    private final List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private boolean firstMove = true;
    private final SystemPrompt systemPrompt = new SystemPrompt();

    static java.util.function.Supplier<String> apiResponseOverride = null;
    static String envTokenOverride = null;

    @FunctionalInterface
    interface HttpCallable {
        java.net.http.HttpResponse<String> call(String requestJson) throws Exception;
    }

    static HttpCallable httpOverride = null;

    /**
     * Construtor: inicializa o cliente HTTP e carrega a chave de API das variáveis de ambiente.
     * Se a chave não estiver definida, lança uma exceção.
     */
    public AiGame(){
        String token = getApiToken();
        if (token == null || token.isEmpty()) {
           throw new IllegalStateException("API_KEY environment variable is not set.");
        }
        this.apiKey = token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    String getApiToken() {
        return (envTokenOverride != null) ? envTokenOverride : System.getenv("API_TOKEN");
    }

    /**
     * Construtor de teste: permite injetar apiKey sem variável de ambiente.
     */
     AiGame(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API_KEY environment variable is not set.");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Método principal: gera os tiros da IA consultando o LLM.
     * @param game Estado atual do jogo
     * @return Resposta processada do jogo com os resultados dos tiros
     */
    public String generateShots(IGame game){
        assert game != null;

        String userMessage = buildUserMessage(game);
        String shotsJson = askLlmForShots(userMessage);

        System.out.println("Tiros decididos pelo LLM: " + shotsJson);

        return game.readEnemyFire(shotsJson);
    }

    /**
     * Constrói a mensagem do utilizador para enviar à IA.
     * Na primeira jogada, envia o prompt do sistema e instruções iniciais.
     * Nas jogadas seguintes, envia o resultado da última rajada e pede novos tiros.
     * @param game Estado atual do jogo
     * @return String contendo a mensagem para enviar à IA
     */
    private String buildUserMessage(IGame game) {

        if (consumeFirstMove()) {
            return buildFirstMoveMessage();
        }
        return buildNextMoveMessage(game);
    }

    private boolean consumeFirstMove() {
        if (!firstMove) return false;
        firstMove = false;
        return true;
    }

    private String buildFirstMoveMessage() {
        return systemPrompt.firstMoveMessage();
    }

    private String buildNextMoveMessage(IGame game) {
        List<IMove> alienMoves = game.getAlienMoves();
        IMove lastMove = getLastMove(alienMoves);

        return "Resultado da tua última rajada (nº" + lastMove.getNumber() + "):\n"
                + lastMove.toDetailedString()
                + "\n"
                + buildAlreadyShotList(alienMoves)
                + buildGameInfo(game)
                + buildInstructions();
    }

    private String buildGameInfo(IGame game) {
        return "\nNavios inimigos ainda a flutuar: " + game.getRemainingShips() + ".\n\n";
    }

    private IMove getLastMove(List<IMove> alienMoves) {
        return alienMoves.get(alienMoves.size() - 1);
    }

    private String buildInstructions(){
        return systemPrompt.instructionsMessage();
    }


    /**
     * Constrói uma lista com todas as coordenadas já disparadas.
     * Isto ajuda a IA a evitar disparar para o mesmo local duas vezes.
     * @param alienMoves Lista de movimentos anteriores da IA
     * @return String formatada com as coordenadas já disparadas
     */
    private String buildAlreadyShotList(List<IMove> alienMoves) {
        StringBuilder sb = new StringBuilder();
        sb.append("COORDENADAS JÁ DISPARADAS (NUNCA repitas nenhuma destas):\n");

        List<String> coords = new ArrayList<>();

        for (IMove move : alienMoves) {
            for (IPosition pos : move.getShots()) {
                coords.add("" + pos.getClassicRow() + pos.getClassicColumn());
            }
        }

        if (coords.isEmpty()) {
            sb.append("Nenhuma ainda.\n");
        } else {
            sb.append(String.join(", ", coords)).append("\n");
            sb.append("Total: ").append(coords.size()).append(" coordenadas bloqueadas.\n");
        }

        return sb.toString();
    }

    /**
     * Pergunta à IA os próximos tiros, com suporte a múltiplas tentativas.
     * Se a IA responder algo inválido, tenta novamente até MAX_RETRIES vezes.
     * Se todas as tentativas falharem, usa um fallback aleatório.
     * @param userMessage Mensagem para enviar à IA
     * @return JSON com os 3 tiros decididos
     */
    private String askLlmForShots(String userMessage) {

        addToHistory("user", userMessage);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String llmResponse = callApi();

            addToHistory("assistant", llmResponse);

            String extracted = extractShotsJson(llmResponse);
            if (extracted != null) {
                return extracted;
            }

            System.out.println("Tentativa " + attempt + "/" + MAX_RETRIES
                    + " — resposta inválida: "
                    + llmResponse.substring(0, Math.min(80, llmResponse.length())));

            if (attempt < MAX_RETRIES) {
                addToHistory("user", systemPrompt.invalidResponseMessage());
            }
        }

        System.out.println("Todas as tentativas falharam. A usar fallback aleatório.");
        return buildRandomFallbackJson();
    }

    /**
     * Faz uma chamada HTTP à API da Hugging Face para obter a resposta da IA.
     * Envia o histórico da conversa e recebe a próxima mensagem.
     * @return Resposta processada da IA (apenas o texto da mensagem)
     * @throws RuntimeException Se houver erro na chamada ou timeout
     */
    private String callApi() {

        // Para teste: permite substituir a chamada real à API
        if (apiResponseOverride != null) {
            return apiResponseOverride.get();
        }

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", MODEL_ID);
            requestBody.put("messages", conversationHistory);
            requestBody.put("max_tokens", MAX_OUTPUT_TOKENS);
            requestBody.put("temperature", 0.2);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = executeHttpCall(requestJson);

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erro HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            return parseAiResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chamada interrompida", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro na API: " + e.getMessage(), e);
        }
    }

    HttpResponse<String> executeHttpCall(String requestJson) throws Exception {
        if (httpOverride != null) {
            return httpOverride.call(requestJson);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)  // Bearer token no header
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    }

    /**
     * Interpreta a resposta JSON da API da Hugging Face.
     * Extrai o texto da mensagem da IA do objeto de resposta complexo.
     * @param responseBody String JSON com a resposta completa da API
     * @return Texto da mensagem da IA
     * @throws RuntimeException Se a estrutura da resposta for inesperada
     */
    private String parseAiResponse(String responseBody) {
        try {
            JsonNode root    = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode content = choices.get(0)
                        .path("message")
                        .path("content");

                if (!content.isMissingNode()) {
                    return content.asText().trim();
                }
            }

            throw new RuntimeException("Estrutura inesperada: " + responseBody);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao interpretar resposta: " + e.getMessage(), e);
        }
    }


    /**
     * Extrai o JSON dos tiros da resposta da IA.
     * Tenta múltiplas estratégias de extração (código, arrays, texto direto).
     * @param llmResponse Resposta completa da IA (pode conter texto extra)
     * @return String com o JSON dos tiros, ou null se não encontrou
     */
    private String extractShotsJson(String llmResponse) {
        if (llmResponse == null || llmResponse.isBlank()) return null;

        String trimmed = llmResponse.trim();
        if (trimmed.startsWith("[") && isValidShotsJson(trimmed)) return trimmed;

        Pattern codeBlock = Pattern.compile(
                "```(?:json)?\\s*(\\[.*?])\\s*```",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = codeBlock.matcher(llmResponse);
        if (m.find()) {
            String c = m.group(1).trim();
            if (isValidShotsJson(c)) return c;
        }

        Pattern arrayPat = Pattern.compile("(\\[\\s*\\{.*?}\\s*])", Pattern.DOTALL);
        Matcher m2 = arrayPat.matcher(llmResponse);
        while (m2.find()) {
            String c = m2.group(1).trim();
            if (isValidShotsJson(c)) return c;
        }

        return null;
    }

    /**
     * Valida se o JSON contém exatamente 3 tiros com coordenadas válidas.
     * Verifica se é um array, tem 3 elementos, e cada tiro tem linha (A-J) e coluna (1-10) válidas.
     * @param json String com o JSON a validar
     * @return true se for válido, false caso contrário
     */
    private boolean isValidShotsJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray() || root.size() != Game.NUMBER_SHOTS) return false;

            for (JsonNode shot : root) {
                JsonNode rowNode = shot.get("row");
                JsonNode colNode = shot.get("column");
                if (rowNode == null || colNode == null) return false;

                String row = rowNode.asText().toUpperCase();
                int    col = colNode.asInt();
                if (row.length() != 1 || row.charAt(0) < 'A' || row.charAt(0) > 'J') return false;
                if (col < 1 || col > 10) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gera um fallback com 3 tiros aleatórios quando a IA falha.
     * Cria uma lista de todas as posições possíveis, embaralha e pega nos primeiros 3.
     * @return JSON formatado com 3 tiros aleatórios
     */
    private String buildRandomFallbackJson() {
        List<IPosition> available = new ArrayList<>();
        for (int r = 0; r < Game.BOARD_SIZE; r++)
            for (int c = 0; c < Game.BOARD_SIZE; c++)
                available.add(new Position(r, c));

        Collections.shuffle(available, new Random());
        return Game.jsonShots(available.subList(0, Game.NUMBER_SHOTS));
    }

    /**
     * Adiciona uma mensagem ao histórico de conversa.
     * Usa um mapa para armazenar o papel (role) e o conteúdo de cada mensagem.
     * @param role "user" ou "assistant"
     * @param content Texto da mensagem
     */
    private void addToHistory(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        conversationHistory.add(message);
    }

    /**
     * Retorna o número de mensagens no histórico de conversa.
     * @return Tamanho do histórico de conversação
     */
    public int getConversationLength() {
        return conversationHistory.size();
    }

    /**
     * Indica se a IA já fez pelo menos um movimento (ou seja, se o jogo já começou).
     * @return true se a IA já começou a jogar
     */
    public boolean isInitialized() {
        return !firstMove;
    }


}
