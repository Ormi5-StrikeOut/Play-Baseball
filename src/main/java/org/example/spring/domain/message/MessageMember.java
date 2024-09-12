package org.example.spring.domain.message;

import jakarta.persistence.*;
import lombok.*;
import org.example.spring.audit.Auditable;
import org.example.spring.domain.member.Member;

@Entity
@Table(name = "message_member")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageMember extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_member_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_room_id", nullable = false)
    private MessageRoom messageRoom;
}
