package com.example.reading.repository;

import com.example.reading.domain.ShelfItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfRepository extends JpaRepository<ShelfItem, Long> {

    boolean existsByUserIdAndWorkId(Long userId, Long workId);

    Page<ShelfItem> findByUserId(Long userId, Pageable pageable);
}