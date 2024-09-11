package org.example.spring.domain.message;

import jakarta.persistence.*;
import lombok.*;
import org.example.spring.audit.Auditable;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "message_room")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageRoom extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_room_id", nullable = false)
    private Long id;

    @Column(name = "last_message_at")
    private Timestamp lastMessageAt;

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private Set<Message> messages = new LinkedHashSet<>();

    public Message getLastMessage() {
        return messages.stream().findFirst().orElse(null);
    }

    public void updateLastMessageAt() {
        Message lastMessage = getLastMessage();
        if (lastMessage != null) {
            this.lastMessageAt = lastMessage.getCreatedAt();
        } else {
            this.lastMessageAt = null;
        }
    }
}
