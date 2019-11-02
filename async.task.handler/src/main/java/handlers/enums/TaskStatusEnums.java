package handlers.enums;

public enum TaskStatusEnums {

    DEFAULT(0, "未执行"),
    LOADING(1, "被加载"),
    FAILED(8, "失败"),
    SUCCESS(9, "成功");

    private Integer status;
    private String name;

    TaskStatusEnums(Integer status, String name) {
        this.status = status;
        this.name = name;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
