package fsa.project.online_shop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null for anonymous users
    
    @Column(name = "session_id", length = 100)
    private String sessionId; // for anonymous users
    
    @Column(name = "conversation_id", length = 100, nullable = false)
    private String conversationId; // unique per conversation
    
    @Column(name = "message_type", length = 10, nullable = false)
    private String messageType; // "user" or "bot"
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "page_type", length = 50)
    private String pageType;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_summarized", nullable = false)
    private Boolean isSummarized = false;
    
    // Constructor for user messages
    public ChatHistory(User user, String sessionId, String conversationId, 
                      String messageType, String message, String pageType) {
        this.user = user;
        this.sessionId = sessionId;
        this.conversationId = conversationId;
        this.messageType = messageType;
        this.message = message;
        this.pageType = pageType;
        this.isSummarized = false;
    }
}
