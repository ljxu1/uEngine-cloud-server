package org.uengine.cloud.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.uengine.cloud.tenant.TenantContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by uengine on 2017. 11. 14..
 */
@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private Environment environment;

//    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
//    public ResponseEntity<List<OneTimeBuy>> accountOneTimeBuy(HttpServletRequest request,
//                                                              HttpServletResponse response,
//                                                              @PathVariable("id") String id) {
//        try {
//            OrganizationRole role = organizationService.getOrganizationRole(request, OrganizationRole.MEMBER);
//            if (!role.getAccept()) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//
//            Organization organization = role.getOrganization();
//
//            Map account = kbRepository.getAccountById(id);
//            if (account == null) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            List<OneTimeBuy> oneTimeBuyList = oneTimeBuyService.selectByAccountId(organization.getId(), id);
//            if (oneTimeBuyList == null) {
//                oneTimeBuyList = new ArrayList<>();
//            }
//
//            return new ResponseEntity<>(oneTimeBuyList, HttpStatus.OK);
//        } catch (Exception ex) {
//            ExceptionUtils.httpExceptionKBResponse(ex, response);
//            return null;
//        }
//    }

//    @RequestMapping(value = "", method = RequestMethod.POST)
//    public ResponseEntity<DevopsApp> createApp(HttpServletRequest request,
//                                                             HttpServletResponse response,
//                                                             @RequestBody List<OneTimeBuyRequest> list,
//                                                             @RequestParam(value = "accountId") String accountId,
//                                                             @RequestParam(value = "requestedDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date requestedDate
//    ) {

    //TODO 생성
    //필요 정보들.
    //categoryItem: null,
//        cpu: 0.4,
    //categoryItemId,
//                mem: 512,
//                instances: 1,
//                appNumber: 1,
//                projectId: "",
//                appName: null,
//                externalProdDomain: null,
//                externalStgDomain: null,
//                externalDevDomain: null,
//                internalProdDomain: null,
//                internalStgDomain: null,
//                internalDevDomain: null

    //밸리데이션
    //appName 중복 체크
    //깃랩 프로젝트, 러너체크

    //1. dcos app 생성. => dcos-apps.yml (dev active)
    //4. deployment/<appName>.json 생성.

//        {
//           status: repository-create
//           definition: { 필요 정보들.
//           }
//        }
    //4. 리턴

    //프로젝트 포크
    // template/<categoryItemId>/definition.json

//        {
//            projectId:
//
//            mappings: [
//                {
//                    path: "src/main/../resources/bootstrap.yml"
//                    file: "file/bootstrap.yml"
//                }
//            ]
//        }
    //파일들
    //pipeline.yml
    //ci.yml
    //application.yml
    //bootstrap.yml
    //Dockerfile


    //포크 후 콘텐트 교체.
    //교체할 파라미터 셋은
    // - config url
    // - appName 어플리케이션 이름

    //러너 등록. 웹훅 등록. 파이프라인 실행
    //스테이터스 변경
//        {
//           status: repository-create-success or repository-create-failed
//           definition: {
//           }
//        }

    //TODO 파이프라인을 실행할 수 있어야 한다.
    //디플로이 명령어
    //파라미터: 어플리케이션 아이디


    //TODO 삭제
    //삭제 : 앱삭제 / 스테이징 삭제
    //앱삭제 경우
    //메소스 앱들을 가져온 후, 모두 지운다.
    //깃랩 프로젝트는 옵션으로 지울지 말지 정한다.
    //dcos-apps 에 해당 정보 삭제.
    //deployment/<appName>.json 삭제.

    //TODO 파이프라인 컨트롤
    //단계 취소. 단계 재실행
    //---단계 취소.
    //pipeline table 취소.
    //pipejob table 모두 취소.


    //TODO 딜리버리
    //[ci skip] 이 커밋에 있을 경우 건너뛰게 된다.
    //파이프라인 결과 저장을 위해서라도 RDBS 가 필요하다.
    //빌드, 스테이징 배치, 테스트(수동), 프로덕션 배치

    //파이프 라인 수동시작시 파이프라인 아이디를 기억해야 함.
    //파이프 라인 수동시작시 깃랩 이미지로 선택하여 시작 가능하다.

    //입력
    //- 깃 주소(고정)
    //- 브런치
    //- 옵션
    //     - 이 변경사항을 Git에 푸시할 때마다 작업 실행
    //     - 이 단계를 수동으로 실행할 때만 작업 실행


    //빌드
    //- gitlab ci 스크립트

    //이미지
    //- gitlab ci 스크립트

    //스테이징 배치
    //- gitlab ci 스크립트 - 배치 옵션(json) => curl
    //그럼, 스테이징 배치 후 기다리는 스크립트 제작해야 함.


    //테스트
    //스테이징에 배치된 컨테이너를 대상으로 테스트함.
    //테스트 이미지
    //테스트 스크립트 및 셀레니움
    //- 옵션
    // 이 작업이 성공 후 관리자의 승인을 받아야 다음단계 진행

    //curl 후 사용자 인증이 안되있으면 스스로 job_id 를 사용해 캔슬.
    //ui 에서 사용자 인증 클릭시 job_id resume.
    //리섬된 잡은 사용자 인증이 되어있으면 컴플리트 처리함.

    //프로덕션 배치
    //- 배치 옵션(json)
    // - 라우팅 옵션
    // 기존 프로덕션에서 신규 프로덕션으로 순차적으로 라우팅
    //   - 듀레이션 (시간)
    // 기존 프로덕션을 바로 신규 프로덕션으로 라우팅

    //curl 로 프로덕션 배치를 요청하고, 스크립트 대기하는 로직이 필요.


    //========
    //프로덕션 배치 내부 로직
    // 스테이징: <appName>-stg
    // 개발(이전) :<appName>-prod-green
    // 개발(신규) :<appName>-prod-blue

    //Zuul 시나리오
    //deployment/<appName>/pipeline.yml => 배치 라우팅 룰.

    //Zuul 순차 라우팅
    //프로덕션 배치 순서가 올 경우
    //----------------------
    // 이전 프로덕션이 있을 경우 (green)
    // <appName>-prod-blue 를 생성한다.
    // green 퍼센트 감소. blue 퍼센트 증가시킨다.
    // 모든 퍼센트가 끝났을 때, dcos-apps 에 prod 를 blue 로 등록한다.
    // 라우트 리프레쉬 한다.
    // green 을 삭제한다.

    //----------------------
    // 이전 프로덕션이 있고, 순차 라우팅이 아닐 경우(green)
    // <appName>-prod-blue 를 생성한다.
    // dcos-apps 에 prod 를 blue 로 등록한다.
    // 라우트 리프레쉬 한다.
    // green 을 삭제한다.

    //----------------------
    // 이전 프로덕션이 없을 경우 (green)
    // <appName>-prod-green 를 생성한다.
    // dcos-apps 에 prod active, green 으로 등록한다.
    // 라우트 리프레쉬

    //폴리글랏 haproxy
    //----------------------
    // 이전 프로덕션이 있을 경우 (green)
    // <appName>-prod-blue 를 생성한다. (haproxy 는 green 으로 쏘고있음)
    // green 을 삭제한다. (haproxy 는 blue 로 쏨)
    // dcos-apps 에 prod 를 blue 로 등록한다.


//        try {
//            OrganizationRole role = organizationService.getOrganizationRole(request, OrganizationRole.ADMIN);
//            if (!role.getAccept()) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//
//            List<OneTimeBuy> oneTimeBuyList = oneTimeBuyService.createOneTimeBuy(role.getOrganization(), list, accountId, requestedDate);
//            return new ResponseEntity<>(oneTimeBuyList, HttpStatus.CREATED);
//        } catch (Exception ex) {
//            ExceptionUtils.httpExceptionKBResponse(ex, response);
//            return null;
//        }
}

//    @RequestMapping(value = "/onetimebuy/{record_id}", method = RequestMethod.DELETE)
//    public ResponseEntity<OneTimeBuy> cancelOneTimeBuy(HttpServletRequest request,
//                                                       HttpServletResponse response,
//                                                       @PathVariable("record_id") Long record_id) {
//
//        try {
//            OrganizationRole role = organizationService.getOrganizationRole(request, OrganizationRole.ADMIN);
//            if (!role.getAccept()) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//
//            Organization organization = role.getOrganization();
//            OneTimeBuy oneTimeBuy = oneTimeBuyService.selectById(organization.getId(), record_id);
//            if (oneTimeBuy == null) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//            OneTimeBuy cancelOneTimeBuy = oneTimeBuyService.cancelOneTimeBuy(organization.getId(), record_id);
//            return new ResponseEntity<>(cancelOneTimeBuy, HttpStatus.OK);
//        } catch (Exception ex) {
//            ExceptionUtils.httpExceptionKBResponse(ex, response);
//            return null;
//        }
//    }

