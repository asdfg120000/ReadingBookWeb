package com.example.reading.controller;

import com.example.reading.dto.ApiDtos.*;
import com.example.reading.service.ReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final ReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RecordCreateRequest request
    ) {
        return readingService.createRecord(userId(jwt), request);
    }

    @GetMapping
    public PageResponse<RecordResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return readingService.getMyRecords(userId(jwt), page, size);
    }

    @GetMapping("/{recordId}")
    public RecordResponse get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId
    ) {
        return readingService.getMyRecord(userId(jwt), recordId);
    }

    @PutMapping("/{recordId}")
    public RecordResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId,
            @Valid @RequestBody RecordUpdateRequest request
    ) {
        return readingService.updateRecord(
                userId(jwt),
                recordId,
                request
        );
    }

    @PatchMapping("/{recordId}/visibility")
    public RecordResponse changeVisibility(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId,
            @Valid @RequestBody VisibilityRequest request
    ) {
        return readingService.changeVisibility(
                userId(jwt),
                recordId,
                request
        );
    }

    @DeleteMapping("/{recordId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long recordId
    ) {
        readingService.deleteRecord(userId(jwt), recordId);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}