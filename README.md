# JPA

## 요구사항

### 3단계: 질문 삭제하기 리팩터링

결과: 질문, 답변 상태 → deleted

#### 삭제 조건

1. 질문자 == 로그인 사용자
   - 실패: 질문을 삭제할 권한이 없습니다.
2. 로그인 사용자 = 답변자 목록
   - 실패: 다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.
   - 성공: 질문, 답변 삭제 후 기록

#### 결과 기록

`DeleteHistory`에 질문, 답변 삭제 기록을 남긴다.

#### 도메인 역할

- 답변
  - 로그인 사용자 비교
  - 삭제 가능 여부 반환
  - 답변 삭제
  - 삭제 권한 에러: `CannotDeleteException`
- `Answers`
  - 삭제 가능 여부 반환
  - 답변 삭제
- 질문
  - 로그인 사용자 비교
  - 답변 목록 삭제 처리
    - 삭제한 답변 목록 반환
  - 질문 삭제 처리
  - 삭제 권한 에러: `CannotDeleteException`
- `QuestionRemover`
  - 질문, 답변 삭제
  - 삭제 기록 반환
  - 삭제 권한 에러: `CannotDeleteException`

#### 서비스 역할

1. Login User, Question ID 입력
2. 데이터베이스 Question ID 조회 → Question 반환
3. `QuestionRemover`에게 삭제 요청
   - 실패: `CannotDeleteException`
   - 성공: `List<DeleteHistory>` 반환
4. `List<DeleteHistory>` 저장

### 2단계: 연관 관계 매핑

- 1단계: 테이블 FK 기준
- 2단계: 자연스러운 호출 객체 형태로 변경한다.

#### 연관 관계

- Question 일:다 Answer 양방향
  - Question이 자신의 Answer 목록을 가져올 수 있다.
    - 삭제 여부에 따른 Answer 목록 반환
  - Answer 생성 과정에서 Question의 Answer 목록에도 추가되어야 한다.
- User(Writer) 일:다 Answer 단방향
- User(Writer) 일:다 Question 단방향
- User(Writer) 일:다 Delete History 단방향

#### 기능 추가

- Cascade
- Lazy loading
- `@Embeddable`, `@Embedded`
- `@Where`

### 1단계: 엔티티 매핑

- DDL에 맞춰 Entity 수정
- Repository 메소드에 맞춰 테스트 작성
- CustomException으로 예외 처리

## Data Definition Language

### User

```sql
create table user
(
  id         bigint generated by default as identity,
  created_at timestamp   not null,
  email      varchar(50),
  name       varchar(20) not null,
  password   varchar(20) not null,
  updated_at timestamp,
  user_id    varchar(20) not null,
  primary key (id)
)

alter table user
  add constraint UK_a3imlf41l37utmxiquukk8ajc unique (user_id)
```

### Question

```sql
create table question
(
  id         bigint generated by default as identity,
  contents   clob,
  created_at timestamp    not null,
  deleted    boolean      not null,
  title      varchar(100) not null,
  updated_at timestamp,
  writer_id  bigint,
  primary key (id)
)

alter table question
  add constraint fk_question_writer
    foreign key (writer_id)
      references user
```

### Answer

```sql
create table answer
(
  id          bigint generated by default as identity,
  contents    clob,
  created_at  timestamp not null,
  deleted     boolean   not null,
  question_id bigint,
  updated_at  timestamp,
  writer_id   bigint,
  primary key (id)
)

alter table answer
  add constraint fk_answer_to_question
    foreign key (question_id)
      references question

alter table answer
  add constraint fk_answer_writer
    foreign key (writer_id)
      references user
```

### Delete History

```sql
create table delete_history
(
  id            bigint generated by default as identity,
  content_id    bigint,
  content_type  varchar(255),
  create_date   timestamp,
  deleted_by_id bigint,
  primary key (id)
)

alter table delete_history
  add constraint fk_delete_history_to_user
    foreign key (deleted_by_id)
      references user
```
