package com.example.reading.dto;

import com.example.reading.domain.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record SignupRequest(
            @NotBlank
            @Email
            @Size(max = 254)
            String email,

            @NotBlank
            @Size(min = 8, max = 64)
            @Pattern(
                    regexp = "^[\\x21-\\x7E]+$",
                    message = "비밀번호는 공백 없이 영문, 숫자, 특수문자로 입력해주세요."
            )
            String password,

            @NotBlank
            @Size(max = 30)
            String nickname
    ) {
    }

    public record LoginRequest(
            @NotBlank
            @Email
            @Size(max = 254)
            String email,

            @NotBlank
            @Size(max = 64)
            String password
    ) {
    }

    public record UserResponse(
            Long id,
            String email,
            String nickname
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname()
            );
        }
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ) {
    }

    public record WorkRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 100) String author
    ) {
    }

    public record WorkResponse(
            Long id,
            String title,
            String author
    ) {
        public static WorkResponse from(Work work) {
            return new WorkResponse(
                    work.getId(),
                    work.getTitle(),
                    work.getAuthor()
            );
        }
    }

    public record ShelfRequest(
            @NotNull @Positive Long workId,
            @NotNull ReadingStatus status
    ) {
    }

    public record StatusRequest(
            @NotNull ReadingStatus status
    ) {
    }

    public record ShelfResponse(
            Long id,
            WorkResponse work,
            ReadingStatus status
    ) {
        public static ShelfResponse from(ShelfItem item) {
            return new ShelfResponse(
                    item.getId(),
                    WorkResponse.from(item.getWork()),
                    item.getStatus()
            );
        }
    }

    public record RecordCreateRequest(
            @NotNull @Positive Long workId,
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 10000) String content,
            @NotNull Boolean isPublic
    ) {
    }

    public record RecordUpdateRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 10000) String content
    ) {
    }

    public record VisibilityRequest(
            @NotNull Boolean isPublic
    ) {
    }

    public record RecordResponse(
            Long id,
            WorkResponse work,
            String nickname,
            String title,
            String content,
            boolean isPublic,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static RecordResponse from(ReadingRecord record) {
            return new RecordResponse(
                    record.getId(),
                    WorkResponse.from(record.getWork()),
                    record.getUser().getNickname(),
                    record.getTitle(),
                    record.getContent(),
                    record.isPublicVisible(),
                    record.getCreatedAt(),
                    record.getUpdatedAt()
            );
        }
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static <T> PageResponse<T> from(
                org.springframework.data.domain.Page<T> result
        ) {
            return new PageResponse<>(
                    result.getContent(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            );
        }
    }
}