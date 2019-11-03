package job.jobImpl;

import handlers.enums.TaskStatusEnums;
import handlers.job.Job;

public class JobOne implements Job {

    @Override
    public TaskStatusEnums exec(String s) {

        System.out.println("I am jobOne。params===>" + s);
        return TaskStatusEnums.SUCCESS;
    }
}
