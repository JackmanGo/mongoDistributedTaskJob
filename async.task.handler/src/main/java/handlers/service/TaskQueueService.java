package handlers.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import handlers.models.TaskQueue;
import handlers.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;

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
		/**
		 * //获取task的状态为0 int status = 0; //获取task的tag为null String tag = null;
		 * //taskQueueDao.peaks中进行findAndModify。但每次只能获取一个,效率太低 TaskQueue taskQueues =
		 * taskQueueDao.peaks(nowExecingIds,status,tag);
		 **/
     
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

		// 将上述获取到所有未处理的或当前线程正在处理的所有线程全部更新为当前线程正常处理
		// 此处的更新的条件除了$in:ids外，还必须threadTag为当下线程的nid。因为此处可能有多个线程获取到未处理的task
		// 但只能有一个线程更新成功“处理中”
		int updateSize = repository.execing(ids, threadNid);

		// 如果更新出的数量为0，说明所有的task全部是处理中的，返回空
		if (updateSize == 0) {
			return new ArrayList<TaskQueue>();
		}

		// 获取出所有处理中的task
		List<Integer> execStatuses = new ArrayList<Integer>();
		execStatuses.add(1);
		List<Integer> execTags = new ArrayList<Integer>();
		execTags.add(threadNid);

		return repository.peaks(execStatuses, execTags, priority);
	}

	/**
	 * public int back(long expired) {
	 * 
	 * Date ltDate = new Date(new Date().getTime() - expired); return
	 * taskQueueDao.back(ltDate); }
	 **/
	public void execSuccess(String _id) {
		repository.success(_id);
	}

	public void execFail(String _id) {
		repository.fail(_id);
	}

	public void execBlock(String _id) {
		repository.block(_id);
	}
}
