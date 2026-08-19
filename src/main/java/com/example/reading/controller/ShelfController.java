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
@RequestMapping("/api/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private final ReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfResponse add(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ShelfRequest request
    ) {
        return readingService.addShelf(userId(jwt), request);
    }

    @GetMapping
    public PageResponse<ShelfResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return readingService.getShelf(userId(jwt), page, size);
    }

    @PatchMapping("/{shelfId}/status")
    public ShelfResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long shelfId,
            @Valid @RequestBody StatusRequest request
    ) {
        return readingService.changeStatus(
                userId(jwt),
                shelfId,
                request
        );
    }

    @DeleteMapping("/{shelfId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long shelfId
    ) {
        readingService.deleteShelf(userId(jwt), shelfId);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}