package handlers.api;

import handlers.api.vo.TaskQueueVO;
import handlers.repository.Repository;
import handlers.task.JobThreadFactory;
import handlers.task.JobTread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

public class TaskHandler{

    private static final Logger log = LoggerFactory.getLogger(TaskHandler.class);

    @Autowired
    private Repository repository;
    @Autowired
    private JobThreadFactory jobThreadFactory;

    public void startThreadPool(Integer threadPoolSize){

        // 创建一个顺序存储的阻塞队列，并指定大小为500
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(500);
        // 创建线程池的饱和策略，AbortPolicy抛异常
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        // 创建线程池，线程池基本大小5 最大线程数为10 线程最大空闲时间10分钟
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize+5, 5, TimeUnit.MINUTES, blockingQueue,
                handler);

        for (int i = 0; i < 5; i++) {
            final int nid = i+1;
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    exec(nid);
                }
            });
        }
    }

    public void insertAsyncTask(TaskQueueVO taskQueue){
        repository.insertTask(taskQueue);
    }

    /**
     * 序号为nid的线程job开始工作
     * @param nid
     */
    private void exec(int nid) {
        JobTread thread = jobThreadFactory.getObject();
        thread.init(nid);

        while (true) {
            log.info("nid===>{}。准备处理任务", nid);
            int tasks = thread.loadTask();
            log.info("nid===>{}，处理任务数量===>{}", nid, tasks);
            if (tasks == 0) {
                // 休息一会
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
