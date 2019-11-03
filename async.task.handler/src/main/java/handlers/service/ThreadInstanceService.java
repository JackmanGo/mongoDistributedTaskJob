package handlers.service;

import handlers.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;

public class ThreadInstanceService {

    @Autowired
    Repository repository;
    
    public void touch(int nid, long threadId, int taskSize, int finishedTasks) {
        Boolean isExisted = null;

        try {
            isExisted = repository.updateThreadTasksByNid(nid, taskSize, finishedTasks);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (!isExisted) {
            repository.create(nid, threadId);
        }
    }

}
