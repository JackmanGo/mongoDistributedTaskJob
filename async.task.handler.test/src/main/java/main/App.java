package main;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import handlers.api.TaskHandler;
import handlers.api.vo.TaskQueueVO;
import handlers.repository.JdbcRepository;
import handlers.repository.MongoDBRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.beans.PropertyVetoException;
import java.util.UUID;

@SpringBootApplication
@ImportResource(value = {"classpath:task.xml"})
public class App {

    private static MongoTemplate mongoTemplate;

    public static void main(String[] args) {

        ApplicationContext ctx = SpringApplication.run(App.class, args);
        String[] taskHandler = ctx.getBean(String[].class);

        System.out.println("==============");
        System.out.println(taskHandler);

        TaskHandler task = ctx.getBean(TaskHandler.class);
        testData(task);

        task.startThreadPool(5);
    }


    @Bean
    public MongoDBRepository getMongoTemplate() {

        MongoDBRepository mongo = new MongoDBRepository();
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        // 设定连接属性
        builder.connectionsPerHost(8);
        builder.threadsAllowedToBlockForConnectionMultiplier(4);
        builder.socketTimeout(1000 * 5);
        builder.maxWaitTime(1000 * 3);
        builder.socketKeepAlive(true);
        builder.socketTimeout(1500);
        MongoClient client;

        if (mongoTemplate != null) {

            return mongo;
        }

        try {

            client = new MongoClient("127.0.0.1", builder.build());
            mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(client, "async_task_handler"));
            return mongo;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * @Bean public JdbcRepository getDataSource() {
     * <p>
     * JdbcRepository repository = new JdbcRepository();
     * <p>
     * ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
     * <p>
     * try {
     * <p>
     * comboPooledDataSource.setDriverClass("com.mysql.jdbc.Driver");
     * comboPooledDataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/async_task_handler");
     * comboPooledDataSource.setUser("root");
     * comboPooledDataSource.setPassword("toor");
     * comboPooledDataSource.setInitialPoolSize(10);
     * comboPooledDataSource.setMinPoolSize(10);
     * comboPooledDataSource.setMaxPoolSize(30);
     * comboPooledDataSource.setAcquireIncrement(3);
     * comboPooledDataSource.setMaxIdleTime(1800);
     * comboPooledDataSource.setCheckoutTimeout(300000);
     * } catch (PropertyVetoException e) {
     * e.printStackTrace();
     * }
     * <p>
     * repository.setDataSource(comboPooledDataSource);
     * <p>
     * return repository;
     * }
     **/

    @Bean
    public String[] jobImpl() {
        return new String[]{"job.jobImpl.JobOne", "job.jobImpl.JobTwo"};
    }

    /**
     * 向数据库中添加要处理的任务
     * 对于JobOne和JobTwo各添加10个
     */
    public static void testData(TaskHandler taskHandler) {

        for (int i = 0; i < 100; i++) {

            TaskQueueVO vo = new TaskQueueVO();
            vo.set_id(UUID.randomUUID().toString());
            vo.setName("JobOne");
            vo.setParams(i + "");

            taskHandler.insertAsyncTask(vo);
        }
        for (int i = 0; i < 100; i++) {

            TaskQueueVO vo = new TaskQueueVO();
            vo.set_id(UUID.randomUUID().toString());
            vo.setName("JobTwo");
            vo.setParams(i + "");
            taskHandler.insertAsyncTask(vo);
        }


    }

    //产生数据供task处理
    //@Override
    public void onApplicationEvent() {
    }
}
