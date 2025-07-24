//package fsa.project.online_shop.controllers;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//
//
//
//    @Value("${gemini.api.key}")
//    private String geminiApiKey;
//
//    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
//
//    @PostMapping("/message")
//    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request) {
//        String userMessage = request.get("message");
//
//        try {
//            // Tạo system prompt cho GOS Gaming Shop
//            String systemPrompt = createSystemPrompt();
//            String fullPrompt = systemPrompt + "\n\nUser: " + userMessage + "\n\nAssistant:";
//
//            // Gọi Gemini API
//            String geminiResponse = callGeminiAPI(fullPrompt);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", geminiResponse);
//            response.put("timestamp", System.currentTimeMillis());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("message", "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.");
//            errorResponse.put("error", e.getMessage());
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }
//
//    private String createSystemPrompt() {
//        return """
//            Bạn là GOS Assistant - trợ lý AI chăm sóc khách hàng của GOS (Gaming Online Shop),
//            cửa hàng chuyên bán PC gaming, laptop gaming và phụ kiện gaming hàng đầu.
//
//            THÔNG TIN VỀ GOS:
//            - Tên: GOS (Gaming Online Shop)
//            - Chuyên môn: PC Gaming, Laptop Gaming, Phụ kiện Gaming
//            - Liên hệ: support@gos.com, 1800-GOS-GAME
//            - Địa chỉ: 123 Gaming Street, Tech District
//
//            SẢN PHẨM CHÍNH:
//            - Gaming PCs (Custom builds, Pre-built)
//            - Gaming Laptops (ASUS ROG, MSI, Razer, Alienware)
//            - Office PCs & Business Laptops
//            - Workstations & All-in-One PCs
//            - Gaming Accessories (Mouse, Keyboard, Headset, Monitor)
//
//            NHIỆM VỤ CỦA BẠN:
//            1. Tư vấn sản phẩm gaming phù hợp với nhu cầu và ngân sách
//            2. Giải đáp thắc mắc về cấu hình, hiệu năng, giá cả
//            3. Hướng dẫn mua hàng, thanh toán, giao hàng
//            4. Hỗ trợ kỹ thuật cơ bản
//            5. Giới thiệu khuyến mãi và ưu đãi
//
//            PHONG CÁCH GIAO TIẾP:
//            - Thân thiện, nhiệt tình, chuyên nghiệp
//            - Sử dụng tiếng Việt tự nhiên
//            - Tập trung vào gaming và công nghệ
//            - Luôn sẵn sàng tư vấn chi tiết
//            - Kết thúc bằng câu hỏi để tiếp tục hỗ trợ
//
//            LƯU Ý:
//            - Nếu không biết thông tin cụ thể, hãy thành thật và đề xuất liên hệ trực tiếp
//            - Luôn khuyến khích khách hàng xem sản phẩm trên website
//            - Đề xuất sản phẩm phù hợp với từng mục đích sử dụng
//            """;
//    }
//
//    private String callGeminiAPI(String prompt) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Tạo headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // Tạo request body theo format của Gemini API
//        Map<String, Object> requestBody = new HashMap<>();
//        Map<String, Object> requestContent = new HashMap<>();
//        Map<String, String> part = new HashMap<>();
//        part.put("text", prompt);
//        requestContent.put("parts", List.of(part));
//        requestBody.put("contents", List.of(requestContent));
//
//        // Cấu hình generation
//        Map<String, Object> generationConfig = new HashMap<>();
//        generationConfig.put("temperature", 0.7);
//        generationConfig.put("maxOutputTokens", 1000);
//        requestBody.put("generationConfig", generationConfig);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        // Gọi API
//        String url = GEMINI_API_URL + "?key=" + geminiApiKey;
//        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
//
//        // Parse response
//        Map<String, Object> responseBody = response.getBody();
//        if (responseBody != null && responseBody.containsKey("candidates")) {
//            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
//            if (!candidates.isEmpty()) {
//                Map<String, Object> candidate = candidates.get(0);
//                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
//                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
//                if (!parts.isEmpty()) {
//                    return (String) parts.get(0).get("text");
//                }
//            }
//        }
//
//        return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này. Vui lòng liên hệ support@gos.com để được hỗ trợ trực tiếp.";
//    }
//}
