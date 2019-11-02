package handlers.models;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TaskQueue {
	@Id
	private String _id;
	private String name;
	private String params;
	private int status; // 状态，0:初始 1:处理中 3:处理失败 4:处理成功
	private int retry; // 重试次数
	private Date create; // 创建时间
	private Date update;
	private int threadNid;
	private int priority; // 时间越小，被执行的概率越大

	public TaskQueue() {

	}

	public TaskQueue(String name, String params) {
		this.name = name;
		this.params = params;
		this.priority = 1;
	}

	public TaskQueue(String name, String params, int priority) {
		this.name = name;
		this.params = params;
		this.priority = priority;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

	public Date getUpdate() {
		return update;
	}

	public void setUpdate(Date update) {
		this.update = update;
	}

	public int getThreadNid() {
		return threadNid;
	}

	public void setThreadNid(int threadNid) {
		this.threadNid = threadNid;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskQueue taskQueue = (TaskQueue) o;
		return status == taskQueue.status &&
				retry == taskQueue.retry &&
				threadNid == taskQueue.threadNid &&
				priority == taskQueue.priority &&
				Objects.equals(_id, taskQueue._id) &&
				Objects.equals(name, taskQueue.name) &&
				Objects.equals(params, taskQueue.params) &&
				Objects.equals(create, taskQueue.create) &&
				Objects.equals(update, taskQueue.update);
	}

	@Override
	public int hashCode() {

		return Objects.hash(_id, name, params, status, retry, create, update, threadNid, priority);
	}
}
