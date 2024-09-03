package org.example.spring.repository.message;

import org.example.spring.domain.message.MessageMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMemberRepository extends JpaRepository<MessageMember, Long> {

    List<MessageMember> findByMessageRoomIdAndMemberId(Long messageRoomId, Long memberId);

    List<MessageMember> findByMessageRoomId(Long messageRoomId);

    Page<MessageMember> findQueryMessageRoom(Long memberId, Pageable pageable);
}
