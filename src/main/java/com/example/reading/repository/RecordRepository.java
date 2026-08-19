package com.example.reading.repository;

import com.example.reading.domain.ReadingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<ReadingRecord, Long> {

    Page<ReadingRecord> findByUserId(Long userId, Pageable pageable);

    Page<ReadingRecord> findByPublicVisibleTrue(Pageable pageable);
}