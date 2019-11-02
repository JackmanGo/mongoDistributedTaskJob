package handlers.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import handlers.enums.TaskStatusEnums;
import handlers.job.Job;
import handlers.models.TaskQueue;
import handlers.service.TaskQueueService;
import handlers.service.ThreadInstanceService;

public class JobTread {

	//当前job线程从数据库中加载出的task
	private List<TaskQueue> prepareExecTask;
	private int nowExecTasks = 0;
	private int finishedTasks = 0;//上次心跳到当前心跳到时间中处理的进程数，用做记录当前线程处理的task总数
	private ThreadInstanceService processService;
	private Timer timer = new Timer();
	private TaskQueueService taskQueueService;
	private String[] jobs;
	private int nid;
	private Map<String, Job> jobMap;
 
	public JobTread() {

	}
	
	public JobTread(int nid, String[] jobs,TaskQueueService taskQueueService ,ThreadInstanceService processService) {

		this.nid = nid;
		this.jobs = jobs;
		this.taskQueueService = taskQueueService;
		this.processService = processService;
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
	 * 每个Thread加载处理的task
	 * 
	 * @return
	 */
	public int loadTask() {

		//从数据库中peak出一定数量的task
		prepareExecTask = taskQueueService.peaks(nid);
		nowExecTasks= prepareExecTask.size();
		prepareExecTask.stream().forEach(it -> this.exec(it));

		return prepareExecTask.size();
	}

	private void openHeartbeat(long threadId) {
		// 开启一个定时
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TaskProcess的心跳，随时更新当前获取时间和正常处理的task数量
				processService.touch(nid, threadId, nowExecTasks, finishedTasks);
				finishedTasks = 0;
			}
		}, 0, 1000);
	}

	/**
	 * 执行task
	 */
	public void exec(TaskQueue take) {

		if (take==null) {
			return;
		}

		// 获取Job实例(com.mq.job.jobImpl下的实例)
		Job jobInstance = jobMap.get(take.getName());
		// Job来执行task
		TaskStatusEnums result = jobInstance.exec(take.getParams());

		switch (result) {
		case FAILED:
			// 处理失败, 置Task.status为8
			taskQueueService.execFail(take.get_id());
			break;
		case SUCCESS:
			// 处理成功，置Task.status为9
			taskQueueService.execSuccess(take.get_id());
			break;
		}

		nowExecTasks--;
		finishedTasks++;
	}
}
