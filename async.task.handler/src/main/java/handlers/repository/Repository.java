package handlers.repository;

import handlers.api.vo.TaskQueueVO;
import handlers.enums.TaskStatusEnums;
import handlers.models.TaskQueue;

import java.sql.SQLException;
import java.util.List;

/**
 * 加载Task的基类
 */
public interface Repository {

    void insertTask(TaskQueueVO taskQueue);

    /**
     * 更新每个线程和当前允许的序号
     * @param nid 运行中的序号
     * @param threadId 线程id
     * @return
     */
    int updateThreadPid(int nid, long threadId);

    /**
     * 更新每个线程当前处理的任务数等
     * @param nid
     * @param nowTaskSize
     * @param finishedTasks
     * @return
     */
    Boolean updateThreadTasksByNid(int nid, int nowTaskSize, int finishedTasks) throws SQLException;

    /**
     * 获取所有的线程id
     * @return
     */
    List<Integer> findAllNid();

    /**
     * 创建一个异步任务线程
     * @param nid
     * @param threadId
     */
    void create(int nid, long threadId);

    /**
     * 从异步任务池中取出一定数量的任务
     * @param statuses
     * @param threadNids
     * @param priority
     * @return
     */
    List<TaskQueue> peaks(List<Integer> statuses, List<Integer> threadNids, int priority);

    /**
     * 某个线程将一些任务归于自己执行
     * @param _ids
     * @param threadNid
     * @return
     */
    int execing(List<String> _ids, int threadNid);

    /**
     * 异步任务执行结果更新
     * @param _id
     */
    void updateTaskStatus(String _id, TaskStatusEnums taskStatusEnums);
}
