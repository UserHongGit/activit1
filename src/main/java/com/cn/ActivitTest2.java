package com.cn;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

import java.util.Date;


/**
 * businessKey关联业务逻辑
 * 流程的挂起和激活
 *      挂起可以通过指定流程定义id, 然后将该流程定义下的所有流程都挂起,
 *      也可以指定流程实例id, 单个挂起一个实例
 */
public class ActivitTest2 {

    /**
     * 启动流程实例，添加businessKey
     * Hong:
     *      主要得使用预留的businessKey与实际的业务关联起来
     *      如图:
     *      https://img-blog.csdnimg.cn/3ec7581ae9f14ce6bead974f5ad226f9.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA5rOi5rOi54Ok6bit,size_20,color_FFFFFF,t_70,g_se,x_16
     *  涉及表:
     *      act_ru_execution
     */
    @Test
    public void test01() {
        // 1.获取ProcessEngine对象
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 3.启动流程实例
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("evection", "1001");
        // 4.输出processInstance相关属性
        System.out.println("businessKey = "+instance.getBusinessKey());
    }

    /**
     * 全部流程挂起实例与激活, 挂起状态继续操作实例会报错
     * suspended: 挂起
     *
     * 在实际场景中可能由于流程变更需要将当前运行的流程暂停而不是删除，流程暂停后将不能继续执行。
     *
     *  涉及表:
     *      act_ru_task
     *      挂起后, 会改变表中SUSPENSION_STATE的值, 挂起状态为2激活为1
     */
    @Test
    public void test02(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService对象
        RepositoryService repositoryService = engine.getRepositoryService();
        // 3.查询流程定义的对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                                                                .processDefinitionKey("evection")
                                                                .singleResult();
        // 4.获取当前流程定义的状态
        boolean suspended = processDefinition.isSuspended();
        String id = processDefinition.getId();
        // 5.如果挂起就激活，如果激活就挂起
        if(suspended){
            // 表示当前定义的流程状态是 挂起的
            //processDefinitionId ->id 流程id
            //suspendProcessInstances ->  true: 是否激活
            //suspensionDate -> null: 挂起时间
            repositoryService.activateProcessDefinitionById(
                    id,
                    true,
                    null
            );
            // 流程定义的id ,true // 是否激活            ,null // 激活时间            );
            System.out.println("流程定义：" + id + ",已激活");
        }else{
            // 非挂起状态，激活状态 那么需要挂起流程定义
            repositoryService.suspendProcessDefinitionById(id);
            //processDefinitionId ->id 流程id
            //suspendProcessInstances ->  true: 是否激活
            //suspensionDate -> null: 挂起时间
            repositoryService.suspendProcessDefinitionById(id,true,null);
            System.out.println("流程定义：" + id + ",已挂起");
        }
    }
    /**
     * 单个流程实例挂起与激活
     */
    @Test
    public void test03(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.获取流程实例对象
        ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId("25001")
                        .singleResult();
        // 4.获取相关的状态操作
        boolean suspended = processInstance.isSuspended();
        String id = processInstance.getId();
        if(suspended){
            // 挂起--》激活
            runtimeService.activateProcessInstanceById(id);
            System.out.println("流程定义：" + id + "，已激活");
        }else{
            // 激活--》挂起
            runtimeService.suspendProcessInstanceById(id);
            System.out.println("流程定义：" + id + "，已挂起");
        }
    }








}
