# 딥러닝 기반 영상분석을 통한 학습자 행동 및 감정 모니터링

## Team Different
- 박지수
- 심나영
- 장효정
- 하유진

## How to run
### 1. Server
```
$ pip install requirements.txt
$ python server.py
``` 
<br/> 
<br/> 

### 2. Application
안드로이드를 통해 편집 및 실행
* 안드로이드와 기기 버전 및 사양

| 이름 | 버전/사양 |
|---|---|
|안드로이드 스튜디오|4.0.1|
|카메라|최소 640x480px|
|안드로이드 버전|2GB 이상|
|모바일 기기 내 잔여 용량|2GB 이상| 
<br/> 
<br/> 

### 3. Web Site
 스프링 sts를 통해 설치 및 실행한다.

* 버전 

| 이름 | 버전 |
|---|---|
|이클립스 IDE|4.17.0|
|서버|아파치톰캣 8.5|
|스프링|4.3.6|
|mysql|8.0.21|
<br/>

* 실행방법 
1. Web 폴더를 클론하여 이클립스 IDE에 import한다. 
2. 서버를 설정 해 준 후 dashboard 프로젝트를 apache tomcat을 통해 실행하면 웹페이지가 열린다. 
3. 웹 페이지의 url은 localhost:8080/dashboard/board/search 이다. 해당 url을 통해 접속한다.
4. 프로그램 실행 시 다음과 같은 화면이 나타나야 한다.  
![image01](https://user-images.githubusercontent.com/48276691/102830412-9b51a100-442c-11eb-89f8-0fba7fd2ce9d.png)
![image02](https://user-images.githubusercontent.com/48276691/102830439-ac9aad80-442c-11eb-96fa-bee69acfd013.png)
![image03](https://user-images.githubusercontent.com/48276691/102830446-b02e3480-442c-11eb-9046-3d962be26793.png) 
<br/> 

- 주의사항 

반드시 데이터베이스에 정보가 들어있어야 실행이 가능하다. 
데이터베이스 관련 코드는 ***dao, dto, form, service, controller, mapper 클래스*** 및 ***demo폴더의 js파일***,***views 폴더 내의 jsp 파일*** 에서 수정 가능하다. 
