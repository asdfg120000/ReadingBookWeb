독서 기록 서비스 API 명세


공통 사항

개발 환경 기본 주소: http://localhost:8080
요청 및 응답 형식: JSON

요청 본문이 있는 경우 헤더 설정 필요.
Content-Type: application/json

인증이 필요한 경우 로그인으로 발급받은 토큰 전달.
Authorization: Bearer {accessToken}

일반 목록 응답 형식
{
"content": [],
"totalElements": 0,
"totalPages": 0
}

content: 현재 페이지의 조회 결과
totalElements: 전체 항목 수
totalPages: 전체 페이지 수

1. 회원가입과 로그인

1.1 회원가입

POST /api/auth/signup
인증: 불필요
기능: 이메일, 비밀번호, 닉네임을 받아 회원 등록

요청 예시
{
"email": "reader@example.com",
"password": "Reading123!",
"nickname": "독서하는사람"
}

email: 로그인에 사용할 이메일
password: 비밀번호
nickname: 서비스에서 사용할 닉네임

중복 이메일이나 잘못된 입력값은 사용 불가
성공 시 회원 정보 반환 

1.2 로그인

POST /api/auth/login
인증: 불필요
기능: 이메일과 비밀번호 확인 후 JWT 액세스 토큰 발급

요청 예시
{
"email": "reader@example.com",
"password": "Reading123!"
}

응답 예시
{
"accessToken": "발급된 JWT",
"tokenType": "Bearer",
"expiresIn": 3600
}

accessToken: 인증 요청에 사용할 토큰
tokenType: 토큰 전달 방식
expiresIn: 토큰 유효기간. 예시 값이며 실제 값은 서버 설정에 따라 결정

이후 인증 요청의 Authorization 헤더에 accessToken 전달.
현재 리프레시 토큰 발급 및 토큰 갱신 API는 미포함.

2. 작품 관리

작품은 여러 사용자가 함께 사용하는 공용 데이터.
작품 등록과 개인 서재 등록은 별도 처리.

2.1 작품 등록

POST /api/works
인증: 필요
기능: 제목과 작가를 직접 입력하여 작품 등록

요청 예시
{
"title": "어린 왕자",
"author": "생텍쥐페리"
}

응답 예시
{
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
}

제목과 작가 기준으로 중복 확인.
같은 작품이 있으면 중복 등록 오류 처리.
작품 등록 후 반환된 id로 서재 등록 API 호출 가능.

2.2 작품 목록 및 검색

GET /api/works
인증: 필요
기능: 내부 DB에 등록된 작품 조회 및 검색

요청 예시
GET /api/works?keyword=어린&page=0&size=20

keyword: 작품 검색어
page: 페이지 번호
size: 페이지당 조회 개수

응답 예시
{
"content": [
{
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
}
],
"totalElements": 1,
"totalPages": 1
}

내부 DB 검색만 수행.
카카오 도서 검색은 별도 API 사용.

3. 카카오 도서 검색

3.1 외부 도서 검색

GET /api/kakao/books
인증: 필요
기능: 카카오 도서 검색 API를 이용한 책 검색

요청 예시
GET /api/kakao/books?query=어린왕자&page=1

query: 도서 검색어
page: 검색 결과 페이지 번호. 1부터 시작

응답 예시
{
"meta": {
"totalCount": 100,
"pageableCount": 100,
"end": false
},
"documents": [
{
"title": "어린 왕자",
"authors": ["생텍쥐페리"],
"publisher": "출판사",
"thumbnail": "https://example.com/book.jpg",
"isbn": "도서 ISBN"
}
]
}

totalCount: 전체 검색 결과 수
pageableCount: 조회 가능한 결과 수
end: 마지막 페이지 여부
documents: 검색된 도서 목록



3.2 검색한 책 등록

POST /api/kakao/books/import
인증: 필요
기능: 선택한 책을 ISBN으로 조회하여 작품 등록

요청 예시
{
"isbn": "선택한 도서의 ISBN"
}

응답 예시
{
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
}

등록된 작품이면 기존 작품 정보 반환
새 작품이면 저장 후 작품 정보 반환
현재 작품 저장 항목은 제목과 작가. 표지와 출판사 정보는 저장 항목에서 제외 (현재)

4. 내 서재

사용자별 작품 등록 및 독서 상태 관리

독서 상태
READING: 읽는 중
COMPLETED: 완독
PAUSED: 잠시 중단
DROPPED: 하차

6.1 서재에 작품 추가

POST /api/shelf
인증: 필요
기능: 작품을 본인의 서재에 추가

요청 예시
{
"workId": 1,
"status": "READING"
}

응답 예시
{
"id": 10,
"work": {
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
},
"status": "READING"
}

id: 서재 항목 ID
work.id: 작품 ID
같은 사용자의 동일 작품 중복 등록 제한

4.2 내 서재 목록 조회

GET /api/shelf
인증: 필요
기능: 본인의 서재 목록 조회

요청 예시
GET /api/shelf?page=0&size=20

응답 예시
{
"content": [
{
"id": 10,
"work": {
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
},
"status": "READING"
}
],
"totalElements": 1,
"totalPages": 1
}


4.3 독서 상태 변경

PATCH /api/shelf/{id}/status
인증: 필요
기능: 본인 서재 항목의 독서 상태 변경

요청 예시
PATCH /api/shelf/10/status

{
"status": "COMPLETED"
}


4.4 서재에서 제거

DELETE /api/shelf/{id}
인증: 필요
기능: 본인의 서재에서 작품 제거

요청 예시
DELETE /api/shelf/10

성공 응답: 204 No Content
응답 본문: 없음


5. 독서기록

작품에 대한 개인 독서기록 작성 및 관리

5.1 독서기록 작성

POST /api/records
인증: 필요
기능: 작품에 대한 독서기록 작성

요청 예시
{
"workId": 1,
"title": "다시 읽은 어린 왕자",
"content": "수정",
"isPublic": false
}

workId: 작품 ID
title: 독서기록 제목
content: 독서기록 내용
isPublic: 공개 여부. true는 공개, false는 비공개

독서기록 응답 예시
{
"id": 100,
"work": {
"id": 1,
"title": "어린 왕자",
"author": "생텍쥐페리"
},
"nickname": "독서하는사람",
"title": "다시 읽은 어린 왕자",
"content": "처음 읽었을 때와 다르게 느껴진 부분을 기록했다.",
"isPublic": false,
"createdAt": "2026-10-07T01:00:00",
"updatedAt": "2026-10-07T01:00:00"
}

5.2 내 독서기록 목록 조회

GET /api/records
인증: 필요
기능: 본인이 작성한 독서기록 목록 조회

요청 예시
GET /api/records?page=0&size=20


5.3 내 독서기록 상세 조회

GET /api/records/{id}
인증: 필요
기능: 본인이 작성한 독서기록 한 건 조회

요청 예시
GET /api/records/100

독서기록 응답 형식 사용.
작성자 권한 확인 후 조회.

5.4 독서기록 수정

PUT /api/records/{id}
인증: 필요
기능: 본인 독서기록의 제목과 내용 수정

요청 예시
PUT /api/records/100

{
"title": "수정한 독서기록 제목",
"content": "수정한 독서기록 내용"
}


5.5 공개 여부 변경

PATCH /api/records/{id}/visibility
인증: 필요
기능: 본인 독서기록의 공개 여부 변경

요청 예시
PATCH /api/records/100/visibility

{
"isPublic": true
}

공개 설정 시 공개 조회 대상에 포함

5.6 독서기록 삭제

DELETE /api/records/{id}
인증: 필요
기능: 본인이 작성한 독서기록 삭제

요청 예시
DELETE /api/records/100


6. 공개 독서기록

6.1 공개 기록 목록 조회

GET /api/public/records
인증: 불필요
기능: 공개된 독서기록 목록 조회

요청 예시
GET /api/public/records?page=0&size=20


6.2 공개 기록 상세 조회

GET /api/public/records/{id}
인증: 불필요
기능: 공개된 독서기록 한 건 조회

요청 예시
GET /api/public/records/100



!!! 예외 처리

주요 오류 처리 기준

400 Bad Request: 필수 항목 누락 또는 잘못된 입력값
401 Unauthorized: 로그인 실패, 토큰 누락 또는 만료
403 Forbidden: 다른 사용자의 데이터 수정 등 권한 없는 요청
404 Not Found: 존재하지 않는 작품, 서재 항목 또는 독서기록
409 Conflict: 이메일, 작품 또는 서재 등록 중복
500 Internal Server Error: 서버 내부 오류

