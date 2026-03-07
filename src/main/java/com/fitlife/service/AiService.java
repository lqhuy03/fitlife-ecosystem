package com.fitlife.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.entity.HealthMetric;
import com.fitlife.entity.Member;
import com.fitlife.repository.HealthMetricRepository;
import com.fitlife.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final MemberRepository memberRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    public JsonNode generateWorkoutPlan(AiWorkoutRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        HealthMetric latestMetric = healthMetricRepository.findFirstByMemberOrderByRecordedDateDesc(member)
                .orElseThrow(() -> new RuntimeException("Hội viên chưa đo BMI."));

        String prompt = buildPrompt(member, latestMetric, request);

        // Payload tối giản: contents -> parts -> text
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(parts));
        requestBody.put("contents", List.of(content));

        // Cấu hình config cơ bản
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.2);
        requestBody.put("generationConfig", config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("Đang gọi Gemini 2.5 Flash...");
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.at("/candidates/0/content/parts/0/text").asText();

            // Xử lý dọn dẹp Markdown
            String cleanJson = aiResponseText.trim();
            if (cleanJson.contains("```json")) {
                cleanJson = cleanJson.substring(cleanJson.indexOf("```json") + 7);
                cleanJson = cleanJson.substring(0, cleanJson.lastIndexOf("```"));
            } else if (cleanJson.contains("```")) {
                cleanJson = cleanJson.substring(cleanJson.indexOf("```") + 3);
                cleanJson = cleanJson.substring(0, cleanJson.lastIndexOf("```"));
            }

            return objectMapper.readTree(cleanJson.trim());

        } catch (Exception e) {
            log.error("Lỗi AI: {}", e.getMessage());
            throw new RuntimeException("Huấn luyện viên AI đang bận.");
        }
    }

    private String buildPrompt(Member member, HealthMetric metric, AiWorkoutRequest req) {
        return "Bạn là PT chuyên nghiệp. Hãy tạo phác đồ cho khách hàng tên " + member.getFullName() +
                ". Mục tiêu: " + req.getGoal() + ". BMI: " + metric.getBmi() +
                ". Chấn thương: " + (req.getInjuries() != null ? req.getInjuries() : "Không") +
                ". Trình độ: " + req.getFitnessLevel() + ". " +
                "Yêu cầu trả về DUY NHẤT 1 khối JSON (không giải thích thêm): " +
                "{ \"disclaimer\": \"...\", \"advice\": \"...\", \"nutritionPlan\": { \"targetCalories\": 0 }, \"workoutSchedule\": [] }";
    }
}