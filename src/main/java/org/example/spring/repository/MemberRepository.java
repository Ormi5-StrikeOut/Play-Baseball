package org.example.spring.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.example.spring.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 회원 엔티티의 CRUD 작업을 담당하는 리포지토리 인터페이스입니다.
 * 회원 정보 저장, 조회, 변경, 삭제 등의 기능을 제공합니다.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);

    List<Member> findByDeletedAtBeforeAndDeletedAtIsNotNull(Timestamp deletedAt);
}