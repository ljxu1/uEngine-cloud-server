/**
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.uengine.cloud.scheduler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.User;
import org.gitlab4j.api.models.Visibility;
import org.opencloudengine.garuda.client.IamClient;
import org.opencloudengine.garuda.client.model.OauthUser;
import org.opencloudengine.garuda.util.ApplicationContextRegistry;
import org.opencloudengine.garuda.util.JsonUtils;
import org.opencloudengine.garuda.util.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.env.Environment;
import org.uengine.cloud.app.AppCreate;
import org.uengine.cloud.app.AppService;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.app.GitlabExtentApi;
import org.uengine.cloud.templates.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeployAppJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String appName = map.get("appName").toString();
        String stage = map.get("stage").toString();
        String commit = null;
        if (map.get("commit") != null) {
            commit = map.get("commit").toString();
        }

        Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        DcosApi dcosApi = ApplicationContextRegistry.getApplicationContext().getBean(DcosApi.class);
        GitLabApi gitLabApi = ApplicationContextRegistry.getApplicationContext().getBean(GitLabApi.class);
        GitlabExtentApi gitlabExtentApi = ApplicationContextRegistry.getApplicationContext().getBean(GitlabExtentApi.class);

        try {

            //deployApp 는 마라톤 어플리케이션이 존재하는 경우만 사용가능하다. 단, 커밋을 전달받을 경우는 존재하지 않아도 가능.
            //이미지를 마라톤 어플리케이션의 이미지로 가져오도록 한다. 그리고 아래의 값을 치환하여 마라톤을 업데이트시킨다.
            //        {{APP_ID}}
            //        {{IMAGE}} => 마라톤 이미지
            //        {{SERVICE_PORT}}
            //        {{DEPLOYMENT}}
            //        {{EXTERNAL_URL}}
            //나머지는 dcosApp 정보에서 가져오도록.

            //앱 정보.
            Map app = appService.getAppByName(appName);
            String appType = app.get("appType").toString();
            int servicePort = (int) ((Map) app.get(stage)).get("service-port");
            String externalUrl = (String) ((Map) app.get(stage)).get("external");


            //커밋이 있고 프로덕션인 경우, 기존 프로덕션 이미지와 커밋이미지가 동일할 경우 blue,green 을 바꾸지 않는다.
            boolean bluegreenDeployment = false;
            if (!StringUtils.isEmpty(commit) && stage.equals("prod")) {
                String expectDockerImage = environment.getProperty("registry.host") + "/" + appName + ":" + commit;
                String currentDeployment = (String) ((Map) app.get(stage)).get("deployment");
                String currentMarathonAppId = appName + "-" + currentDeployment;
                Map marathonApp = null;
                try {
                    marathonApp = dcosApi.getApp(currentMarathonAppId);
                } catch (Exception ex) {

                }
                if (marathonApp != null) {
                    Map container = (Map) ((Map) marathonApp.get("app")).get("container");
                    String currentDockerImage = ((Map) container.get("docker")).get("image").toString();
                    //이미지 배포버젼이 달라졌을 경우 블루 그린 디플로이먼트를 한다.
                    if (!expectDockerImage.equals(currentDockerImage)) {
                        bluegreenDeployment = true;
                    }
                }
            }

            //블루그린 디플로이인 경우
            if (bluegreenDeployment) {
                String newMarathonAppId = null;
                String newDeployment = null;

                //현재 blue,green 상태와 반대되는 이름으로 생성
                String oldDeployment = (String) ((Map) app.get(stage)).get("deployment");
                String oldMarathonAppId = appName + "-" + oldDeployment;

                if (oldDeployment.equals("blue")) {
                    newMarathonAppId = appName + "-green";
                    newDeployment = "green";
                } else {
                    newMarathonAppId = appName + "-blue";
                    newDeployment = "blue";
                }

                //기존 마라톤 앱 가져오기(현재 프로덕션)
                Map oldMarathonApp = null;
                try {
                    oldMarathonApp = dcosApi.getApp(oldMarathonAppId);
                } catch (Exception ex) {

                }
                //신규 마라톤 앱 가져오기(롤백 프로덕션)
                Map newMarathonApp = null;
                try {
                    newMarathonApp = dcosApi.getApp(newMarathonAppId);
                } catch (Exception ex) {

                }


                //도커 이미지 가져오기 (현재 프로덕션 참조)
                String dockerImage = this.getDockerImage(commit, appName, oldMarathonApp);
                String deployJson = this.createDeployJson(
                        appName,
                        stage,
                        newMarathonAppId,
                        dockerImage,
                        servicePort,
                        newDeployment,
                        externalUrl
                );
                //신규 앱이 있을 경우 업데이트 디플로이
                if (newMarathonApp != null) {
                    dcosApi.updateApp(newMarathonAppId, deployJson);
                }
                //신규 앱이 없을 경우 신규 디플로이
                else {
                    dcosApi.createApp(deployJson);
                }

                //신규 앱 디플로이 종료 대기
                //5분동안 기다리기. 60 * 5s = 300s = 5min
                int MAX_COUNT = 60;
                int CURRENT_COUNT = 0;
                boolean deploySuccess = false;
                while (true) {
                    try {
                        Map deployingApp = dcosApi.getApp(newMarathonAppId);

                        //검증
                        //앱 배포 중단으로 인해 삭제됨.
                        if (deployingApp == null) {
                            System.out.println("Not found deploying marathonApp.");
                            break;
                        }

                        Map appMap = (Map) deployingApp.get("app");
                        Map container = (Map) appMap.get("container");
                        String deployingDockerImage = ((Map) container.get("docker")).get("image").toString();

                        //앱 배포 중단으로 인해 이미지가 변경되지 않음.
                        if (!deployingDockerImage.equals(dockerImage)) {
                            System.out.println("Deploying marathonApp Docker image is " + deployingDockerImage + " , But expect image is " + dockerImage);
                            break;
                        }
                        //중복 시도에 대해서는 로직이 같으므로 같은 결과.

                        //앱 배포 성공
                        int TASKS_RUNNING = (int) appMap.get("tasksRunning");
                        int TASKS_HEALTHY = (int) appMap.get("tasksHealthy");
                        int DEPLOYMENTS_LENGTH = ((List) appMap.get("deployments")).size();
                        if (TASKS_RUNNING == TASKS_HEALTHY && DEPLOYMENTS_LENGTH == 0) {
                            System.out.println("Deploy completed!!");
                            deploySuccess = true;
                            break;
                        }

                        CURRENT_COUNT++;
                        if (CURRENT_COUNT > MAX_COUNT) {
                            System.out.println("Time out. deployment will cancel.");
                            try {
                                dcosApi.deleteApp(newMarathonAppId);
                            } catch (Exception e) {

                            }
                            break;
                        } else {
                            System.out.println("Running deployment..");
                            Thread.sleep(5000);
                        }
                    } catch (Exception ex) {
                        CURRENT_COUNT++;
                        if (CURRENT_COUNT > MAX_COUNT) {
                            System.out.println("Time out. deployment will cancel.");
                            try {
                                dcosApi.deleteApp(newMarathonAppId);
                            } catch (Exception e) {

                            }
                            break;
                        } else {
                            System.out.println("Exception while get deployment app. Running deployment..");
                            Thread.sleep(5000);
                        }
                    }
                }


                //디플로이 성공하였을 경우
                if (deploySuccess) {
                    //dcosApp 에 디플로이먼트, 마라톤 아이디 업데이트
                    Map dcosMap = appService.getDcosMap();
                    ((Map) app.get(stage)).put("deployment", newDeployment);
                    ((Map) app.get(stage)).put("marathonAppId", "/" + newMarathonAppId);
                    Map apps = (Map) ((Map) dcosMap.get("dcos")).get("apps");
                    apps.put(appName, app);
                    appService.saveDcosYaml(dcosMap);

                    //ci-deploy-rollback.json 을 생성(ci-deploy-production.json 카피)
                    appService.copyDeployJson(appName, stage, "rollback");

                    //라우터 리프레쉬
                    dcosApi.refreshRouter();

                    //springboot 가 아닌경우 이전 버젼 삭제
                    if (!appType.equals("springboot")) {
                        dcosApi.deleteApp(oldMarathonAppId);
                    }

                } else {
                    System.out.println("Deployment failed by cancel.");
                }
            }
            //일반 배포인 경우
            else {
                String deployment = (String) ((Map) app.get(stage)).get("deployment");
                String marathonAppId = appName + "-" + deployment;
                Map marathonApp = null;
                try {
                    marathonApp = dcosApi.getApp(marathonAppId);
                } catch (Exception ex) {

                }
                String dockerImage = this.getDockerImage(commit, appName, marathonApp);
                String deployJson = this.createDeployJson(
                        appName,
                        stage,
                        marathonAppId,
                        dockerImage,
                        servicePort,
                        deployment,
                        externalUrl
                );
                //기존 앱이 있을 경우 업데이트 디플로이
                if (marathonApp != null) {
                    dcosApi.updateApp(marathonAppId, deployJson);
                }
                //기존 앱이 없을 경우 신규 디플로이
                else {
                    dcosApi.createApp(deployJson);
                }
                //라우터 리프레쉬
                dcosApi.refreshRouter();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getDockerImage(String commit, String appName, Map marathonApp) throws Exception {
        String dockerImage = null;
        //커밋이 없을경우 기존 마라톤 어플리케이션에서 이미지명 추출.
        if (commit == null) {
            if (marathonApp == null) {
                throw new Exception("Not found marathon app");
            }
            Map container = (Map) ((Map) marathonApp.get("app")).get("container");
            dockerImage = ((Map) container.get("docker")).get("image").toString();
        }
        //커밋이 있을경우 이미지 지정
        else {
            Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
            dockerImage = environment.getProperty("registry.host") + "/" + appName + ":" + commit;
        }
        return dockerImage;
    }

    private String createDeployJson(
            String appName,
            String stage,
            String marathonAppId,
            String dockerImage,
            int servicePort,
            String deployment,
            String externalUrl
    ) throws Exception {
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        Map deployJson = appService.getDeployJson(appName, stage);
        Map data = new HashMap();

        //APP_ID 는 디플로이 json 파일에 이미 "/" 처리가 되어있다.
        data.put("APP_ID", marathonAppId);
        data.put("IMAGE", dockerImage);
        data.put("SERVICE_PORT", servicePort);
        data.put("DEPLOYMENT", deployment);
        data.put("EXTERNAL_URL", externalUrl);

        String deployJsonString = JsonUtils.marshal(deployJson);
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        Map unmarshal = JsonUtils.unmarshal(templateEngine.executeTemplateText(deployJsonString, data));
        List<Map> portMappings = (List<Map>) ((Map) unmarshal.get("container")).get("portMappings");
        for (int i = 0; i < portMappings.size(); i++) {
            if (portMappings.get(i).containsKey("servicePort")) {
                portMappings.get(i).put("servicePort", servicePort);
            }
        }
        return JsonUtils.marshal(unmarshal);
    }
}
