package handlers.task;

import handlers.service.TaskQueueService;
import handlers.service.ThreadInstanceService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * JobThread 任务线程生成工厂
 * @author wangxi
 * @date 2019-11-03 16:22
 */
public class JobThreadFactory implements FactoryBean {


    @Autowired
    private ThreadInstanceService processService;
    @Autowired
    private TaskQueueService taskQueueService;
    @Autowired
    @Qualifier("jobClassName")
    private String[] jobs;

    @Override
    public JobTread getObject(){
        return new JobTread(processService, taskQueueService, jobs);
    }

    @Override
    public Class<?> getObjectType() {
        return JobTread.class;
    }
}
