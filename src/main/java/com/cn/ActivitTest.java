package com.cn;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 *  执行顺序:
 *      1, 流程部署
 *      2.0, 启动一个流程实例
 *      2.1  根据节点的处理人, 查询流程中的实例
 *      3, 节点的处理人complete任务, 让流程进入下一个节点
 *  流程本体相关功能:
 *      1, 历史流程查询
 *      2, 删除流程, 且流程删除了, 在db里也存着, 也可以查询出来数据
 *      3, 查询流程具体信息
 *      4, 根据流程下载png和bpmn图片
 *  问题:
 *      如果一个流程同时启动两个实例
 *      1, 两个实例处于不同的节点, 比如1流程在faqiren节点, 2流程在zongjingli节点
 *          faqiren去complete任务是没问题的, zongjingli去complete任务也是没问题的
 *          也就是同一个流程处于不同的活动节点, 各个节点complete任务是没问题的
 *      2, 但是如果1流程在zongjingli, 2流程也在zongjingli, zongjingli去complete任务的时候就会报错
 *          也就是同一个流程处于同一个活动节点, 各个节点去调用complete任务会报错, 提示超过2个
 *      解决:  启动两个实例会生成两个实例id, complete的时候需要指定实例id去complete
 */
@Slf4j
public class ActivitTest {

    /**
     * 生成Activiti的相关的表结构
     */
   @Test
   public void inittable(){
       // 使用classpath下的activiti.cfg.xml中的配置来创建 ProcessEngine对象
       ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
       System.out.println(engine);
   }

    /**
     *
     * 1, 流程部署, 可以单个文件部署, 也可以打包成一个zip, 进行部署
     * 实现文件的单个部署
     *  不能部署两份
     */
    @Test
    public void test03(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection.bpmn20.xml") // 添加bpmn资源
                .addClasspathResource("bpmn/evection.png") // 添加png资源
                .name("出差申请流程")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
        //流程部署的id:32501
        //流程部署的名称：出差申请流程
    }
    /**
     * 2.0, 通过1定义好了流程(Java类),  现在需要启动实例, 走流程了(对象)
     * 启动一个流程实例
     */
    @Test
    public void test05(){
        // 1.创建ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.根据流程定义的id启动流程
        String id= "evection";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(id);
        // 4.输出相关的流程实例信息
        System.out.println("流程定义的ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例的ID：" + processInstance.getId());
        System.out.println("当前活动的ID：" + processInstance.getActivityId());
        //流程定义的ID：evection:2:20004
        //流程实例的ID：30001
        //当前活动的ID：null
    }
    /**
     * 2.1
     *      查询节点人有没有任务
     *      指定流程id, 根据受让人(流程某一个节点的人id)查询任务
     *      只有该流程中, 处于该节点的活动状态, 才能查询到任务信息
     *      也就是, 只有该流程中, 到了那一步节点了, 才能查询出任务信息
     */
    @Test
    public void test06(){
        String assignee ="faqiren";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 任务查询 需要获取一个 TaskService 对象
        TaskService taskService = engine.getTaskService();
        // 根据流程的key和任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee(assignee)
                .list();
        //HistoricProcessInstanceQuery query = engine.getHistoryService().createHistoricProcessInstanceQuery();
        //HistoricProcessInstanceQuery instance = query.processInstanceId("2501"); //指定实例id查询
        //List<HistoricProcessInstance> list2 = instance.unfinished().list();//如果实例里有unfinished的, list为0, 即代表流程结束
                                                                            //或者instance.unfinished.singleResult 返回null就代表流程结束
        // 输出当前用户具有的任务
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id:" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
        //流程实例id：27501
        //任务id:27505
        //任务负责人：faqiren
        //任务名称：创建请假单

        //流程实例id：30001
        //任务id:30005
        //任务负责人：faqiren
        //任务名称：创建请假单
    }

    /**
     * 3, 流程任务的处理
     *      开始真正走流程了, 一个节点一个节点的走,
     *      通过指定taskAssignee  >>> zhangsna,jingli,zongjingli,caiwu 一个一个的complete任务, 让流程内节点一个一个走
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processInstanceId("30001")   //如果启动了两个流程实例, 就需要指定实例id, 然后完成
                .processDefinitionKey("evection")
                .taskAssignee("faqiren")
                .singleResult();
        engine.getHistoryService().createHistoricActivityInstanceQuery().processDefinitionId("evection");
        task.getDelegationState();
        // 完成任务
        taskService.complete(task.getId());
        log.info("{} 处理了任务!",task.getAssignee());
    }

    /**
     * 流程处理:
     *      查询流程的定义
     */
    @Test
    public void test08(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 获取一个 ProcessDefinitionQuery对象 用来查询操作
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionKey("evection")
                .orderByProcessDefinitionVersion() // 安装版本排序
                .desc() // 倒序
                .list();
        // 输出流程定义的信息
        for (ProcessDefinition processDefinition : list) {
            System.out.println("流程定义的ID：" + processDefinition.getId());
            System.out.println("流程定义的name：" + processDefinition.getName());
            System.out.println("流程定义的key:" + processDefinition.getKey());
            System.out.println("流程定义的version:" + processDefinition.getVersion());
            System.out.println("流程部署的id:" + processDefinition.getDeploymentId());
        }
        //流程定义的ID：evection:1:4
        //流程定义的name：出差申请单
        //流程定义的key:evection
        //流程定义的version:1
        //流程部署的id:1
    }
    /**
     * 流程处理
     *      删除流程
     */
    @Test
    public void test09(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 删除流程定义，如果该流程定义已经有了流程实例启动则删除时报错
        repositoryService.deleteDeployment("90011",true);
        // 设置为TRUE 级联删除流程定义，及时流程有实例启动，也可以删除，设置为false 非级联删除操作。
        //repositoryService.deleteDeployment("12501",true);
    }
    /**
     * 流程处理
     *      读取数据库中的资源文件
     *      指定一个流程id, 从数据库里读取流程图和bpmn图
     */
    @Test
    public void test10() throws Exception{
        // 1.得到ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService对象
        RepositoryService repositoryService = engine.getRepositoryService();
        // 3.得到查询器
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("evection")
                .singleResult();
        // 4.获取流程部署的id
        String deploymentId = definition.getDeploymentId();
        // 5.通过repositoryService对象的相关方法 来获取图片信息和bpmn信息
        // png图片
        InputStream pngInput = repositoryService
                .getResourceAsStream(deploymentId, definition.getDiagramResourceName());
        // bpmn 文件的流
        InputStream bpmnInput = repositoryService
                .getResourceAsStream(deploymentId, definition.getResourceName());
        // 6.文件的保存
        File filePng = new File("F:\\HongStudy\\idea\\logs/evection.png");
        File fileBpmn = new File("F:\\HongStudy\\idea\\logs/evection.bpmn");
        OutputStream pngOut = new FileOutputStream(filePng);
        OutputStream bpmnOut = new FileOutputStream(fileBpmn);

        IOUtils.copy(pngInput,pngOut);
        IOUtils.copy(bpmnInput,bpmnOut);

        pngInput.close();
        pngOut.close();
        bpmnInput.close();
        bpmnOut.close();
    }
    /**
     * 流程处理
     *      流程历史信息查看
     */
    @Test
    public void test11(){
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


}
