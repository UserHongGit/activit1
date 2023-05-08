package com.cn.process.var;

import com.cn.process.var.entity.Evection;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程变量的使用
 */
public class ActivityTest6 {
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
                service.createDeployment().addClasspathResource("process-var-bpmn/evection-process-var.bpmn20.xml")
                        // 添加bpmn资源
                        .addClasspathResource("process-var-bpmn/evection-process-var.png")
                        // 添加png资源
                        .name("出差申请流程-流程变量")
                        .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());

    }


    /**
     * 启动流程实例，设置流程变量
     * 1, 设置global变量方式一
     * */
    @Test
    public void test02(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        // 流程定义key
        String key = "evection-process-var";
        // 创建变量集合
        Map<String,Object> variables = new HashMap<>();
        // 创建出差对象 POJO
        Evection evection = new Evection();
        // 设置出差天数
        evection.setNum(4d);
        evection.setZjl(true);
        evection.setBmjl(false);
        // 定义流程变量到集合中
        variables.put("entity",evection);
        // 设置assignee的取值
        variables.put("var0","李白");
        variables.put("var1","露娜");
        variables.put("var2","王昭君");
        //Hong:  通过这种方式在初始化的时候就设置了global的变量
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        //ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key);
        // 输出信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());

        Map<String, Object> processVar = processInstance.getProcessVariables();
        Map<String, Object> runtimeVar = runtimeService.getVariables(key);
        System.out.println();

        //获取流程实例名称：null
        //流程定义ID：evection-process-var:1:85004
    }


    /**
     * 2, 设置global变量方式二
     *  这里新加了一个对象,  runtime-evection
     *  executionId通过CommonTest.test06(), 获取执行id
     */
    @Test
    public void setGlobalVariableByExecutionId(){
        //    当前流程实例执行 id，通常设置为当前执行的流程实例
        String executionId="140008";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //        创建出差pojo对象
        Evection evection = new Evection();
        evection.setNum(2d);
        //      通过流程实例 id设置流程变量
        //Hong: 通过这种方式, 也可以设置global变量
        runtimeService.setVariable(executionId, "runtime-evection", evection);
        //Map<String,Object> variables = new HashMap<>();
        //variables.put("assignee0","张三1");
        //variables.put("assignee1","李四1");
        //      一次设置多个值
        //runtimeService.setVariables(executionId, variables);

        Map<String, Object> globalExecutionProcessVar = runtimeService.getVariables(executionId);
        System.out.println();
    }


    /**
     * 3, 设置global变量方式三
     */
    @Test
    public void setGlobalVariableByTaskId(){

        //当前待办任务id
        String taskId="112511";
        //     获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Evection evection = new Evection();
        evection.setNum(3);
        //通过任务设置流程变量
        taskService.setVariable(taskId, "task-evection", evection);
        //一次设置多个值
        // taskService.setVariables(taskId, variables);


        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> taskProcessVar = taskService.getVariables(taskId);// 这俩数据一样
        Map<String, Object> map = runtimeService.getVariables(""); //  这俩数据一样
        System.out.println();

    }

    /**
     * 1, 设置本地变量
     */
    /**处理任务时设置local流程变量*/
    @Test
    public void completTask() {
        //任务id
        String taskId = "157511";
        //  获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //  定义流程变量
        Map<String, Object> variables = new HashMap<String, Object>();
        Evection evection = new Evection ();
        evection.setNum(8d);
        // 定义流程变量
        //  变量名是holiday，变量值是holiday对象
        variables.put("local-evection", evection);
        //  设置local变量，作用域为该任务
        taskService.setVariablesLocal(taskId, variables);

        Map<String, Object> globalVar = taskService.getVariables(taskId); //获取全部的全局变量 num = 4
        Map<String, Object> localVar = taskService.getVariablesLocal(taskId); //获取全部的本地变量 num = 8

        Object localVarObj = taskService.getVariable(taskId, "local-evection"); //因为全局变量没有evection, 所以得到的是本地变量的值  num = 8
        Object globalVarObj = taskService.getVariable(taskId, "entity"); //全局变量有entity, 所以得到的是全局的  num = 8
        Object localVarObj2 = taskService.getVariableLocal(taskId, "local-evection"); //获取本地变量值
        //  完成任务
        taskService.complete(taskId);
    }

    /**
     * 2, 设置本地变量
     */
    @Test
    public void setLocalVariableByTaskId(){
        //   当前待办任务id
        String taskId="1404";
        //  获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Evection evection = new Evection ();
        evection.setNum(3d);
        //  通过任务设置流程变量
        taskService.setVariableLocal(taskId, "evection", evection);
        //  一次设置多个值
        //taskService.setVariablesLocal(taskId, variables);
        HistoryService historyService = processEngine.getHistoryService();
        // 创建历史任务查询对象
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
        // 查询结果包括 local变量
        List<HistoricTaskInstance> list = historicTaskInstanceQuery.includeTaskLocalVariables().list();
        for (HistoricTaskInstance historicTaskInstance : list) {
            System.out.println("==============================");
            System.out.println("任务id：" + historicTaskInstance.getId());
            System.out.println("任务名称：" + historicTaskInstance.getName());
            System.out.println("任务负责人：" + historicTaskInstance.getAssignee());
            System.out.println("任务local变量："+ historicTaskInstance.getTaskLocalVariables());
        }

    }




}
