package com.example.reading.controller;

import com.example.reading.dto.ApiDtos.WorkResponse;
import com.example.reading.dto.KakaoBookDtos.*;
import com.example.reading.service.KakaoBookClient;
import com.example.reading.service.KakaoBookImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kakao/books")
@RequiredArgsConstructor
public class KakaoBookController {

    private final KakaoBookClient kakaoBookClient;
    private final KakaoBookImportService kakaoBookImportService;

    @GetMapping
    public SearchResponse search(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return kakaoBookClient.search(query, page);
    }

    @PostMapping("/import")
    public WorkResponse importBook(
            @Valid @RequestBody ImportRequest request
    ) {
        Book book = kakaoBookClient.findByIsbn(request.isbn());

        return kakaoBookImportService.importBook(book);
    }
}