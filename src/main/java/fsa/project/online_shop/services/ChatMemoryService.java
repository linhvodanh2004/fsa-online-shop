package fsa.project.online_shop.services;

import fsa.project.online_shop.models.ChatHistory;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMemoryService {
    
    private final ChatHistoryRepository chatHistoryRepository;
    private static final int MAX_CONTEXT_MESSAGES = 8; // 4 conversation pairs (reduced for learning)
    private static final int SUMMARIZATION_THRESHOLD = 16; // messages (reduced for learning)
    
    /**
     * Save user message to history (DB for logged users, session for anonymous)
     */
    @Transactional
    public void saveUserMessage(User user, HttpSession session, String conversationId,
                               String message, String pageType) {
        if (user != null) {
            // Logged-in user: save to database
            ChatHistory chatHistory = new ChatHistory(
                user, null, conversationId, "user", message, pageType
            );
            chatHistoryRepository.save(chatHistory);
            log.debug("💬 Saved user message to DB for user: {}", user.getEmail());
        } else {
            // Anonymous user: save to session
            saveToSession(session, conversationId, "user", message, pageType);
            log.debug("💬 Saved user message to session for conversation: {}", conversationId);
        }
    }
    
    /**
     * Save bot response to history (DB for logged users, session for anonymous)
     */
    @Transactional
    public void saveBotResponse(User user, HttpSession session, String conversationId,
                               String response, String pageType) {
        if (user != null) {
            // Logged-in user: save to database
            ChatHistory chatHistory = new ChatHistory(
                user, null, conversationId, "bot", response, pageType
            );
            chatHistoryRepository.save(chatHistory);
            log.debug("🤖 Saved bot response to DB for user: {}", user.getEmail());
        } else {
            // Anonymous user: save to session
            saveToSession(session, conversationId, "bot", response, pageType);
            log.debug("🤖 Saved bot response to session for conversation: {}", conversationId);
        }
    }

    /**
     * Save message to session for anonymous users
     */
    private void saveToSession(HttpSession session, String conversationId, String messageType,
                              String message, String pageType) {
        String sessionKey = "chat_" + conversationId;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) session.getAttribute(sessionKey);

        if (messages == null) {
            messages = new ArrayList<>();
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("type", messageType);
        messageData.put("message", message);
        messageData.put("pageType", pageType);
        messageData.put("timestamp", LocalDateTime.now().toString());

        messages.add(messageData);
        session.setAttribute(sessionKey, messages);
    }

    /**
     * Get messages from session for anonymous users
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getFromSession(HttpSession session, String conversationId) {
        String sessionKey = "chat_" + conversationId;
        List<Map<String, Object>> messages = (List<Map<String, Object>>) session.getAttribute(sessionKey);
        return messages != null ? messages : new ArrayList<>();
    }

    /**
     * Get conversation context for AI (last 4 message pairs for learning project)
     */
    public String getConversationContext(User user, HttpSession session, String conversationId) {
        StringBuilder context = new StringBuilder();

        if (user != null) {
            // Logged-in user: get from database
            List<ChatHistory> recentHistory = chatHistoryRepository.findRecentConversationHistory(
                user, null, conversationId
            );

            if (!recentHistory.isEmpty()) {
                List<ChatHistory> contextHistory = recentHistory.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .limit(MAX_CONTEXT_MESSAGES)
                    .collect(Collectors.toList());

                context.append("LỊCH SỬ CUỘC TRÒ CHUYỆN:\n");
                for (ChatHistory history : contextHistory) {
                    String role = "user".equals(history.getMessageType()) ? "Khách hàng" : "Assistant";
                    context.append(String.format("%s: %s\n", role, history.getMessage()));
                }

                log.debug("📚 Built DB context with {} messages for user: {}",
                         contextHistory.size(), user.getEmail());
            }
        } else {
            // Anonymous user: get from session
            List<Map<String, Object>> sessionMessages = getFromSession(session, conversationId);

            if (!sessionMessages.isEmpty()) {
                // Get last MAX_CONTEXT_MESSAGES
                int startIndex = Math.max(0, sessionMessages.size() - MAX_CONTEXT_MESSAGES);
                List<Map<String, Object>> contextMessages = sessionMessages.subList(startIndex, sessionMessages.size());

                context.append("LỊCH SỬ CUỘC TRÒ CHUYỆN:\n");
                for (Map<String, Object> message : contextMessages) {
                    String role = "user".equals(message.get("type")) ? "Khách hàng" : "Assistant";
                    context.append(String.format("%s: %s\n", role, message.get("message")));
                }

                log.debug("📚 Built session context with {} messages for conversation: {}",
                         contextMessages.size(), conversationId);
            }
        }

        if (context.length() > 0) {
            context.append("\nDựa trên lịch sử trò chuyện trên, hãy trả lời câu hỏi tiếp theo một cách nhất quán và có ngữ cảnh.\n\n");
        }

        return context.toString();
    }

    /**
     * Get chat history for display in frontend
     */
    public List<Map<String, Object>> getChatHistoryForDisplay(User user, HttpSession session, String conversationId) {
        if (user != null) {
            // Logged-in user: get from database
            List<ChatHistory> history = chatHistoryRepository.findByUserAndConversationIdOrderByCreatedAtAsc(user, conversationId);
            return history.stream()
                .map(chat -> {
                    Map<String, Object> message = new HashMap<>();
                    message.put("type", chat.getMessageType());
                    message.put("message", chat.getMessage());
                    message.put("timestamp", chat.getCreatedAt().toString());
                    message.put("conversationId", chat.getConversationId());
                    return message;
                })
                .collect(Collectors.toList());
        } else {
            // Anonymous user: get from session
            List<Map<String, Object>> messages = getFromSession(session, conversationId);
            // Add conversationId to each message
            messages.forEach(msg -> msg.put("conversationId", conversationId));
            return messages;
        }
    }

    /**
     * Get ALL chat history for display (all conversations)
     */
    public List<Map<String, Object>> getAllChatHistoryForDisplay(User user, HttpSession session) {
        List<Map<String, Object>> allMessages = new ArrayList<>();

        if (user != null) {
            // Logged-in user: get all conversations from database
            List<String> conversationIds = chatHistoryRepository.findUserConversations(user);

            for (String convId : conversationIds) {
                List<ChatHistory> history = chatHistoryRepository.findByUserAndConversationIdOrderByCreatedAtAsc(user, convId);

                // Add conversation separator if not first conversation
                if (!allMessages.isEmpty()) {
                    Map<String, Object> separator = new HashMap<>();
                    separator.put("type", "separator");
                    separator.put("message", "New Chat");
                    separator.put("conversationId", convId);
                    allMessages.add(separator);
                }

                // Add messages from this conversation
                allMessages.addAll(history.stream()
                    .map(chat -> {
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", chat.getMessageType());
                        message.put("message", chat.getMessage());
                        message.put("timestamp", chat.getCreatedAt().toString());
                        message.put("conversationId", chat.getConversationId());
                        return message;
                    })
                    .collect(Collectors.toList()));
            }
        } else {
            // Anonymous user: get all conversations from session
            Set<String> conversationIds = new HashSet<>();

            // Find all conversation IDs in session
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                if (attributeName.startsWith("chat_")) {
                    conversationIds.add(attributeName.substring(5)); // Remove "chat_" prefix
                }
            }

            // Sort conversation IDs (oldest first)
            List<String> sortedConvIds = new ArrayList<>(conversationIds);
            Collections.sort(sortedConvIds);

            for (String convId : sortedConvIds) {
                List<Map<String, Object>> messages = getFromSession(session, convId);

                if (!messages.isEmpty()) {
                    // Add conversation separator if not first conversation
                    if (!allMessages.isEmpty()) {
                        Map<String, Object> separator = new HashMap<>();
                        separator.put("type", "separator");
                        separator.put("message", "New Chat");
                        separator.put("conversationId", convId);
                        allMessages.add(separator);
                    }

                    // Add conversationId to each message and add to all messages
                    messages.forEach(msg -> msg.put("conversationId", convId));
                    allMessages.addAll(messages);
                }
            }
        }

        return allMessages;
    }
    
    /**
     * Generate new conversation ID
     */
    public String generateConversationId() {
        return "conv_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Check if conversation needs summarization
     */
    public boolean needsSummarization(User user, HttpSession session, String conversationId) {
        if (user != null) {
            // Logged-in user: check database
            Long messageCount = chatHistoryRepository.countConversationMessages(user, null, conversationId);
            return messageCount != null && messageCount > SUMMARIZATION_THRESHOLD;
        } else {
            // Anonymous user: check session
            List<Map<String, Object>> messages = getFromSession(session, conversationId);
            return messages.size() > SUMMARIZATION_THRESHOLD;
        }
    }
    
    /**
     * Summarize long conversations (future enhancement)
     */
    @Transactional
    public void summarizeConversation(User user, HttpSession session, String conversationId) {
        if (user != null) {
            // Logged-in user: summarize database records
            List<ChatHistory> allHistory = chatHistoryRepository.findByUserAndConversationIdOrderByCreatedAtAsc(user, conversationId);

            if (allHistory.size() > MAX_CONTEXT_MESSAGES) {
                List<ChatHistory> toSummarize = allHistory.subList(0, allHistory.size() - MAX_CONTEXT_MESSAGES);
                toSummarize.forEach(history -> history.setIsSummarized(true));
                chatHistoryRepository.saveAll(toSummarize);

                log.info("📝 Marked {} DB messages as summarized for user: {}",
                        toSummarize.size(), user.getEmail());
            }
        } else {
            // Anonymous user: trim session messages
            List<Map<String, Object>> messages = getFromSession(session, conversationId);

            if (messages.size() > MAX_CONTEXT_MESSAGES) {
                // Keep only last MAX_CONTEXT_MESSAGES
                List<Map<String, Object>> trimmedMessages = new ArrayList<>(
                    messages.subList(messages.size() - MAX_CONTEXT_MESSAGES, messages.size())
                );

                String sessionKey = "chat_" + conversationId;
                session.setAttribute(sessionKey, trimmedMessages);

                log.info("📝 Trimmed session messages to {} for conversation: {}",
                        trimmedMessages.size(), conversationId);
            }
        }
    }
    

    
    /**
     * Get user's conversation list (for logged-in users)
     */
    public List<String> getUserConversations(User user) {
        if (user == null) {
            return List.of();
        }
        return chatHistoryRepository.findUserConversations(user);
    }
}
