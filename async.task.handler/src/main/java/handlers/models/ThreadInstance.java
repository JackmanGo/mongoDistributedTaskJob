package handlers.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Objects;

@Document
public class ThreadInstance{
	@Id
	private String _id;
	@Indexed(unique=true)
	private int nid; // 序号
	private long pid; // 线程id
	private int finishedCounts; // 已处理完成的任务数
	private int nowCounts; //当前处理的线程总数
	private Date create; // 创建时间
	private Date update; // 上次活跃时间
	
	public ThreadInstance() {
		
	}
	public ThreadInstance(int nid, long threadId, int finishedCounts, int nowCounts) {
		this.nid = nid;
		this.pid = threadId;
		this.finishedCounts = finishedCounts;
		this.nowCounts = nowCounts;
		this.update = new Date();
	}
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public int getNid() {
		return nid;
	}

	public void setNid(int nid) {
		this.nid = nid;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public Date getUpdate() {
		return update;
	}

	public void setUpdate(Date update) {
		this.update = update;
	}

	public int getFinishedCounts() {
		return finishedCounts;
	}

	public void setFinishedCounts(int finishedCounts) {
		this.finishedCounts = finishedCounts;
	}

	public int getNowCounts() {
		return nowCounts;
	}

	public void setNowCounts(int nowCounts) {
		this.nowCounts = nowCounts;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ThreadInstance that = (ThreadInstance) o;
		return nid == that.nid &&
				pid == that.pid &&
				finishedCounts == that.finishedCounts &&
				nowCounts == that.nowCounts &&
				Objects.equals(_id, that._id) &&
				Objects.equals(create, that.create) &&
				Objects.equals(update, that.update);
	}

	@Override
	public int hashCode() {

		return Objects.hash(_id, nid, pid, finishedCounts, nowCounts, create, update);
	}
}
