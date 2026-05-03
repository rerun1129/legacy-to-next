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

* Critical - 코드 파일 하나의 코드가 300줄이 넘어가면 분리 검토, 500줄이 넘어가면 무조건 분리. 두 케이스 모두 사용자에게 보고. (테스트 코드 클래스 예외)
* Critical - 테스트의 수정 및 삭제는 에이전트 단독 판단이 아닌 사용자의 명시적인 승인을 얻은 후에 진행. 신규 작성은 Backend/Frontend-coder 재량.
* 메인 에이전트는 오케스트레이션 역할만 담당하며 각 서브 에이전트에 전달할 내용만을 Input / Output함.
* 전체 파이프라인 흐름 및 메인 책임은 .claude/agents/PIPELINE.md 참조 필수.

* Plan Mode에서 plan 출력 시 10~20줄 사이로 간결하게 요약해서 출력할것. 사용자 요청이 있으면 상세 설명 첨부 가능. 단, 20줄 이내로 요약이 불가능한 경우에는 사용자에게 상세 설명 첨부 요청 가능.


## 디렉토리 구조

'front-end/' 프론트엔드 'back-end/' 자바 백엔드 'schema/' DDL 스크립트 보관소 'docs/' PRD 및 중요 문서 보관소
