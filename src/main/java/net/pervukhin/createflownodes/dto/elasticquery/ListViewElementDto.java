package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class ListViewElementDto {
    private String id;
    private Long key;
    private Long partitionId;
    private Long processDefinitionKey;
    private String processName;
    private Long processVersion;
    private String bpmnProcessId;
    private String startDate;
    private String endDate;
    private String state;
    private Long batchOperationIds;
    private Long parentProcessInstanceKey;
    private Long parentFlowNodeInstanceKey;
    private String treePath;
    private Boolean incident;
    private JoinRelationDto joinRelation;
    private Long processInstanceKey;
    private String activityId;
    private String activityState;
    private String activityType;
}


/*

{
  "id": "2251799813685259",
  "key": 2251799813685259,
  "partitionId": 1,
  "processDefinitionKey": 2251799813685256,
  "processName": "Запуск процесса",
  "processVersion": 1,
  "bpmnProcessId": "run-process-mock",
  "startDate": "2023-10-09T18:54:55.640+0000",
  "endDate": "2023-10-09T18:54:55.696+0000",
  "state": "COMPLETED",
  "batchOperationIds": null,
  "parentProcessInstanceKey": null,
  "parentFlowNodeInstanceKey": null,
  "treePath": "PI_2251799813685259",
  "incident": false,
  "joinRelation": {
    "name": "processInstance",
    "parent": null
  },
  "processInstanceKey": 2251799813685259
}


 */