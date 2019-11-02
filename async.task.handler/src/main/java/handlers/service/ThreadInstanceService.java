package handlers.service;

import handlers.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;

public class ThreadInstanceService {

    //private ThreadDao threadDao = new ThreadDaoImpl();
    @Autowired
    Repository repository;

    public void touch(int nid, long threadId, int taskSize, int finishedTasks) {
        Boolean isExisted = null;
        isExisted = repository.updateThreadTasksByNid(nid, taskSize, finishedTasks);

        if (!isExisted) {
            repository.create(nid, threadId);
        }
    }

}
