package com.example.reading.service;

import com.example.reading.domain.*;
import com.example.reading.dto.ApiDtos.*;
import com.example.reading.exception.ApiException;
import com.example.reading.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final ShelfRepository shelfRepository;
    private final RecordRepository recordRepository;

    // ---------- 작품 ----------

    @Transactional
    public WorkResponse createWork(WorkRequest request) {
        String title = request.title().strip();
        String author = request.author().strip();

        if (workRepository.existsByTitleAndAuthor(title, author)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "같은 제목과 작가의 작품이 이미 등록되어 있습니다."
            );
        }

        Work work = workRepository.save(new Work(title, author));

        return WorkResponse.from(work);
    }

    public PageResponse<WorkResponse> searchWorks(
            String keyword,
            int page,
            int size
    ) {
        String search = keyword.strip();

        if (search.length() > 200) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "검색어는 200자 이하여야 합니다."
            );
        }

        Page<WorkResponse> result = workRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                        search,
                        search,
                        pageable(page, size)
                )
                .map(WorkResponse::from);

        return PageResponse.from(result);
    }

    public WorkResponse getWork(Long workId) {
        return WorkResponse.from(findWork(workId));
    }

    // ---------- 내 서재 ----------

    @Transactional
    public ShelfResponse addShelf(Long userId, ShelfRequest request) {
        User user = findUser(userId);
        Work work = findWork(request.workId());

        if (shelfRepository.existsByUserIdAndWorkId(
                userId,
                work.getId()
        )) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "이미 내 서재에 등록한 작품입니다."
            );
        }

        ShelfItem item = shelfRepository.save(
                new ShelfItem(user, work, request.status())
        );

        return ShelfResponse.from(item);
    }

    public PageResponse<ShelfResponse> getShelf(
            Long userId,
            int page,
            int size
    ) {
        Page<ShelfResponse> result = shelfRepository
                .findByUserId(userId, pageable(page, size))
                .map(ShelfResponse::from);

        return PageResponse.from(result);
    }

    @Transactional
    public ShelfResponse changeStatus(
            Long userId,
            Long shelfId,
            StatusRequest request
    ) {
        ShelfItem item = ownedShelf(userId, shelfId);
        item.changeStatus(request.status());

        return ShelfResponse.from(item);
    }

    @Transactional
    public void deleteShelf(Long userId, Long shelfId) {
        ShelfItem item = ownedShelf(userId, shelfId);
        shelfRepository.delete(item);
    }

    // ---------- 내 독서기록 ----------

    @Transactional
    public RecordResponse createRecord(
            Long userId,
            RecordCreateRequest request
    ) {
        User user = findUser(userId);
        Work work = findWork(request.workId());

        if (!shelfRepository.existsByUserIdAndWorkId(
                userId,
                work.getId()
        )) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "먼저 작품을 내 서재에 등록해주세요."
            );
        }

        ReadingRecord record = new ReadingRecord(
                user,
                work,
                request.title().strip(),
                request.content(),
                request.isPublic()
        );

        return RecordResponse.from(
                recordRepository.saveAndFlush(record)
        );
    }

    public PageResponse<RecordResponse> getMyRecords(
            Long userId,
            int page,
            int size
    ) {
        Page<RecordResponse> result = recordRepository
                .findByUserId(userId, pageable(page, size))
                .map(RecordResponse::from);

        return PageResponse.from(result);
    }

    public RecordResponse getMyRecord(Long userId, Long recordId) {
        return RecordResponse.from(ownedRecord(userId, recordId));
    }

    @Transactional
    public RecordResponse updateRecord(
            Long userId,
            Long recordId,
            RecordUpdateRequest request
    ) {
        ReadingRecord record = ownedRecord(userId, recordId);

        record.update(
                request.title().strip(),
                request.content()
        );

        recordRepository.flush();

        return RecordResponse.from(record);
    }

    @Transactional
    public RecordResponse changeVisibility(
            Long userId,
            Long recordId,
            VisibilityRequest request
    ) {
        ReadingRecord record = ownedRecord(userId, recordId);
        record.changeVisibility(request.isPublic());

        recordRepository.flush();

        return RecordResponse.from(record);
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        ReadingRecord record = ownedRecord(userId, recordId);
        recordRepository.delete(record);
    }

    // ---------- 공개 독서기록 ----------

    public PageResponse<RecordResponse> getPublicRecords(
            int page,
            int size
    ) {
        Page<RecordResponse> result = recordRepository
                .findByPublicVisibleTrue(pageable(page, size))
                .map(RecordResponse::from);

        return PageResponse.from(result);
    }

    public RecordResponse getPublicRecord(Long recordId) {
        ReadingRecord record = findRecord(recordId);

        if (!record.isPublicVisible()) {
            throw notFound("독서기록을 찾을 수 없습니다.");
        }

        return RecordResponse.from(record);
    }

    // ---------- 공통 조회 및 권한 검사 ----------

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.UNAUTHORIZED,
                                "회원 정보를 확인할 수 없습니다."
                        )
                );
    }

    private Work findWork(Long workId) {
        return workRepository.findById(workId)
                .orElseThrow(() ->
                        notFound("작품을 찾을 수 없습니다.")
                );
    }

    private ReadingRecord findRecord(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() ->
                        notFound("독서기록을 찾을 수 없습니다.")
                );
    }

    private ShelfItem ownedShelf(Long userId, Long shelfId) {
        ShelfItem item = shelfRepository.findById(shelfId)
                .orElseThrow(() ->
                        notFound("서재 항목을 찾을 수 없습니다.")
                );

        if (!item.getUser().getId().equals(userId)) {
            throw notFound("서재 항목을 찾을 수 없습니다.");
        }

        return item;
    }

    private ReadingRecord ownedRecord(Long userId, Long recordId) {
        ReadingRecord record = findRecord(recordId);

        if (!record.getUser().getId().equals(userId)) {
            throw notFound("독서기록을 찾을 수 없습니다.");
        }

        return record;
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "page는 0 이상, size는 1~50이어야 합니다."
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    private ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }
}