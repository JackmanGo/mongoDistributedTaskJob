package handlers.task;

import handlers.enums.TaskStatusEnums;
import handlers.job.Job;
import handlers.models.TaskQueue;
import handlers.service.TaskQueueService;
import handlers.service.ThreadInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class JobTread {

	private static final Logger log = LoggerFactory.getLogger(JobTread.class);


	private ThreadInstanceService processService;
	private TaskQueueService taskQueueService;
	private String[] jobs;

	//当前job线程从数据库中加载出的task
	private List<TaskQueue> prepareExecTask;
	private int nowExecTasks = 0;
	//上次心跳到当前心跳到时间中处理的进程数，用做记录当前线程处理的task总数
	private int finishedTasks = 0;
	//执行心跳检测job
	private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("check-task-thread");
			t.setDaemon(true);
			return t;
		}
	});
	private int nid;
	private Map<String, Job> jobMap;

	//防止方法执行超时
	private static ExecutorService futureTaskService = Executors.newSingleThreadExecutor();


	public JobTread() {
	}

	public JobTread(ThreadInstanceService processService, TaskQueueService taskQueueService, String[] jobs) {

		this.processService = processService;
		this.taskQueueService = taskQueueService;
		this.jobs = jobs;
	}

	/**
	 * 初始化jobThread
	 * @param nid
	 */
	public void init(int nid){

		this.nid = nid;
		this.jobMap = getJobMap();
		openHeartbeat(Thread.currentThread().getId());
	}


	private Map<String, Job> getJobMap() {
		jobMap = new HashMap<String, Job>();
		// 根据jobs中的类名，反射出Class，根据类名为key，Class为value存入map
		for (int i = 0; i < jobs.length; i++) {
			String classPath = jobs[i];
			try {
				Class<Job> clazz = (Class<Job>) Class.forName(classPath);
				String className[] = clazz.getName().split("\\.");
				jobMap.put(className[className.length - 1], clazz.newInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jobMap;
	}

	/**
	 * 每个Thread-Job加载并处理的task
	 * 
	 * @return 当前处理对task数量
	 */
	public int loadTask() {

		//从数据库中peak出一定数量的task
		prepareExecTask = taskQueueService.peaks(nid);
		log.info("nid===>{}，获取任务数量===>{}", nid, prepareExecTask.size());
		nowExecTasks= prepareExecTask.size();
		prepareExecTask.stream().forEach(it -> this.execTask(it));
		log.info("nid===>{}，任务数量===>{}。执行完成", nid, prepareExecTask.size());

		return prepareExecTask.size();
	}

	/**
	 * 每秒执行一次
	 * @param threadId
	 */
	private void openHeartbeat(long threadId) {

		scheduledService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				log.info("每秒执行一次");
				// TaskProcess的心跳，随时更新当前获取时间和正常处理的task数量
				processService.touch(nid, threadId, nowExecTasks, finishedTasks);
				finishedTasks = 0;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * 执行task
	 * @param take task任务
	 */
	public void execTask(TaskQueue take) {

		if (take==null) {
			return;
		}

		// 获取Job实例(com.mq.job.jobImpl下的实例)
		Job jobInstance = jobMap.get(take.getName());

		FutureTask<TaskStatusEnums> futureTask = new FutureTask<>(new Callable<TaskStatusEnums>() {

			@Override
			public TaskStatusEnums call() throws Exception {
				//Job来执行task
				return jobInstance.exec(take.getParams());
			}
		});

		futureTaskService.execute(futureTask);

		//超时时间30秒，
		TaskStatusEnums result = null;
		try {
			result = futureTask.get(30, TimeUnit.SECONDS);
		} catch (Throwable e) {
			//e.printStackTrace();
			log.error("{}",e);
			if(e instanceof TimeoutException){
				log.error("任务id===>{},执行超时", take.get_id());
				result = TaskStatusEnums.TIMEOUT;
			}else {
				log.error("任务id===>{},执行失败", take.get_id());
				result = TaskStatusEnums.FAILED;

			}
		}

		taskQueueService.updateTaskStatus(take.get_id(), result);

		nowExecTasks--;
		finishedTasks++;
	}
}
