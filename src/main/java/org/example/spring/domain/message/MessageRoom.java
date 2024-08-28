package org.example.spring.domain.message;

import jakarta.persistence.*;
import lombok.*;
import org.example.spring.audit.Auditable;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "messageRoom")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageRoom extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageRoomId;

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("messageId DESC")
    private Set<Message> messages = new LinkedHashSet<>();

    public Message getLastMessage() {
        return messages.stream().findFirst().orElse(null);
    }
}
