package com.example.reading.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "works",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_work_title_author",
                columnNames = {"title", "author"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    public Work(String title, String author) {
        this.title = title;
        this.author = author;
    }
}