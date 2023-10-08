package net.pervukhin.createflownodes.dto;

import java.util.List;

public class ResultDto {
    private String id;
    private Long key;
    private Integer partitionId;
    private String name;
    private Integer version;
    private String bpmnProcessId;
    private String bpmnXml;
    private String resourceName;
    private List<FlowNode> flowNodes;

    public ResultDto() {
    }

    public ResultDto(String id, Long key, Integer partitionId, String name, Integer version, String bpmnProcessId,
                     String bpmnXml, String resourceName, List<FlowNode> flowNodes) {
        this.id = id;
        this.key = key;
        this.partitionId = partitionId;
        this.name = name;
        this.version = version;
        this.bpmnProcessId = bpmnProcessId;
        this.bpmnXml = bpmnXml;
        this.resourceName = resourceName;
        this.flowNodes = flowNodes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<FlowNode> getFlowNodes() {
        return flowNodes;
    }

    public void setFlowNodes(List<FlowNode> flowNodes) {
        this.flowNodes = flowNodes;
    }
}