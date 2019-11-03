package handlers.repository;

import handlers.api.vo.TaskQueueVO;
import handlers.enums.TaskStatusEnums;
import handlers.models.TaskQueue;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcRepository implements Repository {

    private ThreadLocal<Connection> connectLocal = new ThreadLocal();

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void insertTask(TaskQueueVO taskQueue){

        Connection connection = null;
        PreparedStatement stmt = null;

        try {

            connection = dataSource.getConnection();
            String sqlStr = "insert into task_queue(_id, name, params, priority) value (?,?,?,?)";
            stmt = connection.prepareStatement(sqlStr);

            stmt.setString(1, taskQueue.get_id());
            stmt.setString(2, taskQueue.getName());
            stmt.setString(3, taskQueue.getParams());
            stmt.setInt(4, taskQueue.getPriority());

            stmt.executeUpdate();
        }catch (SQLException e){

            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int updateThreadPid(int nid, long threadId) {

        Connection connection;

        PreparedStatement stmt;

        try {

            connection = dataSource.getConnection();
            String sqlStr = "update thread_instance set pid = ? where nid = ?";
            stmt = connection.prepareStatement(sqlStr);

            stmt.setLong(1, threadId);
            stmt.setInt(2, nid);

            int result = stmt.executeUpdate();
            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    @Override
    public Boolean updateThreadTasksByNid(int nid, int nowTaskSize, int finishedTasks) throws SQLException {

        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = dataSource.getConnection();
            String selectSqlStr = "select * from thread_instance where nid = ?";

            stmt = connection.prepareStatement(selectSqlStr);
            stmt.setInt(1, nid);
            ResultSet resultSet = stmt.executeQuery();

            if (!resultSet.next()) {
                return Boolean.FALSE;
            }

            String updateSqlStr = "update thread_instance set now_counts = ? and finished_counts = ? and `update` = now() where nid = ?";
            stmt = connection.prepareStatement(updateSqlStr);

            stmt.setInt(1, nowTaskSize);
            stmt.setInt(2, finishedTasks);
            stmt.setInt(3, nid);

            stmt.executeUpdate();

        } catch (Throwable e) {

            throw new RuntimeException(e.getMessage());
        }finally {
            connection.close();
        }

        return Boolean.TRUE;
    }

    @Override
    public List<Integer> findAllNid() {

        Connection connection = null;
        PreparedStatement stmt = null;

        List<Integer> lists = new ArrayList<>();
        try {

            connection = dataSource.getConnection();
            String selectSqlStr = "select nid from thread_instance";

            stmt = connection.prepareStatement(selectSqlStr);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                lists.add(resultSet.getInt("nid"));
            }

            return lists;
        } catch (SQLException e) {

            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void create(int nid, long threadId) {

        Connection connection = null;
        PreparedStatement stmt = null;

        List<Integer> lists = new ArrayList<>();
        try {

            connection = dataSource.getConnection();
            String selectSqlStr = "insert into thread_instance(nid, pid) values (?,?)";

            stmt = connection.prepareStatement(selectSqlStr);

            stmt.setInt(1, nid);
            stmt.setLong(2, threadId);

            stmt.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<TaskQueue> peaks(List<Integer> statuses, List<Integer> threadNids, int priority) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {

            connection = dataSource.getConnection();
            StringBuilder selectSqlStr = new StringBuilder();
            selectSqlStr.append("select * from task_queue where status in (");

            for (int i = 0; i < statuses.size(); i++) {

                if (i == statuses.size() - 1) {

                    selectSqlStr.append("?");
                } else {
                    selectSqlStr.append("?,");
                }
            }

            selectSqlStr.append(") and threadNid in (");

            for (int i = 0; i < threadNids.size(); i++) {

                if (i == threadNids.size() - 1) {

                    selectSqlStr.append("?");
                } else {
                    selectSqlStr.append("?,");
                }
            }

            selectSqlStr.append(") and priority = ? limit 30");

            stmt = connection.prepareStatement(selectSqlStr.toString());

            int index = 1;

            for (int i = 0; i < statuses.size(); i++) {
                stmt.setInt(index++, statuses.get(i));
            }

            for (int i = 0; i < threadNids.size(); i++) {
                stmt.setInt(index++, threadNids.get(i));
            }

            stmt.setInt(index++, priority);

            ResultSet resultSet = stmt.executeQuery();

            List<TaskQueue> taskQueues = new ArrayList<>();

            while (resultSet.next()) {

                TaskQueue taskQueue = new TaskQueue();

                taskQueue.set_id(resultSet.getString("_id"));
                taskQueue.setName(resultSet.getString("name"));
                taskQueue.setParams(resultSet.getString("params"));
                taskQueue.setStatus(resultSet.getInt("status"));
                taskQueue.setRetry(resultSet.getInt("retry"));
                taskQueue.setThreadNid(resultSet.getInt("threadNid"));
                taskQueue.setPriority(resultSet.getInt("priority"));

                taskQueues.add(taskQueue);
            }

            return taskQueues;
        } catch (SQLException e) {

            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int execing(List<String> _ids, int threadNid) {


        if(_ids.size() == 0){
            return 0;
        }
        Connection connection = null;
        PreparedStatement stmt = null;

        try {

            connection = dataSource.getConnection();
            StringBuilder selectSqlStr = new StringBuilder();
            selectSqlStr.append("update task_queue set threadNid = ?, status = 1 where threadNid in (0, ?) and _id in (");

            for (int i = 0; i < _ids.size(); i++) {

                if (i == _ids.size() - 1) {

                    selectSqlStr.append("?");
                } else {
                    selectSqlStr.append("?,");
                }
            }

            selectSqlStr.append(")");

            stmt = connection.prepareStatement(selectSqlStr.toString());

            int index = 1;
            stmt.setInt(index++, threadNid);
            stmt.setInt(index++, threadNid);

            for (int i = 0; i < _ids.size(); i++) {
                stmt.setString(index++, _ids.get(i));
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void updateTaskStatus(String _id, TaskStatusEnums taskStatusEnums) {

        Connection connection = null;
        PreparedStatement stmt = null;

        try {

            connection = dataSource.getConnection();
            StringBuilder selectSqlStr = new StringBuilder();
            selectSqlStr.append("update task_queue set status = "+ taskStatusEnums.getStatus() +" where _id = ?");

            stmt = connection.prepareStatement(selectSqlStr.toString());

            stmt.setString(1, _id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}
