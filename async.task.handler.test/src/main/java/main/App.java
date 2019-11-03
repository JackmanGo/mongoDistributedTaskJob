package main;


import com.alibaba.druid.pool.DruidDataSource;
import handlers.api.TaskHandler;
import handlers.api.vo.TaskQueueVO;
import handlers.repository.JdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import java.util.UUID;

@SpringBootApplication
@ImportResource(value = {"classpath:task.xml"})
public class App {

    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(App.class, args);

        /**
         * 加载任务执行器
         */
        TaskHandler task = ctx.getBean(TaskHandler.class);
        //生成测试Task
        //testData(task);

        task.startThreadPool(5);
    }


    /**
     * @Bean public MongoDBRepository getMongoTemplate() {
     * <p>
     * MongoDBRepository mongo = new MongoDBRepository();
     * MongoClientOptions.Builder builder = MongoClientOptions.builder();
     * // 设定连接属性
     * builder.connectionsPerHost(8);
     * builder.threadsAllowedToBlockForConnectionMultiplier(4);
     * builder.socketTimeout(1000 * 5);
     * builder.maxWaitTime(1000 * 3);
     * builder.socketKeepAlive(true);
     * builder.socketTimeout(1500);
     * MongoClient client;
     * <p>
     * if (mongoTemplate != null) {
     * <p>
     * return mongo;
     * }
     * <p>
     * try {
     * <p>
     * client = new MongoClient("127.0.0.1", builder.build());
     * mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(client, "async_task_handler"));
     * return mongo;
     * } catch (Exception e) {
     * throw new RuntimeException(e.getMessage());
     * }
     * }
     **/


    @Bean
    public JdbcRepository getDataSource() {
        JdbcRepository repository = new JdbcRepository();
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/async_task_handler");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("toor");
        druidDataSource.setMaxActive(100);
        repository.setDataSource(druidDataSource);
        return repository;
    }


    @Bean("jobClassName")
    public String[] jobImpl() {
        return new String[]{"job.jobImpl.JobOne", "job.jobImpl.JobTwo"};
    }

    /**
     * 向数据库中添加要处理的模拟任务
     * 对于JobOne和JobTwo各添加100个
     */
    public static void testData(TaskHandler taskHandler) {

        for (int i = 0; i < 50; i++) {

            TaskQueueVO vo = new TaskQueueVO();
            vo.set_id(UUID.randomUUID().toString());
            vo.setName("JobOne");
            vo.setParams(i + "");
            vo.setPriority(1);

            taskHandler.insertAsyncTask(vo);
        }
        for (int i = 0; i < 50; i++) {

            TaskQueueVO vo = new TaskQueueVO();
            vo.set_id(UUID.randomUUID().toString());
            vo.setName("JobTwo");
            vo.setParams(i + "");
            vo.setPriority(1);

            taskHandler.insertAsyncTask(vo);
        }


    }
}
