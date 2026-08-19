package com.example.reading.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "shelf_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shelf_user_work",
                columnNames = {"user_id", "work_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShelfItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingStatus status;

    public ShelfItem(User user, Work work, ReadingStatus status) {
        this.user = user;
        this.work = work;
        this.status = status;
    }

    public void changeStatus(ReadingStatus status) {
        this.status = status;
    }
}