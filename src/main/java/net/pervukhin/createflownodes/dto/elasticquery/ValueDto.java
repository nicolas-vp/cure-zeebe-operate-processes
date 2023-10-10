package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class ValueDto {
    private String bpmnProcessId;
    private Long processInstanceKey;
    private Long processDefinitionKey;
    private String elementId;
    private Long flowScopeKey;
    private String bpmnElementType;
    private String bpmnEventType;
    private Long parentProcessInstanceKey;
    private Long parentElementInstanceKey;
    private Long version;
}
