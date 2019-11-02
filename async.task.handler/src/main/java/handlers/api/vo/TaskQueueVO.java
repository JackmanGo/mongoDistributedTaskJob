package handlers.api.vo;

import java.util.Objects;

public class TaskQueueVO {

    private String _id;
    private String name;
    private String params = "";
    private Integer priority = 0;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskQueueVO that = (TaskQueueVO) o;
        return Objects.equals(_id, that._id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(params, that.params) &&
                Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_id, name, params, priority);
    }
}
