## 빌드 명령어

### 프론트엔드

* npm --prefix front-end run dev
* npm --prefix front-end run build
* npm --prefix front-end run lint



### 백엔드

* back-end/java-spring/gradlew.bat -p back-end/java-spring build
* back-end/java-spring/gradlew.bat -p back-end/java-spring bootRun
* back-end/java-spring/gradlew.bat -p back-end/java-spring test



## 규칙

* Critical - 코드 파일 하나의 코드가 300줄이 넘어가면 분리 검토, 500줄이 넘어가면 무조건 분리. 두 케이스 모두 사용자에게 보고.
* Critical - 테스트의 생성 및 수정, 삭제는 에이전트 단독 판단이 아닌 사용자의 명시적인 승인을 얻은 후에 진행.
* 메인 에이전트는 오케스트레이션 역할만 담당하며 각 서브 에이전트에 전달할 내용만을 Input / Output함.
* 전체 파이프라인 흐름 및 메인 책임은 .claude/agents/PIPELINE.md 참조 필수.
* 메인은 git commit 완료 직후 `touch .claude/.review_pending` 실행 (REJECTED 재작업 후 commit 시도 동일). 단, `/pipeline-start` 누적 모드에서는 마커 미생성 · `.claude/.review_skip` sentinel 유지. `/pipeline-review` 또는 `/pipeline`에서만 sentinel 제거 후 마커 생성.
* 백엔드/프론트엔드 코드는 헥사고널(Ports & Adapters) 아키텍처 적용. 인프라·스크립트·마이그레이션 등 비도메인 파일은 아키텍처 규칙 적용 대상 외.



## 서브 에이전트

* Planner - 사용자에게 받은 작업 지시를 개발이 가능한 기획으로 정리 및 승인 요청. 이후 Coder에게 흐름을 넘긴다.
* Coder - 백엔드 및 프론트엔드 모두 DDD에 입각한 코드 개발 및 테스트 코드 시범 동작.
* Reviewer - 서브 에이전트는 아니지만 격리된 컨텍스트를 가진 외부 메인 에이전트. Stop 훅은 메인 응답 종료 시마다 발화하며 `.review_pending` 마커가 있을 때만 Reviewer가 실행됨 (메인이 git commit 후 `touch .claude/.review_pending` 실행 시 다음 Stop에서 트리거). 2단계(Sonnet 1차 → ESCALATE 시 Opus 2차) 리뷰. REJECTED 시 메인 재개하여 Coder 재호출 후 재검토, APPROVED 시 메인 재개하여 QA 호출.
* QA - 코드 오류 검수 및 테스트 코드 최종 동작
* Mediator - 병렬 Coder의 worktree 머지 시 충돌 파일 내용 해결. 충돌 발생 시에만 호출.



## 서브 에이전트 파일 경로

* 파이프라인 정의: .claude/agents/PIPELINE.md
* Planner: .claude/agents/Planner.md
* Coder: .claude/agents/Coder.md
* Mediator: .claude/agents/Mediator.md
* QA: .claude/agents/QA.md



## 디렉토리 구조

'front-end/' 프론트엔드 'back-end/' 자바 백엔드 'schema/' DDL 스크립트 보관소 'docs/' PRD 및 중요 문서 보관소
