package handlers.repository;

import handlers.api.vo.TaskQueueVO;
import handlers.models.TaskQueue;
import handlers.models.ThreadInstance;
import com.mongodb.WriteResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MongoDBRepository implements Repository {

    @Autowired
    MongoTemplate template;

    @Override
    public void insertTask(TaskQueueVO taskQueueVO){

        TaskQueue taskQueue = new TaskQueue();
        BeanUtils.copyProperties(taskQueueVO, taskQueue);
        template.save(taskQueue);

    }

    @Override
    public int updateThreadPid(int nid, long threadId) {
        return 0;
    }

    @Override
    public Boolean updateThreadTasksByNid(int nid, int nowTaskSize, int finishedTasks) {

        if(template.findAndModify(Query.query(Criteria.where("nid").is(nid)), new Update().set("update", new Date()).set("finishedCounts", finishedTasks).inc("nowCounts", nowTaskSize), ThreadInstance.class) == null){
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public List<Integer> findAllNid() {

        List<ThreadInstance> threads = template.findAll(ThreadInstance.class);
        return threads.stream().sorted().map(thread -> thread.getNid()).collect(Collectors.toList());
    }

    @Override
    public void create(int nid, long threadId) {

        template.insert(new ThreadInstance(nid,threadId,0,0));
    }

    @Override
    public List<TaskQueue> peaks(List<Integer> statuses, List<Integer> threadNids, int priority) {

        return template.find(Query.query(Criteria.where("status").in(statuses).and("threadNid").in(threadNids).and("priority").is(priority)).limit(20), TaskQueue.class);
    }

    @Override
    public int execing(List<String> _ids, int threadNid) {

        WriteResult writeResult = template.updateMulti(Query.query(Criteria.where("_id").in(_ids).and("threadNid").is(null)), Update.update("status", 1).set("threadNid", threadNid), TaskQueue.class);
        return writeResult.getN();
    }

    @Override
    public void success(String _id) {

        template.updateFirst(Query.query(Criteria.where("_id").is(_id)), Update.update("status", 9), TaskQueue.class);
    }

    @Override
    public void fail(String _id) {

        template.updateFirst(Query.query(Criteria.where("_id").is(_id)), Update.update("status", 8), TaskQueue.class);
    }

    @Override
    public void block(String _id) {

    }
}
