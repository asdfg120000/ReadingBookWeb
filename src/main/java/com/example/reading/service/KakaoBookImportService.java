package com.example.reading.service;

import com.example.reading.domain.Work;
import com.example.reading.dto.ApiDtos.WorkResponse;
import com.example.reading.dto.KakaoBookDtos.Book;
import com.example.reading.exception.ApiException;
import com.example.reading.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KakaoBookImportService {

    private final WorkRepository workRepository;

    @Transactional
    public WorkResponse importBook(Book book) {
        String title = book.title() == null
                ? ""
                : book.title().strip();

        List<String> authors = book.authors() == null
                ? List.of()
                : book.authors();

        String author = String.join(
                ", ",
                authors.stream()
                        .filter(Objects::nonNull)
                        .map(String::strip)
                        .filter(value -> !value.isBlank())
                        .toList()
        );

        //작ㄱ가가 없을 경우
        if (author.isBlank()) {
            author = "작가 미상";
        }

        if (title.isBlank()) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "검색된 책에 제목 정보가 없습니다."
            );
        }

        //
        if (title.length() > 200 || author.length() > 100) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "현재 작품 저장 길이를 초과한 책입니다. 직접 등록 기능을 이용해주세요."
            );
        }

        Work exampleWork = new Work(title, author);

        Work existing = workRepository
                .findOne(Example.of(exampleWork))
                .orElse(null);

        if (existing != null) {
            return WorkResponse.from(existing);
        }

        Work saved = workRepository.saveAndFlush(exampleWork);

        return WorkResponse.from(saved);
    }
}