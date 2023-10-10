package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class FlowNodeInstanceElementDto {
    private Long id;
    private Long key;
    private Long partitionId;
    private String flowNodeId;
    private String startDate;
    private String endDate;
    private String state;
    private String type;
    private String incidentKey;
    private Long processInstanceKey;
    private Long processDefinitionKey;
    private String bpmnProcessId;
    private String treePath;
    private Integer level;
    private Long position;
    private Boolean incident;
}