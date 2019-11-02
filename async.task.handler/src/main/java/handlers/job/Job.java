package handlers.job;

import handlers.enums.TaskStatusEnums;

public interface Job {
  TaskStatusEnums exec(String param);
}
