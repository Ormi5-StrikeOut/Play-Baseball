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
    private Long messageRoomId;

    @Column(name = "last_message_at")
    private Timestamp lastMessageAt;

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createAt DESC")
    private Set<Message> messages = new LinkedHashSet<>();

    public Message getLastMessage() {
        return messages.stream().findFirst().orElse(null);
    }

    @PrePersist
    @PreUpdate
    private void updateLastMessageAt() {
        if (!messages.isEmpty()) {
            this.lastMessageAt = messages.stream()
                    .map(Message::getCreateAt)
                    .max(Timestamp::compareTo)
                    .orElse(null);
        }
    }
}
