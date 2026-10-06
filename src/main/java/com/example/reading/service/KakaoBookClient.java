package com.example.reading.service;

import com.example.reading.dto.KakaoBookDtos.*;
import com.example.reading.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;

@Service
public class KakaoBookClient {

    private final RestClient restClient;
    private final String apiKey;

    public KakaoBookClient(
            @Value("${kakao.rest-api-key:}") String apiKey
    ) {
        this.apiKey = apiKey.strip();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .requestFactory(requestFactory)
                .build();
    }

    public SearchResponse search(String query, int page) {
        String keyword = query.strip();

        if (keyword.isBlank() || keyword.length() > 200) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "검색어는 1~200자로 입력해주세요."
            );
        }

        if (page < 1 || page > 50) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "페이지는 1~50이어야 합니다."
            );
        }

        return request(keyword, page, null);
    }

    public Book findByIsbn(String isbn) {
        String normalized = isbn.toUpperCase(Locale.ROOT);
        SearchResponse response = request(normalized, 1, "isbn");

        return response.documents().stream()
                .filter(book -> matchesIsbn(book.isbn(), normalized))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "해당 ISBN의 책을 카카오에서 찾을 수 없습니다."
                ));
    }

    private SearchResponse request(
            String query,
            int page,
            String target
    ) {
        if (apiKey.isBlank()) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "KAKAO_REST_API_KEY 환경변수를 설정하고 서버를 다시 실행해주세요."
            );
        }

        try {
            SearchResponse response = restClient.get()
                    .uri(builder -> {
                        builder.path("/v3/search/book")
                                .queryParam("query", "{query}")
                                .queryParam("page", page)
                                .queryParam("size", 10)
                                .queryParam("sort", "accuracy");

                        if (target != null) {
                            builder.queryParam("target", target);
                        }

                        return builder.build(query);
                    })
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(SearchResponse.class);

            if (response == null
                    || response.meta() == null
                    || response.documents() == null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "카카오 검색 응답을 확인할 수 없습니다."
                );
            }

            return response;

        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();

            if (status == 401 || status == 403) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "카카오 API 인증에 실패했습니다. REST API 키와 호출 허용 설정을 확인해주세요."
                );
            }

            if (status == 429) {
                throw new ApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "카카오 API 호출 한도에 도달했습니다. 잠시 후 다시 시도해주세요."
                );
            }

            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "카카오 도서 검색 요청에 실패했습니다."
            );

        } catch (ResourceAccessException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "카카오 서버 연결이 지연됩니다. 잠시 후 다시 시도해주세요."
            );

        } catch (RestClientException e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "카카오 응답을 처리하지 못했습니다."
            );
        }
    }

    private boolean matchesIsbn(String value, String requested) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return Arrays.stream(value.strip().split("\\s+"))
                .anyMatch(part -> part.equalsIgnoreCase(requested));
    }
}