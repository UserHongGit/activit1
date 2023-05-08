package com.cn.group;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class ActivitTest7 {
    /**
     * 根据候选人查询组任务
     * 查询组任务
     * */
    @Test
    public void test03(){
        String key = "evection-group";
        String candidateUser = "西施";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                                    .processDefinitionKey(key)
                                    .taskCandidateUser(candidateUser)
                                    .list();
        for (Task task : list) {
            System.out.println("流程实例Id：" + task.getProcessInstanceId());
            System.out.println("任务ID：" + task.getId());
            System.out.println("负责人:" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    //候选人员拾取组任务后该任务变为自己的个人任务
    /**
     * 候选人 拾取任务
     * */
    @Test
    public void test04() {
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "177502";
        // 候选人
        String userId = "西施";
        // 拾取任务
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskCandidateUser(userId)
                // 根据候选人查询
                .singleResult();
        if (task != null) {
            // 可以拾取任务
            taskService.claim(taskId, userId);
            System.out.println("拾取成功");
        }
    }

    //查询个人待办任务,查询方式同个人任务查询
    @Test
    public void test037(){
        String key = "evection-group";
        String candidateUser = "西施";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                                    .processDefinitionKey(key)
                                    //.taskCandidateUser(candidateUser)
                                    //.taskCandidateOrAssigned(candidateUser)
                                    .taskAssignee(candidateUser)
                                    .list();
        for (Task task : list) {
            System.out.println("流程实例Id：" + task.getProcessInstanceId());
            System.out.println("任务ID：" + task.getId());
            System.out.println("负责人:" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    /**
     *   完成个人任务
     *   办理个人任务,同个人任务办理
     *
     *   问题点:
     *       候选人是: 貂蝉, 西施
     *       西施拾取任务之后, 使用貂蝉complete任务, 竟然可以
     */
    @Test
    public void test05(){
        String  taskId = "177502";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        taskService.setAssignee(taskId,"貂蝉");
        taskService.complete(taskId);
        System.out.println("完成任务：" + taskId);
    }
    /**
     *   归还任务
     *      如果个人不想办理该组任务，可以归还组任务，归还后该用户不再是该任务的负责人
     */
    @Test
    public void test06(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "177502";
        String userId= "貂蝉";
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if(task != null){
            // 如果设置为null，归还组任务，任务没有负责人
            taskService.setAssignee(taskId,null);
        }
    }


    /**
     * 任务交接
     *      任务负责人将任务交给其他负责人来处理
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "75002";
        String userId= "zhangsan";
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if(task != null){
            // 设置该任务的新的负责人
            taskService.setAssignee(taskId,"赵六");
        }
    }

    /**
        查询当前任务执行表
        SELECT * FROM act_ru_task
        任务执行表，记录当前执行的任务，由于该任务当前是组任务，所有assignee为空，当拾取任务后该字段就是拾取用户的id


        查询任务参与者
        SELECT * FROM act_ru_identitylink
        任务参与者，记录当前参考任务用户或组，当前任务如果设置了候选人，会向该表插入候选人记录，有几个候选就插入几个
        与act_ru_identitylink对应的还有一张历史表act_hi_identitylink，向act_ru_identitylink插入记录的同时也会向历史表插入记录。任务完成
     */








}
