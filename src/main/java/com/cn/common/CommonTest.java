package com.cn.common;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class CommonTest {
    private String processDefinitionKey = "evection-group";
    String assignee ="武则天";


    /*
    部署流程
     */
    @Test
    public void initProcess(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("group/evection-group.bpmn20.xml") // 添加bpmn资源
                .addClasspathResource("group/evection-group.png") // 添加png资源
                .name("Group-出差申请流程")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
        //流程部署的id:32501
        //流程部署的名称：出差申请流程
    }

    /**
     * 开始一个新流程实例
     */
    @Test
    public void startProcessInstance(){
        // 1.创建ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.根据流程定义的id启动流程
        String id= processDefinitionKey;
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(id);
        // 4.输出相关的流程实例信息
        System.out.println("流程定义的ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例的ID：" + processInstance.getId());
        System.out.println("当前活动的ID：" + processInstance.getActivityId());
    }

    /**
     * 根据流程定义id查询流程实例信息
     */
    @Test
    public void getInfoByProcessDefinedKey(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 任务查询 需要获取一个 TaskService 对象
        TaskService taskService = engine.getTaskService();
        // 根据流程的key和任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();
        //HistoricProcessInstanceQuery query = engine.getHistoryService().createHistoricProcessInstanceQuery();
        //HistoricProcessInstanceQuery instance = query.processInstanceId("2501"); //指定实例id查询
        //List<HistoricProcessInstance> list2 = instance.unfinished().list();//如果实例里有unfinished的, list为0, 即代表流程结束
        //或者instance.unfinished.singleResult 返回null就代表流程结束
        // 输出当前用户具有的任务
        //Task -->  act_ru_task
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id:" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
            System.out.println("定义id:"+task.getProcessDefinitionId());
            System.out.println("执行id:"+task.getExecutionId());
        }
    }

    /*
      查看历史流程信息
   */
    @Test
    public void historyInfo(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 查看历史信息我们需要通过 HistoryService来实现
        HistoryService historyService = engine.getHistoryService();
        // 获取 actinst 表的查询对象
        HistoricActivityInstanceQuery instanceQuery = historyService.createHistoricActivityInstanceQuery();
        //instanceQuery.processDefinitionId("evection:1:4");
        instanceQuery.orderByHistoricActivityInstanceStartTime().desc();
        List<HistoricActivityInstance> list = instanceQuery.list();
        // 输出查询的结果
        for (HistoricActivityInstance hi : list) {
            System.out.println(hi.getActivityId());
            System.out.println(hi.getActivityName());
            System.out.println(hi.getActivityType());
            System.out.println(hi.getAssignee());
            System.out.println(hi.getProcessDefinitionId());
            System.out.println(hi.getProcessInstanceId());
            System.out.println("-----------------------");
        }
    }

    /**
     * 指定人查询是否有活动处理
     */
    @Test
    public void getInfoByAssignee(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 任务查询 需要获取一个 TaskService 对象
        TaskService taskService = engine.getTaskService();
        // 根据流程的key和任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(assignee)
                .list();
        //HistoricProcessInstanceQuery query = engine.getHistoryService().createHistoricProcessInstanceQuery();
        //HistoricProcessInstanceQuery instance = query.processInstanceId("2501"); //指定实例id查询
        //List<HistoricProcessInstance> list2 = instance.unfinished().list();//如果实例里有unfinished的, list为0, 即代表流程结束
        //或者instance.unfinished.singleResult 返回null就代表流程结束
        // 输出当前用户具有的任务
        //Task -->  act_ru_task
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id:" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
            System.out.println("定义id:"+task.getProcessDefinitionId());
            System.out.println("执行id:"+task.getExecutionId());
        }
    }


    /**
     * 指定负责人完成当前审批
     */
    @Test
    public void completeTaskByAssignee(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                //.processInstanceId("30001")   //如果启动了两个流程实例, 就需要指定实例id, 然后完成
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(assignee)
                .singleResult();
        task.getDelegationState();
        // 完成任务
        taskService.complete(task.getId());
    }

    /**
     * 流程处理
     *      删除流程
     */
    @Test
    public void delProcessByDepId(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 删除流程定义，如果该流程定义已经有了流程实例启动则删除时报错
        repositoryService.deleteDeployment("115001",true);
        // 设置为TRUE 级联删除流程定义，及时流程有实例启动，也可以删除，设置为false 非级联删除操作。
        //repositoryService.deleteDeployment("12501",true);
    }

    /**
     * 查询流程是否继续
     */
    @Test
    public void isProcessContinue(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 任务查询 需要获取一个 TaskService 对象
        TaskService taskService = engine.getTaskService();
        // 根据流程的key和任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(assignee)
                .list();
        HistoricProcessInstanceQuery query = engine.getHistoryService().createHistoricProcessInstanceQuery();
        HistoricProcessInstanceQuery instance = query.processDefinitionKey(processDefinitionKey);
        //HistoricProcessInstanceQuery instance = query.processInstanceId("2501"); //指定实例id查询
        List<HistoricProcessInstance> list2 = instance.unfinished().list();//如果实例里有unfinished的, list为0, 即代表流程结束
        //或者instance.unfinished.singleResult 返回null就代表流程结束
        if(null==list2 || list.size()<1){
            System.out.println("流程结束");
        }else{
            System.out.println("流程还在继续");
        }

    }




}
