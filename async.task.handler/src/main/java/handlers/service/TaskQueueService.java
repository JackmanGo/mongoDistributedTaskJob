package handlers.service;

import handlers.enums.TaskStatusEnums;
import handlers.models.TaskQueue;
import handlers.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对数据库中对Task操作
 */
public class TaskQueueService {

	//private TaskQueueDao taskQueueDao = new TaskQueueDaoImpl();
	@Autowired
	Repository repository;

	/**
	 * 获取该次peak中的优先级
	 * 返回为1的概率最大，2，3，4，5依次
	 * @return
	 */
	public int roll() {
		Double random = Math.random();
		for (int i = 1; i <= 5; i++) {
			if (random > 1 / Math.pow(2, i)) {
				return i;
			}
		}
		return 1;
	}

	/**
	 * 从线程池中获取一定数量的task
	 * 
	 * @param threadNid
	 *            需要获取task的threadNid.非线程id而是作为唯一索引的nid
	 * @return List<TaskQueue>
	 */
	public List<TaskQueue> peaks(int threadNid) {


		//获取条件：task的状态为0和1（未处理和已加载内存）and threadNid 为0和当前线程threadNid
		// 获取task状态为0，1(初始化，处理中)。避免将加载到内存中task因重启而丢失
		List<Integer> statuses = new ArrayList<Integer>();
		statuses.add(0);
		statuses.add(1);

		// 获取task的tag为null和正在处理中
		List<Integer> tags = new ArrayList<Integer>();
		tags.add(0);
		tags.add(threadNid);
        
		//获取一个优先级
		int priority = this.roll();
				
		List<TaskQueue> taskQueues = repository.peaks(statuses, tags, priority);
		List<String> ids = taskQueues.stream().map(it -> it.get_id()).collect(Collectors.toList());


		// 如果查询数量为0，说明所有的task全部是处理中的，返回空
		if (ids.size() == 0) {
			return new ArrayList<TaskQueue>();
		}

		// 将上述获取到所有未处理的或当前线程正在处理的所有线程全部更新为当前线程正常处理
		// 此处的更新的条件除了in:ids外，还必须threadNid为当下线程的nid或0。因为此处可能有多个线程获取到未处理的task
		// 但只能有一个线程更新成功“处理中”
		repository.execing(ids, threadNid);

		// 再次获取出所有处理中的task
		List<Integer> execStatuses = new ArrayList<Integer>();
		execStatuses.add(1);
		List<Integer> execTags = new ArrayList<Integer>();
		execTags.add(threadNid);

		return repository.peaks(execStatuses, execTags, priority);
	}

	/**
	 * _id任务执行结果
	 * @param _id
	 */
	public void updateTaskStatus(String _id, TaskStatusEnums statusEnums) {
		repository.updateTaskStatus(_id, statusEnums);
	}

}
