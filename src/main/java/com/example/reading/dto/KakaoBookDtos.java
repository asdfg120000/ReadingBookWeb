package com.example.reading.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public final class KakaoBookDtos {

    private KakaoBookDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(
            Meta meta,
            List<Book> documents
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("total_count")
            int totalCount,

            @JsonProperty("pageable_count")
            int pageableCount,

            @JsonProperty("is_end")
            boolean end
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Book(
            String title,
            List<String> authors,
            String publisher,
            String thumbnail,
            String isbn
    ) {
    }

    public record ImportRequest(
            @NotBlank
            @Pattern(
                    regexp = "^(?:[0-9]{13}|[0-9]{9}[0-9Xx])$",
                    message = "ISBN은 13자리 숫자 또는 10자리 형식이어야 합니다."
            )
            String isbn
    ) {
    }
}