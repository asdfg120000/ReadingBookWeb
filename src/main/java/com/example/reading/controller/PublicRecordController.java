package com.example.reading.controller;

import com.example.reading.dto.ApiDtos.*;
import com.example.reading.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/records")
@RequiredArgsConstructor
public class PublicRecordController {

    private final ReadingService readingService;

    @GetMapping
    public PageResponse<RecordResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return readingService.getPublicRecords(page, size);
    }

    @GetMapping("/{recordId}")
    public RecordResponse get(@PathVariable Long recordId) {
        return readingService.getPublicRecord(recordId);
    }
}