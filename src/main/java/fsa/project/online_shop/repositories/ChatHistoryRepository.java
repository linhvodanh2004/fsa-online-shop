package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.ChatHistory;
import fsa.project.online_shop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
    // Get recent chat history for logged-in user
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.user = :user AND ch.conversationId = :conversationId " +
           "ORDER BY ch.createdAt ASC")
    List<ChatHistory> findByUserAndConversationIdOrderByCreatedAtAsc(
            @Param("user") User user, 
            @Param("conversationId") String conversationId);
    
    // Get recent chat history for anonymous user (session-based)
    @Query("SELECT ch FROM ChatHistory ch WHERE ch.sessionId = :sessionId AND ch.conversationId = :conversationId " +
           "ORDER BY ch.createdAt ASC")
    List<ChatHistory> findBySessionIdAndConversationIdOrderByCreatedAtAsc(
            @Param("sessionId") String sessionId, 
            @Param("conversationId") String conversationId);
    
    // Get last 6 conversations for context (memory limit)
    @Query("SELECT ch FROM ChatHistory ch WHERE " +
           "(ch.user = :user OR ch.sessionId = :sessionId) AND ch.conversationId = :conversationId " +
           "ORDER BY ch.createdAt DESC LIMIT 12") // 6 pairs of user-bot messages
    List<ChatHistory> findRecentConversationHistory(
            @Param("user") User user, 
            @Param("sessionId") String sessionId,
            @Param("conversationId") String conversationId);
    
    // Count messages in current conversation
    @Query("SELECT COUNT(ch) FROM ChatHistory ch WHERE " +
           "(ch.user = :user OR ch.sessionId = :sessionId) AND ch.conversationId = :conversationId")
    Long countConversationMessages(
            @Param("user") User user, 
            @Param("sessionId") String sessionId,
            @Param("conversationId") String conversationId);
    
    // Get conversations that need summarization (>16 messages)
    @Query("SELECT DISTINCT ch.conversationId FROM ChatHistory ch WHERE " +
           "(ch.user = :user OR ch.sessionId = :sessionId) AND ch.isSummarized = false " +
           "GROUP BY ch.conversationId HAVING COUNT(ch) > 16")
    List<String> findConversationsNeedingSummary(
            @Param("user") User user,
            @Param("sessionId") String sessionId);
    
    // Get all user conversations (for logged-in users)
    @Query("SELECT DISTINCT ch.conversationId FROM ChatHistory ch WHERE ch.user = :user " +
           "ORDER BY MAX(ch.createdAt) DESC")
    List<String> findUserConversations(@Param("user") User user);
}
