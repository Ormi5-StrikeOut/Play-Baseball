package org.example.spring.repository.message.custom;

import org.example.spring.domain.message.MessageMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMessageRoomRepository {
    Page<MessageMember> findQueryMessageRoom(
            Long memberId,
            Pageable pageable
    );
}
