package com.cn.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

public class ActivitTest4 {
    /**
     *  先将新定义的流程部署到Activiti中数据库中
     */
    @Test
    public void test3(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy =
                service.createDeployment().addClasspathResource("listener-bpmn/evection-listener.bpmn20.xml")
                        // 添加bpmn资源
                        .addClasspathResource("listener-bpmn/evection-listener.png")
                        // 添加png资源
                        .name("出差申请流程-监听赋值")
                        .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());

    }

    /**
     * 创建一个流程实例
     *    给流程定义中的 UEL表达式赋值
     */
    @Test
    public void test02(){
        // 获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 创建流程实例
        runtimeService.startProcessInstanceByKey("evection-listener");

    }

}
