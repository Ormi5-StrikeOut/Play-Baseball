package org.example.spring.domain.message;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.*;
import org.example.spring.audit.Auditable;
import org.example.spring.domain.Member;

@Entity
@Table(name = "messageMember")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageMemberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_room_id", nullable = false)
    private MessageRoom messageRoomId;
}
