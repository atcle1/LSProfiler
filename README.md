# LSProfiler @ 2015
 - 스마트폰 알림 최적화 연구 중 안드로이드 사용자 사용 정보수집 및 제어 앱입니다.
 - 전체 도구는 스마트폰{'수집 및 제어 앱(LSProfiler)', '수정 프레임워크', '커널 수집모듈', '커널 제어 데몬'}, 스마트워치{'수집 앱', '커널 수집모듈', '커널 제어 데몬'} 으로 구성됩니다.

## 기능
- 안드로이드 사용정보 수집 및 전송
    - 안드로이드 사용자 영역 및 프레임워크 정보 포함
- 커널 정보 수집 데몬 제어 및 수집
- 스마트워치 수집 도구 제어 및 수집

#### 안드로이드 수집정보
 - 통화 수/발신, 문자수신
 - 알림 생성
 - 배터리 레벨 및 상태
 - 패키지 인스톨/삭제
 - dumpsys (alarm, etc.)
#### 프레임워크 수집정보
 - 포그라운드 앱
 - 알림 삭제 및 이유
 - 상태바 열림/닫힘
 - 블루투스 트래픽 정보
#### 커널 수집정보
 - Suspend/wakeup 세션, wakeup reason
 - 세션 별 CPU 통계
 - touch event
 - 세션별 Top 실행 프로세스 정보
 - 스크린 on/off/밝기
 - Wi-fi traffic

## 관련 링크
 - 알림 전달 최적화를 통한 스마트워치 에너지 소모 감소 기법, 한국정보과학회, 2016.6
    - http://www.dbpia.co.kr/journal/articleDetail?nodeId=NODE07017528
 - 스마트워치의 알림 분석 및 전달 최적화, 학위논문, 2016.8
    - http://www.riss.kr/link?id=T14226809
    


