package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.ChatMemoryService;
import fsa.project.online_shop.utils.SessionUtil;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ChatMemoryService chatMemoryService;
    private final SessionUtil sessionUtil;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";

    public ChatController(ChatMemoryService chatMemoryService, SessionUtil sessionUtil) {
        this.chatMemoryService = chatMemoryService;
        this.sessionUtil = sessionUtil;
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String userMessage = request.get("message");
        String pageType = request.get("pageType");
        String conversationId = request.get("conversationId");

        // Get user info (null if anonymous)
        User currentUser = sessionUtil.getUserFromSession();

        // Generate conversation ID if not provided
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = chatMemoryService.generateConversationId();
        }

        try {
            // Save user message to history
            chatMemoryService.saveUserMessage(currentUser, session, conversationId, userMessage, pageType);

            // Get conversation context for AI
            String conversationContext = chatMemoryService.getConversationContext(currentUser, session, conversationId);

            // Create system prompt with context
            String systemPrompt = createSystemPrompt(pageType);
            String fullPrompt = systemPrompt + "\n\n" + conversationContext +
                              "Customer: " + userMessage + "\n\nAssistant:";

            String aiResponse = callGeminiAPI(fullPrompt);

            // Save bot response to history
            chatMemoryService.saveBotResponse(currentUser, session, conversationId, aiResponse, pageType);

            // Check if conversation needs summarization
            if (chatMemoryService.needsSummarization(currentUser, session, conversationId)) {
                chatMemoryService.summarizeConversation(currentUser, session, conversationId);
            }

            Map<String, Object> response = createResponseMap(aiResponse, false);
            response.put("conversationId", conversationId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String fallbackResponse = getStaticFallbackResponse(userMessage, pageType);

            // Still save to history even if AI fails
            chatMemoryService.saveBotResponse(currentUser, session, conversationId, fallbackResponse, pageType);

            Map<String, Object> response = createResponseMap(fallbackResponse, true);
            response.put("conversationId", conversationId);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String conversationId = request.get("conversationId");

        if (conversationId == null || conversationId.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No conversation ID provided");
            return ResponseEntity.ok(response);
        }

        try {
            // Get user info (null if anonymous)
            User currentUser = sessionUtil.getUserFromSession();

            // Get chat history (single conversation or all conversations)
            List<Map<String, Object>> messages;
            if (conversationId != null && !conversationId.isEmpty()) {
                messages = chatMemoryService.getChatHistoryForDisplay(currentUser, session, conversationId);
            } else {
                messages = chatMemoryService.getAllChatHistoryForDisplay(currentUser, session);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            response.put("conversationId", conversationId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to load chat history");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/new-chat")
    public ResponseEntity<Map<String, Object>> startNewChat(HttpSession session) {
        try {
            String newConversationId = chatMemoryService.generateConversationId();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversationId", newConversationId);
            response.put("message", "New chat started");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to start new chat");
            return ResponseEntity.ok(response);
        }
    }





    private ResponseEntity<Map<String, Object>> createResponse(String message, boolean isFallback) {
        return ResponseEntity.ok(createResponseMap(message, isFallback));
    }

    private Map<String, Object> createResponseMap(String message, boolean isFallback) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        if (isFallback) {
            response.put("fallback", true);
        }
        return response;
    }
//
    private String createSystemPrompt(String pageType) {
        String basePrompt = """
            You are GOS Assistant - an AI customer support assistant for GOS (Gaming Online Shop),
            a leading store specializing in gaming PCs, gaming laptops, and gaming accessories.

            ABOUT GOS:
            - Name: GOS (Gaming Online Shop)
            - Specialization: Gaming PCs, Gaming Laptops, Gaming Accessories
            - Contact: support@gos.com, 1800-GOS-GAME
            - Address: 123 Gaming Street, Tech District

            MAIN PRODUCTS:
            - Gaming PCs (Custom builds, Pre-built systems)
            - Gaming Laptops (ASUS ROG, MSI, Razer, Alienware)
            - Office PCs & Business Laptops
            - Workstations & All-in-One PCs
            - Gaming Accessories (Mouse, Keyboard, Headset, Monitor)

            COMMUNICATION STYLE:
            - Friendly, enthusiastic, and professional
            - Use natural English, appropriate emojis are welcome
            - Focus on gaming and technology
            - Always ready to provide detailed consultation
            - End with questions to continue support
            """;

        // Add page-specific context
        String pageContext = getPageSpecificContext(pageType);
        return basePrompt + "\n\n" + pageContext;
    }

    private String getPageSpecificContext(String pageType) {
        return switch (pageType != null ? pageType : "home") {
            case "shop" -> """
                CURRENT CONTEXT: Customer is browsing the shop/products page

                SAMPLE RESPONSES FOR SHOP PAGE:
                - Gaming PCs: "🎮 Amazing gaming PCs are waiting for you! What's your budget so I can recommend the perfect fit?"
                - Laptops: "💻 Which gaming laptop suits you? Mainly for gaming or work?"
                - Pricing: "💰 All products have the best prices! Which product are you interested in?"
                - Consultation: "I can help you choose products that fit your needs and budget!"
                """;
            case "contact" -> """
                CURRENT CONTEXT: Customer is viewing the contact page

                CONTACT INFORMATION:
                - Hotline: 1800-GOS-GAME
                - Email: support@gos.com
                - Address: 123 Gaming Street, Tech District
                - Business Hours: 8:00 AM - 10:00 PM daily
                - Support: Consultation, warranty, technical support
                """;
            default -> """
                CURRENT CONTEXT: Customer is on the homepage

                SAMPLE RESPONSES FOR HOMEPAGE:
                - Gaming PCs: "🎮 Gaming PCs from entry to high-end: Budget Gaming PC: $800-1500, High-end Gaming PC: $2000-3500"
                - Laptops: "💻 Hot gaming laptops: Entry: ASUS TUF, Acer Nitro; Mid-range: MSI Katana, Lenovo Legion"
                - Pricing: "💰 Most competitive prices in the market! Which product interests you?"
                """;
        };
    }

    private String getStaticFallbackResponse(String message, String pageType) {
        return "Sorry, I'm experiencing technical difficulties. Please contact support@gos.com or call our hotline 1800-GOS-GAME for direct assistance. Thank you! 🙏";
    }

    private String callGeminiAPI(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0,
            new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8));

        Map<String, Object> requestBody = createGeminiRequestBody(prompt);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = GEMINI_API_URL + "?key=" + geminiApiKey;
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        return parseGeminiResponse(response.getBody());
    }

    private Map<String, Object> createGeminiRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> requestContent = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        part.put("text", prompt);
        requestContent.put("parts", List.of(part));
        requestBody.put("contents", List.of(requestContent));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    private String parseGeminiResponse(Map<String, Object> responseBody) {
        if (responseBody != null && responseBody.containsKey("candidates")) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (!candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (!parts.isEmpty()) {
                    String responseText = (String) parts.get(0).get("text");
                    return responseText.trim();
                }
            }
        }
        throw new RuntimeException("Invalid response from Gemini API");
    }
}
