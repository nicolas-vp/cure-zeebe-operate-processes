package net.pervukhin.createflownodes.dto;

public class FlowNode {
    private String id;
    private String name;

    public FlowNode() {
    }

    public FlowNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
