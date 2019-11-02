package job.jobImpl;

import handlers.enums.TaskStatusEnums;
import handlers.job.Job;

public class JobTwo implements Job {
    @Override
    public TaskStatusEnums exec(String s) {

        System.out.println("I am jobTwo" + s);
        return TaskStatusEnums.SUCCESS;
    }
}
