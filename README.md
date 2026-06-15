# pick-meal - AI식단 백엔드 프로젝트

### 가족 구성원의 건강 상태, 선호/비선호 재료, 알러지 정보를 기반으로 식단 후보를 조회하고 선택할 수 있는 식단 추천 서비스

## 1. 프로젝트 개요

- 가족 단위 식단 선택 서비스
- 메뉴/재료 데이터를 외부 공공 API에서 수집
- 카테고리, 요리 종류, 영양 정보 기반 메뉴 조회
- 향후 재료 가격 데이터를 연동하여 포인트로 메뉴를 선택할 수 있게 확장할 예정

## 2. 기술 스택

| 구분           | 기술                                |
|--------------|-----------------------------------|
| Backend      | Spring Boot, Spring Security, JPA |
| Database     | PostgreSQL                        |
| Cache/Auth   | Redis, JWT                        |
| Test         | JUnit5, Mockito, MockMvc          |
| Infra        | Docker, GitHub Actions            |
| External API | 공공데이터 레시피 API                     |

## 3. 주요 기능

### 회원 / 인증

- JWT 기반 로그인
- Refresh Token Redis 저장
- 로그아웃 시 Access Token blacklist 처리

## 주요 API 핵심 서비스 흐름

1. 사용자가 회원가입 및 로그인을 진행한다.
2. 사용자는 가족 그룹을 생성하거나 초대 코드를 통해 기존 가족에 합류한다.
3. 가족 구성원은 건강 정보와 선호/비선호 재료 정보를 등록한다.
4. 서비스는 외부 공공 API에서 수집한 메뉴 및 재료 데이터를 기반으로 메뉴 후보를 제공한다.
5. 사용자는 카테고리와 요리 종류를 기준으로 메뉴를 필터링한다.
6. 메뉴 상세 화면에서 칼로리, 탄수화물, 단백질, 지방, 나트륨 등의 영양 정보와 재료 정보를 확인한다.
7. 가족 구성원은 원하는 메뉴를 선택하고, 선택 결과를 기반으로 식단 후보를 구성한다.

상세 API 명세는 Notion 문서에 정리되어 있습니다.

## 4. 아키텍처 / 패키지 구조

```
kongju.pickmeal
├─ api
│  ├─ auth
│  ├─ family
│  ├─ menu
│  └─ user
│
├─ application
│  ├─ auth
│  ├─ family
│  ├─ menu
│  │  ├─ data
│  │  └─ importer
│  └─ user
│
├─ core
│  ├─ auth
│  ├─ diet
│  ├─ family
│  ├─ menu
│  ├─ service
│  └─ user
│
├─ infrastructure
│  ├─ external.recipe
│  ├─ repository
│  └─ config
│
└─ common
   ├─ ApiResponse
   ├─ config
   └─ exception
```

### 설계 의도

이 프로젝트는 `api`, `application`, `core`, `infrastructure` 계층을 분리하여 구성했습니다.

- `api`: HTTP 요청과 응답 처리
- `application`: 유스케이스 흐름 처리
- `core`: 도메인 모델과 repository interface 정의
- `infrastructure`: JPA, Redis, 외부 API 연동 구현

repository는 `core` 계층에 interface로 정의하고, `infrastructure` 계층에서 JPA adapter로 구현했습니다. 이를 통해 application 계층이 Spring Data JPA
구현체에 직접 의존하지 않도록 분리했습니다.

## 5. ERD / 핵심 도메인 구조

![ERD](./docs/erd.png)

### 핵심 도메인

- `User`: 서비스 사용자
- `Family`: 가족 그룹
- `FamilyMember`: 가족 구성원과 권한 관리
- `FamilyJoinRequest`: 가족 합류 신청
- `UserHealthProfile`: 사용자의 건강 정보
- `UserDisease`: 사용자의 질병 정보
- `UserIngredientPreference`: 선호/비선호/제한 재료 정보
- `Menu`: 외부 API 기반 메뉴 데이터
- `Ingredient`: 재료 마스터 데이터
- `MenuIngredient`: 메뉴와 재료의 연결 정보
- `Diet`: 가족 또는 사용자의 식단
- `DietItem`: 식단에 포함된 메뉴
- `UserMenuPick`: 가족 구성원의 메뉴 선택 정보

### 주요 관계

- 사용자는 하나의 가족에 소속될 수 있습니다.
- 가족 리더는 가족 합류 신청을 승인하거나 거절할 수 있습니다.
- 사용자는 건강 정보와 선호/비선호 재료 정보를 등록할 수 있습니다.
- 메뉴는 여러 재료와 연결됩니다.
- 가족 구성원은 메뉴 후보 중 원하는 메뉴를 선택할 수 있습니다.
- 식단은 여러 메뉴 항목으로 구성됩니다.

상세 테이블 구조와 컬럼 설명은 Notion 문서에 정리했습니다.

## 6. 대표 API

| 기능          | Method | Endpoint                                                     | 설명                |
|-------------|--------|--------------------------------------------------------------|-------------------|
| 회원가입        | POST   | `/api/v1/users/signup`                                       | 사용자 계정 생성         |
| 로그인         | POST   | `/api/v1/auth/login`                                         | JWT 발급            |
| 가족 생성       | POST   | `/api/v1/families`                                           | 가족 그룹 생성          |
| 가족 합류 신청    | POST   | `/api/v1/families/applications`                              | 초대 코드 기반 가족 합류 신청 |
| 가족 합류 승인/거절 | PATCH  | `/api/v1/families/me/applications/{requestId}`               | 리더가 가입 신청을 처리     |
| 질병 정보 수정    | PATCH  | `/api/v1/users/me/diseases`                                  | 질병 정보 수정          |
| 재료 선호 정보 수정 | PATCH  | `/api/v1/users/me/ingredient-preferences`                    | 선호/비선호 재료 수정      |
| 메뉴 필터 옵션 조회 | GET    | `/api/v1/menus/filter-options`                               | 카테고리/요리 종류 목록 조회  |
| 메뉴 목록 조회    | GET    | `/api/v1/menus?category=KOREAN&dishType=STEW&page=0&size=20` | 조건 기반 메뉴 페이징 조회   |
| 메뉴 상세 조회    | GET    | `/api/v1/menus/{menuId}`                                     | 메뉴 영양 정보 및 재료 조회  |
| 메뉴 선택       | POST   | `/api/v1//diets/candidates/{candidateId}`                    | 가족 구성원이 메뉴 선택     |

## 7. 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/사용자명/pickmeal.git
cd pickmeal
```

### 2. 환경 변수 설정

application-local.yml 또는 .env 파일에 아래 값을 설정합니다.

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pickmeal
    username: your_username
    password: your_password

jwt:
  secret: your_jwt_secret

external:
  recipe:
    public-data:
      api-key: your_public_data_api_key
    food-safety:
  api-key: your_food_safety_api_key
```

### 3. PostgreSQL / Redis 실행

Docker Compose를 사용하는 경우:

```
docker compose up -d
```

### 4. 애플리케이션 실행

```
bash gradlew bootRun --args='--spring.profiles.active=local'
```

### 5. 테스트 실행

```
bash gradlew test
```

## 외부 API 데이터 Import

공공데이터 기반 메뉴/재료 데이터 적재:

```
bash gradlew bootRun --args='--spring.profiles.active=local,public-data-import'

```

## 8. 테스트 방법

### 테스트 실행

```bash
bash gradlew test
```

| 도구                   | 사용 목적                        |
|----------------------|------------------------------|
| JUnit5               | 테스트 코드 작성 및 실행               |
| Mockito              | Service 테스트에서 의존 객체 Mock 처리  |
| MockMvc              | Controller 계층의 HTTP 요청/응답 검증 |
| Spring Security Test | 인증/인가가 필요한 API 테스트           |

### 테스트 범위

- 인증 API
    - 로그인, 로그아웃, 토큰 재발급 검증
- 가족 관리 API
    - 가족 생성
    - 가족 합류 신청
    - 합류 신청 승인/거절
    - 가족 구성원 조회
    - 가족 구성원 방출, 탈퇴, 해체 권한 검증
- 사용자 정보 API
    - 건강 정보 수정
    - 선호/비선호 재료 정보 수정
    - 메뉴 API
    - 메뉴 필터 옵션 조회
    - 카테고리/요리 종류 기반 메뉴 목록 조회
    - 메뉴 목록 페이징 응답 검증
    - 메뉴 상세 조회 및 영양 정보 응답 검증

Controller 테스트에서는 MockMvc를 사용해 HTTP 상태 코드와 JSON 응답 구조를 검증했습니다.
인증이 필요한 API는 @WithMockUser 또는 Spring Security Test의 mock user를 사용해 테스트했습니다.

## 9. 핵심 기술적 고민 3~5개 핵심 설계 및 트러블슈팅

### 1. Menu와 Diet 도메인 분리

초기에는 메뉴 관련 모델을 `diet` 패키지에 함께 두었지만, `Menu`는 사용자가 실제로 선택한 식단 기록이 아니라 식단 선택을 위한 후보 데이터에 가깝다고 판단했습니다.

따라서 `Menu`, `Ingredient`, `MenuIngredient`는 `menu` 도메인으로 분리하고, `Diet`는 사용자가 실제로 선택하거나 확정한 식단 기록을 담당하도록 분리했습니다.

이를 통해 메뉴 데이터 조회/필터링과 식단 선택/확정 로직의 책임을 분리했습니다.

### 2. Repository Port-Adapter 분리

초기에는 application 계층에서 Spring Data JPA Repository를 직접 의존했습니다.  
이 경우 도메인/유스케이스 로직이 JPA 구현체에 강하게 결합된다고 판단했습니다.

이를 개선하기 위해 `core` 계층에는 repository interface를 정의하고, `infrastructure` 계층에서 JPA adapter가 이를 구현하도록 분리했습니다.

이를 통해 application 계층은 JPA 구현체가 아니라 repository port에만 의존하도록 구성했습니다.

### 3. 외부 API 기반 메뉴 데이터 Import

메뉴와 재료 데이터는 직접 입력하지 않고, 공공 레시피 API와 식품안전나라 API를 기반으로 수집했습니다.

각 API는 응답 구조와 제공 데이터가 달라 import service와 client를 분리했습니다.  
또한 import 작업은 일반 애플리케이션 실행과 분리하기 위해 `public-data-import`, `food-safety-import` profile 기반 runner로 실행되도록 구성했습니다.

이를 통해 일반 서버 실행과 데이터 적재 작업이 동시에 수행되지 않도록 분리했습니다.

### 4. 비정형 재료 문자열 파싱

외부 API의 재료 정보는 구조화된 JSON이 아니라 `"된장 5g, 두부 20g"`과 같은 문자열 형태로 제공되었습니다.

이를 메뉴와 재료 테이블로 정규화하기 위해 재료 문자열을 줄바꿈, 쉼표, 수량 시작 위치 기준으로 파싱하는 parser를 구현했습니다.

수량 단위가 일정하지 않은 데이터가 많았기 때문에 `quantity`와 `unit`을 무리하게 분리하지 않고, 원문 수량을 `quantityText`로 보존했습니다.

### 5. 가족 리더 권한 처리

가족 가입 승인, 구성원 방출, 초대 코드 재발급, 가족 해체와 같은 기능은 가족 리더만 수행할 수 있도록 제한했습니다.

컨트롤러에서는 인증 사용자를 기준으로 요청을 받고, 서비스 계층에서 해당 사용자가 대상 가족의 리더인지 검증했습니다.

이를 통해 다른 가족의 데이터에 접근하거나, 권한 없는 사용자가 가족 관리 기능을 수행하지 못하도록 처리했습니다.

## 10. Notion 상세 문서 링크

https://app.notion.com/p/33cc5ec7e63d80ca8f9ef6a42faaf6dd?source=copy_link
