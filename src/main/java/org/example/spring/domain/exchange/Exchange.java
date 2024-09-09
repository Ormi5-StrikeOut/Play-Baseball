package org.example.spring.domain.exchange;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.Review;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Exchange {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "exchange_id", nullable = false)
  private Long id;

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  @Column(name = "price", nullable = false)
  private int price;

  @Column(name = "regular_price", nullable = false)
  private int regularPrice;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "view_count", nullable = false)
  private int viewCount;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private SalesStatus status;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private Timestamp createdAt;

  @Column(name = "updated_at")
  @UpdateTimestamp
  private Timestamp updatedAt;

  @Column(name = "deleted_at")
  private Timestamp deletedAt;

  @OneToOne(mappedBy = "exchange", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private Review review;

  @OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ExchangeImage> images = new ArrayList<>();

  @Column(name = "reviewed_at")
  private Timestamp reviewedAt;

  public void markAsReviewed() {
    this.reviewedAt = new Timestamp(System.currentTimeMillis());
  }

  public void addImage(ExchangeImage image) {
    if (images == null) {
      images = new ArrayList<>();
    }
    images.add(image);
    image.associateExchange(this);
  }

  public void removeImage(ExchangeImage image) {
    images.remove(image);
    image.disassociateExchange();
  }

  public ExchangeBuilder toBuilder() {
    return Exchange.builder()
        .id(this.id)
        .member(this.member)
        .title(this.title)
        .price(this.price)
        .regularPrice(this.regularPrice)
        .content(this.content)
        .viewCount(this.viewCount)
        .status(this.status)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .deletedAt(this.deletedAt)
        .review(this.review)
        .images(this.images);
  }
}
