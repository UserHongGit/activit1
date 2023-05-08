package com.cn;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class ActivityTest5 {
    // 查询当前个人待执行的任务
     @Test
    public void findPersonalTaskList() {
        // 流程定义key
        String processDefinitionKey = "evection-uel";
        // 任务负责人
        String assignee = "张三";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                                .processDefinitionKey(processDefinitionKey)
                                .includeProcessVariables()
                                .taskAssignee(assignee)  //条件可删除
                                .list();
        for (Task task : taskList) {
            System.out.println("----------------------------");
            System.out.println("流程实例id： " + task.getProcessInstanceId());
            System.out.println("任务id： " + task.getId());
            System.out.println("任务负责人： " + task.getAssignee());
            System.out.println("任务名称： " + task.getName());
        }
    }

    /**
     * 1,根据流程定义id 和 负责人  找到一个活动任务
     * 2,根据活动任务找到流程实例id
     * 3, 根据id找到对应的businessKey
     */
    @Test
    public void findProcessInstance(){
         //        获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //        获取TaskService
        TaskService taskService = processEngine.getTaskService();
        //        获取RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //        查询流程定义的对象
        Task task = taskService.createTaskQuery()
                                .processDefinitionKey("evection")
                                .taskAssignee("jingli")
                                .singleResult();
        //        使用task对象获取实例id
        String processInstanceId = task.getProcessInstanceId();
        //          使用实例id，获取流程实例对象
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                                                        .processInstanceId(processInstanceId)
                                                        .singleResult();
        //        使用processInstance，得到 businessKey
        String businessKey = processInstance.getBusinessKey();
        System.out.println("businessKey=="+businessKey);
    }

    /**
     *  完成任务，判断当前用户是否有权限
     */
    @Test
    public void completTask() {
        //任务id
        String taskId = "15005";
        //        任务负责人
        String assingee = "张三";
        //获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 创建TaskService
        TaskService taskService = processEngine.getTaskService();
        //        完成任务前，需要校验该负责人可以完成当前任务
        //        校验方法：
        //        根据任务id和任务负责人查询当前任务，如果查到该用户有权限，就完成
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(assingee)
                .singleResult();
        if(task != null){
            taskService.complete(taskId);
            System.out.println("完成任务");
        }
    }



    }
