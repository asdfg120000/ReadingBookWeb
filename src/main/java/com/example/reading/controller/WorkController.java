package com.example.reading.controller;

import com.example.reading.dto.ApiDtos.*;
import com.example.reading.service.ReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final ReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkResponse create(
            @Valid @RequestBody WorkRequest request
    ) {
        return readingService.createWork(request);
    }

    @GetMapping
    public PageResponse<WorkResponse> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return readingService.searchWorks(keyword, page, size);
    }

    @GetMapping("/{workId}")
    public WorkResponse get(@PathVariable Long workId) {
        return readingService.getWork(workId);
    }
}