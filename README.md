###异步任务处理器中间件
####实现功能：

解耦耗时操作，异步处理

####依赖组件：

**mongodb或mysql**
**JDK1.8**

数据库设计 taskQueue.sql

```
CREATE TABLE `task_queue` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '异步任务名称',
  `params` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '异步任务的参数，用户可将参数序列号或转JSON来存储',
  `status` tinyint(4) DEFAULT '0' COMMENT '当前任务状态：0为初始状态，1为被加载准备执行，8为执行失败，9为执行成功',
  `retry` int(11) NOT NULL DEFAULT '0' COMMENT '当前重试次数',
  `create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  `threadNid` int(11) NOT NULL DEFAULT '0' COMMENT '线程序号',
  `priority` int(11) NOT NULL DEFAULT '1' COMMENT '从1开始，数值越小，被执行的概率越大',
  `_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'uuid',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务表';

ALTER TABLE `task_queue` ADD UNIQUE (`_id`);

CREATE TABLE `thread_instance`(
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `nid` int(11) NOT NULL DEFAULT '0' COMMENT '线程序号，从1开始',
  `pid` int(11) NOT NULL DEFAULT '0' COMMENT '线程在os中的pid',
  `now_counts` int(11) NOT NULL DEFAULT '0' COMMENT '当前正在处理的任务数',
  `finished_counts` int(11) NOT NULL DEFAULT '0' COMMENT '已处理完成的任务数',
  `create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上次更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线程实例表';

```


