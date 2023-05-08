package com.cn.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;


/**
 * 实例是实现TaskListener(任务监听器)
 * 但是报错, 提示需要实现ExecutionListener(流程监听器), 测试可以用, 但是没办法给赋值了
 *  区别和为什么先没看
 */
public class MyTaskListener implements ExecutionListener {
    //@Override
    public void notify(DelegateTask delegateTask) {

    }

    @Override
    public void notify(DelegateExecution execution) {
        System.out.println(execution.getEventName());
        System.out.println(execution.getProcessDefinitionId());
        System.out.println(execution.getProcessInstanceId());
        System.out.println(execution.getProcessInstanceBusinessKey());

        //if("创建请假单".equals(delegateTask.getName())
        //        && "create".equals( execution.getEventName())){
        //    // 指定任务的负责人
        //    delegateTask.setAssignee("李四-Listener");
        //}
    }
}

