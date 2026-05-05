package com.example.hackaton_01.Service;

import com.example.hackaton_01.DTO.TropelSignalRequestDTO;
import com.example.hackaton_01.DTO.TropelSignalResponseDTO;
import com.example.hackaton_01.Event.TropelSignalCreatedEvent;
import com.example.hackaton_01.Model.CareResponse;
import com.example.hackaton_01.Model.Guardian;
import com.example.hackaton_01.Model.Sector;
import com.example.hackaton_01.Model.Tropel;
import com.example.hackaton_01.Model.TropelSignal;
import com.example.hackaton_01.Repository.CareResponseRepository;
import com.example.hackaton_01.Repository.GuardianRepository;
import com.example.hackaton_01.Repository.TropelRepository;
import com.example.hackaton_01.Repository.TropelSignalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TropelSignalService {

    private final TropelRepository tropelRepository;
    private final GuardianRepository guardianRepository;
    private final TropelSignalRepository signalRepository;
    private final CareResponseRepository careResponseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${github.models.url}")
    private String apiUrl;

    @Value("${github.models.model-id}")
    private String modelId;

    @Value("${github.token}")
    private String githubToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Promt exacto del documento
    private final String SYSTEM_PROMPT = """
        Eres el sistema de clasificación de señales del TropelCare Signal Engine, desarrollado por Tuckersoft.
        Recibes señales emitidas por criaturas digitales llamadas Tropeles y debes clasificarlas.
        Responde ÚNICAMENTE con este JSON en una sola línea, sin texto adicional, sin markdown, sin bloques de código:
        {"signalType":"<TIPO>","severity":"<GRAVEDAD>","assignedUnit":"<UNIDAD>","recommendedAction":"<acción breve y concreta en español>"}
        """;

    @Transactional
    public TropelSignalResponseDTO processSignal(TropelSignalRequestDTO request) {
        // 1. Validar request
        Tropel tropel = tropelRepository.findById(request.getTropelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tropel no encontrado"));

        Guardian guardian = guardianRepository.findById(request.getGuardianId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guardián no encontrado"));

        if (!tropel.getGuardian().getId().equals(guardian.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El guardián no coincide con el del Tropel");
        }

        Sector sector = tropel.getSector();
        boolean isFallback = false;
        JsonNode aiResponseJson = null;

        // 2. Llamar a la IA
        try {
            aiResponseJson = callGitHubModels(request.getRawContent());
            validateAiValues(aiResponseJson);
        } catch (Exception e) {
            System.err.println("Error con la IA, aplicando Fallback: " + e.getMessage());
            isFallback = true;
        }

        // 3. Preparar variables según sea éxito o fallback
        String signalType = isFallback ? "SENAL_CORRUPTA" : aiResponseJson.get("signalType").asText();
        String severity = isFallback ? "LEVE" : aiResponseJson.get("severity").asText();
        String assignedUnit = isFallback ? "Archivo de Senales" : aiResponseJson.get("assignedUnit").asText();
        String recommendedAction = isFallback ? "Archivar la señal y revisar manualmente si se repite." : aiResponseJson.get("recommendedAction").asText();
        String status = isFallback ? "ERROR" : "RECIBIDA";

        // 4. Actualizar Tropel y Sector SOLO si NO hubo fallback
        if (!isFallback) {
            updateTropelStats(tropel, severity);
            updateSectorStability(sector, signalType);
        }

        // 5. Guardar TropelSignal
        TropelSignal signal = TropelSignal.builder()
                .tropel(tropel)
                .guardian(guardian)
                .senderTag(request.getSenderTag())
                .rawContent(request.getRawContent())
                .signalType(signalType)
                .severity(severity)
                .assignedUnit(assignedUnit)
                .recommendedAction(recommendedAction)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        signal = signalRepository.save(signal);

        // 6. Guardar CareResponse
        CareResponse careResponse = CareResponse.builder()
                .signal(signal)
                .responseCode(mapResponseCode(signalType))
                .description(recommendedAction)
                .createdAt(Instant.now())
                .build();
        careResponseRepository.save(careResponse);

        // 7. Publicar evento SOLO si NO es fallback
        if (!isFallback) {
            eventPublisher.publishEvent(new TropelSignalCreatedEvent(signal.getId()));
        }

        // 8. Retornar DTO (201 se maneja en el Controller)
        TropelSignalResponseDTO responseDTO = new TropelSignalResponseDTO();
        responseDTO.setId(signal.getId());
        responseDTO.setSignalType(signalType);
        responseDTO.setSeverity(severity);
        responseDTO.setStatus(status);
        responseDTO.setRecommendedAction(recommendedAction);
        responseDTO.setCreatedAt(signal.getCreatedAt());

        return responseDTO;
    }

    // --- MÉTODOS AUXILIARES ---

    private JsonNode callGitHubModels(String rawContent) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(githubToken);

        Map<String, Object> requestBody = Map.of(
                "model", modelId,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", rawContent)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "/chat/completions", entity, String.class);

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String aiText = rootNode.path("choices").get(0).path("message").path("content").asText();

        // Extraer SOLO el JSON usando Regex por si la IA devuelve basura
        Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(aiText);
        if (matcher.find()) {
            return objectMapper.readTree(matcher.group());
        }
        throw new Exception("No se encontró JSON válido en la respuesta");
    }

    private void validateAiValues(JsonNode json) throws Exception {
        if (!json.hasNonNull("signalType") || !json.hasNonNull("severity")) throw new Exception("Faltan campos");

        List<String> validTypes = List.of("HAMBRE", "ABANDONO", "MUTACION", "FUGA", "CONFLICTO", "REPRODUCCION_MASIVA", "SENAL_CORRUPTA");
        List<String> validSeverities = List.of("LEVE", "MODERADO", "GRAVE", "CRITICO");

        if (!validTypes.contains(json.get("signalType").asText()) || !validSeverities.contains(json.get("severity").asText())) {
            throw new Exception("Valores fuera de las listas permitidas");
        }
    }

    private void updateTropelStats(Tropel tropel, String severity) {
        int energyMod = 0, chaosMod = 0, mutationMod = 0;

        switch (severity) {
            case "LEVE" -> { energyMod = -5; chaosMod = 5; }
            case "MODERADO" -> { energyMod = -10; chaosMod = 15; }
            case "GRAVE" -> { energyMod = -20; chaosMod = 30; }
            case "CRITICO" -> { energyMod = -30; chaosMod = 45; mutationMod = 1; }
        }

        tropel.setEnergyLevel(Math.max(0, Math.min(100, tropel.getEnergyLevel() + energyMod)));
        tropel.setChaosIndex(Math.max(0, Math.min(100, tropel.getChaosIndex() + chaosMod)));
        tropel.setMutationStage(Math.min(5, tropel.getMutationStage() + mutationMod));

        // Actualizar Vital State
        if (tropel.getChaosIndex() >= 80) {
            tropel.setVitalState("CRITICO");
        } else if (tropel.getChaosIndex() < 80 && tropel.getEnergyLevel() <= 20) {
            tropel.setVitalState("HAMBRIENTO");
        } else if (severity.equals("CRITICO")) {
            tropel.setVitalState("MUTANDO");
        } else if (severity.equals("GRAVE")) {
            tropel.setVitalState("AGITADO");
        }

        tropel.setUpdatedAt(Instant.now());
    }

    private void updateSectorStability(Sector sector, String signalType) {
        if (signalType.equals("FUGA")) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 10));
        } else if (signalType.equals("REPRODUCCION_MASIVA")) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 15));
        }
    }

    private String mapResponseCode(String signalType) {
        return switch (signalType) {
            case "HAMBRE" -> "DISPATCH_NUTRIENT_PACK";
            case "ABANDONO" -> "SEND_COMPANIONSHIP_PROTOCOL";
            case "MUTACION" -> "ISOLATE_AND_OBSERVE";
            case "FUGA" -> "ACTIVATE_SECTOR_LOCK";
            case "CONFLICTO" -> "DEPLOY_MEDIATION_FIELD";
            case "REPRODUCCION_MASIVA" -> "ENABLE_POPULATION_CONTROL";
            default -> "ARCHIVE_AND_IGNORE";
        };
    }
}