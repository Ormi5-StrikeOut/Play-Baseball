package org.example.spring.domain.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.Member;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @JoinColumn(name = "exchange_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Exchange post;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @Column(length = 300, nullable = false)
    private String content;

    @Column(nullable = false)
    private int rate;

    @Column(nullable = false)
    private boolean is_secret;

    @Column(nullable = false)
    private ZonedDateTime created_at;

    @Column
    private ZonedDateTime updated_at;

    @Column
    private ZonedDateTime deleted_at;
}