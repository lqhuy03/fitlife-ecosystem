package com.fitlife.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.entity.*;
import com.fitlife.repository.AiPlanRepository;
import com.fitlife.repository.HealthMetricRepository;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final MemberRepository memberRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final AiPlanRepository aiPlanRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api-url}")
    private String geminiApiUrl;

    /**
     * Giai đoạn 1: Gọi AI tạo phác đồ và lưu vào lịch sử (JSON)
     * Nhận username từ Controller để truy vấn thông tin Member an toàn qua Token.
     */
    @Transactional
    public JsonNode generateWorkoutPlan(String username, AiWorkoutRequest request) {

        // 1. Tìm Member bằng username từ JWT Token
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        // 2. Kiểm tra chỉ số sức khỏe mới nhất để AI có dữ liệu phân tích BMI
        HealthMetric latestMetric = healthMetricRepository.findFirstByMemberOrderByRecordedAtDesc(member)
                .orElseThrow(() -> new RuntimeException("Hội viên chưa có chỉ số sức khỏe. Vui lòng cập nhật chiều cao/cân nặng tại Dashboard."));

        // 3. Xây dựng câu lệnh (Prompt) gửi tới Gemini
        String prompt = buildPrompt(member, latestMetric, request);

        // 4. Chuẩn bị Payload và Headers
        Map<String, Object> requestBody = createGeminiPayload(prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

        try {
            log.info("===> Đang gửi yêu cầu tới Gemini cho: {}", member.getFullName());
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            // 5. Bóc tách text trả về từ cấu hình JSON của Google Gemini
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseText = rootNode.at("/candidates/0/content/parts/0/text").asText();

            // 6. Làm sạch JSON (Xử lý trường hợp AI trả về có bọc ký tự Markdown ```json)
            String cleanJson = extractJson(aiResponseText);

            // 7. Lưu vào bảng lịch sử AI_WORKOUT_PLANS
            AiWorkoutPlan planHistory = AiWorkoutPlan.builder()
                    .member(member)
                    .goal(request.getGoal())
                    .planData(cleanJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Lưu và lấy lại đối tượng đã có ID tự sinh từ DB
            planHistory = aiPlanRepository.save(planHistory);

            log.info("===> Đã lưu phác đồ AI vào lịch sử thành công.");

            // 8. ĐẶC BIỆT: Đính kèm planId vào JSON để Frontend nhận được và có thể gọi API Activate ngay sau đó
            JsonNode resultNode = objectMapper.readTree(cleanJson);
            if (resultNode.isObject()) {
                ((ObjectNode) resultNode).put("planId", planHistory.getId());
            }

            return resultNode;

        } catch (Exception e) {
            log.error("Lỗi AI Service: {}", e.getMessage());
            throw new RuntimeException("Hệ thống AI đang bận hoặc phản hồi không đúng định dạng. Vui lòng thử lại sau.");
        }
    }

    /**
     * Giai đoạn 2: Bóc tách JSON từ lịch sử và áp dụng vào bảng WorkoutPlan chính thức.
     * Thao tác này sẽ thay đổi lịch tập của Member trên Dashboard.
     */
    @Transactional
    public void activatePlan(Long aiPlanId) {
        AiWorkoutPlan aiPlanRecord = aiPlanRepository.findById(aiPlanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phác đồ AI trong lịch sử"));

        Member member = aiPlanRecord.getMember();

        try {
            JsonNode root = objectMapper.readTree(aiPlanRecord.getPlanData());

            // 1. Hủy các lịch tập đang ACTIVE cũ của Member
            workoutPlanRepository.findByMemberAndStatus(member, WorkoutPlan.PlanStatus.ACTIVE)
                    .ifPresent(p -> {
                        p.setStatus(WorkoutPlan.PlanStatus.CANCELLED);
                        workoutPlanRepository.save(p);
                    });

            // 2. Khởi tạo WorkoutPlan chính thức
            WorkoutPlan officialPlan = WorkoutPlan.builder()
                    .name("Lịch tập AI: " + aiPlanRecord.getGoal())
                    .description(root.path("advice").asText())
                    .member(member)
                    .startDate(LocalDateTime.now())
                    .status(WorkoutPlan.PlanStatus.ACTIVE)
                    .build();

            Set<WorkoutSession> sessions = new LinkedHashSet<>();
            JsonNode scheduleNode = root.path("workoutSchedule");

            // 3. Duyệt qua từng ngày tập trong JSON
            for (JsonNode dayNode : scheduleNode) {
                WorkoutSession session = WorkoutSession.builder()
                        .dayOfWeek(dayNode.path("day").asText())
                        .focusArea(dayNode.path("focus").asText())
                        .workoutPlan(officialPlan)
                        .build();

                Set<WorkoutDetail> details = new LinkedHashSet<>();
                JsonNode exercisesNode = dayNode.path("exercises");

                // 4. Duyệt qua từng bài tập trong ngày
                for (JsonNode exNode : exercisesNode) {
                    WorkoutDetail detail = WorkoutDetail.builder()
                            .exercise_name(exNode.path("name").asText())
                            .sets(exNode.path("sets").asInt())
                            .reps(exNode.path("reps").asText())
                            .notes(exNode.path("notes").asText())
                            .session(session)
                            .build();
                    details.add(detail);
                }
                session.setDetails(details);
                sessions.add(session);
            }

            officialPlan.setSessions(sessions);
            workoutPlanRepository.save(officialPlan);
            log.info("===> Lịch tập AI đã trở thành lịch chính thức cho hội viên: {}", member.getFullName());

        } catch (Exception e) {
            log.error("Lỗi Kích hoạt: {}", e.getMessage());
            throw new RuntimeException("Cấu trúc dữ liệu AI không tương thích để tự động kích hoạt.");
        }
    }

    /**
     * Lấy danh sách phác đồ AI đã từng tư vấn cho hội viên
     */
    public List<AiWorkoutPlan> getMemberHistory(String username) {
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));
        return aiPlanRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // --- CÁC HÀM HỖ TRỢ (HELPERS) ---

    private String extractJson(String text) {
        String clean = text.trim();
        if (clean.contains("```json")) {
            clean = clean.substring(clean.indexOf("```json") + 7);
            clean = clean.substring(0, clean.lastIndexOf("```"));
        } else if (clean.contains("```")) {
            clean = clean.substring(clean.indexOf("```") + 3);
            clean = clean.substring(0, clean.lastIndexOf("```"));
        }
        return clean.trim();
    }

    private Map<String, Object> createGeminiPayload(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));
        payload.put("contents", List.of(content));

        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.3); // Giữ độ chính xác cao cho lịch tập gym
        payload.put("generationConfig", config);
        return payload;
    }

    private String buildPrompt(Member member, HealthMetric metric, AiWorkoutRequest req) {
        return String.format(
                "Bạn là Chuyên gia thể hình cấp cao chuẩn NASM. Hãy thiết kế lộ trình tập luyện cá nhân hóa cho hội viên: %s. " +
                        "Thông số hiện tại: Cân nặng %.1fkg, Chiều cao %.1fcm, BMI %.1f. Mục tiêu: %s. " +
                        "Tình trạng chấn thương: %s. Trình độ kinh nghiệm: %s. Thiết bị sẵn có: %s. " +
                        "YÊU CẦU QUAN TRỌNG: Chỉ trả về duy nhất 1 chuỗi JSON thuần túy (không giải thích), cấu trúc chính xác như sau: " +
                        "{\"disclaimer\": \"...\", \"advice\": \"...\", \"nutritionPlan\": {\"targetCalories\": 2000}, " +
                        "\"workoutSchedule\": [{\"day\": \"Thứ...\", \"focus\": \"...\", \"exercises\": [{\"name\": \"...\", \"sets\": 3, \"reps\": \"12\", \"notes\": \"...\"}]}]}",
                member.getFullName(), metric.getWeight(), metric.getHeight(), metric.getBmi(),
                req.getGoal(), (req.getInjuries() != null && !req.getInjuries().isEmpty() ? req.getInjuries() : "Không có"),
                req.getFitnessLevel(), (req.getEquipment() != null && !req.getEquipment().isEmpty() ? req.getEquipment() : "Phòng tập Gym đầy đủ")
        );
    }
}