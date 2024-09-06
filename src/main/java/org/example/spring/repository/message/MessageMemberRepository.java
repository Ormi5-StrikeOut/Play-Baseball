package org.example.spring.repository.message;

import org.example.spring.domain.message.MessageMember;
import org.example.spring.repository.message.custom.CustomMessageRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMemberRepository extends JpaRepository<MessageMember, Long>, CustomMessageRoomRepository {

    @Query("SELECT mm FROM MessageMember mm WHERE mm.messageRoom.id = :messageRoomId AND mm.member.id = :memberId")
    List<MessageMember> findByMessageRoomIdAndMemberId(@Param("messageRoomId") Long messageRoomId, @Param("memberId") Long memberId);

    List<MessageMember> findByMessageRoomId(Long messageRoomId);

}
