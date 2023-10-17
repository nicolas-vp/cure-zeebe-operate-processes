package net.pervukhin.createflownodes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.pervukhin.createflownodes.dto.ProcessDto;
import net.pervukhin.createflownodes.dto.elasticquery.FlowNodeInstanceContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.FlowNodeInstanceElementDto;
import net.pervukhin.createflownodes.dto.elasticquery.JoinRelationDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewElementDto;
import net.pervukhin.createflownodes.dto.elasticquery.ProcessContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.QueryResultDto;
import net.pervukhin.createflownodes.dto.elasticquery.ValueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.TimeZone;

@Service
@Slf4j
public class ZeebeRecordProcessInstanceService {

    private static final JoinRelationDto JOIN_RELATION_DTO = new JoinRelationDto("processInstance");

    @Value("${elasticsearch.create-list-view}")
    private String createListView;

    @Value("${elasticsearch.create-flow-node}")
    private String createFlowNode;

    @Value("${app.partition}")
    private Long partition;

    @Value("${elasticsearch.create-process}")
    private String getProcessDefinition;

    @Value("${app.import-single-instance-id}")
    private Long singleInstanceId;

    @Autowired
    private RestTemplate restTemplate;

    public void checkListViewElement(QueryResultDto source) {
        if (source.getId() != null && getProcessInstance(source.getId()) == null) {
            createListViewItem(mapTargetListViewRelation(source));
        } else {
            log.info("Получение ID было пустым");
        }
    }

    public void checkProcess(QueryResultDto source) {
        if (source.getValue().getProcessInstanceKey() != null) {
            if (getProcessInstance(source.getValue().getProcessInstanceKey()) == null) {
                createListViewItem(mapTargetListViewNew(source));
                log.info("Восстановлен инстанс {}", source.getValue().getProcessInstanceKey());
            }
        }
        if (source.getValue().getParentElementInstanceKey() != null && source.getValue().getParentElementInstanceKey() != -1) {
            if (getProcessInstance(source.getValue().getParentProcessInstanceKey()) == null) {
                createListViewItem(mapParentListViewNew(source));
                log.info("Нужно создать пустой родительский инстанс {}", source.getValue().getParentProcessInstanceKey());
            }
        }
    }

    public void createBlankFlowNode(Long flowNodeElementId) {
        QueryResultDto source = new QueryResultDto();
        source.setTimestamp(Instant.now().toEpochMilli());
        source.setKey(flowNodeElementId);
        ValueDto valueDto = new ValueDto();
        valueDto.setProcessInstanceKey(singleInstanceId);
        valueDto.setElementId("Activity_");
        valueDto.setBpmnEventType("CALL_ACTIVITY");
        source.setValue(valueDto);
        createListViewItem(mapTargetListViewRelation(source));
    }

    public void createBlankProcess(Long processInstanceKey) {
        QueryResultDto queryResultDto = new QueryResultDto();
        ValueDto valueDto = new ValueDto();
        valueDto.setParentProcessInstanceKey(processInstanceKey);
        queryResultDto.setValue(valueDto);
        queryResultDto.setTimestamp(Instant.now().toEpochMilli());
        createListViewItem(mapParentListViewNew(queryResultDto));
    }

    private ListViewElementDto getProcessInstance(Long processInstanceId) {
        try {
            ListViewContainerDto dto = restTemplate.getForObject(createListView, ListViewContainerDto.class,
                    processInstanceId);
            return dto != null ? dto.get_source() : null;
        } catch (Exception ex) {
            log.info("НЕТ инстанса процесса в базе: {}", processInstanceId);
            return null;
        }
    }

    public void createFlowNodeInstance(FlowNodeInstanceElementDto target) {
        if (target != null && target.getId() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity(target, headers);

            try {
                restTemplate.exchange(createFlowNode, HttpMethod.POST, requestEntity, Object.class, target.getId());
            } catch (HttpClientErrorException e) {
                log.error("{}", e.getResponseBodyAsString());
            }
        } else {
            log.info("Попытка записи пустого элемента");
        }
    }

    public void createListViewItem(ListViewElementDto target) {
        if (target != null && target.getId() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity(target, headers);

            try {
                restTemplate.exchange(createListView+"?routing=" + target.getProcessInstanceKey(), HttpMethod.POST, requestEntity, Object.class, target.getId());
            } catch (HttpClientErrorException e) {
                log.error("{}", e.getResponseBodyAsString());
            }
        } else {
            log.info("Попытка записи пустого инстанса");
        }
    }

    public ListViewElementDto mapTargetListViewNew(QueryResultDto source) {
        ListViewElementDto target = new ListViewElementDto();
        Long instanceKey = source.getValue().getProcessInstanceKey();

        target.setId(String.valueOf(instanceKey));
        target.setKey(instanceKey);
        target.setPartitionId(partition);
        target.setProcessDefinitionKey(source.getValue().getProcessDefinitionKey());
        ProcessDto processDto = getProcess(source.getValue().getProcessDefinitionKey());
        target.setProcessName(processDto.getName());
        target.setProcessVersion(source.getValue().getVersion());
        target.setBpmnProcessId(source.getValue().getBpmnProcessId());
        target.setStartDate(parseDate(source.getTimestamp()));
        target.setState("ACTIVE");
        target.setTreePath("PI_" + instanceKey);
        target.setIncident(false);
        target.setProcessInstanceKey(instanceKey);
        target.setJoinRelation(JOIN_RELATION_DTO);
        target.setParentFlowNodeInstanceKey(source.getValue().getParentElementInstanceKey());
        target.setParentProcessInstanceKey(source.getValue().getParentProcessInstanceKey());
        return target;
    }

    private ListViewElementDto mapTargetListViewRelation(QueryResultDto source) {
        ListViewElementDto target = new ListViewElementDto();
        target.setKey(source.getKey());
        target.setId(String.valueOf(source.getKey()));
        target.setPartitionId(partition);
        target.setProcessInstanceKey(source.getValue().getProcessInstanceKey());
        target.setActivityId(source.getValue().getElementId());
        target.setActivityState("ACTIVE");
        target.setActivityType(source.getValue().getBpmnElementType());
        target.setIncident(false);
        target.setJoinRelation(new JoinRelationDto(
                "activity",
                source.getValue().getProcessInstanceKey()));
        return target;
    }

    private ListViewElementDto mapParentListViewNew(QueryResultDto source) {
        ListViewElementDto target = new ListViewElementDto();
        Long instanceKey = source.getValue().getParentProcessInstanceKey();

        target.setId(String.valueOf(instanceKey));
        target.setKey(instanceKey);
        target.setPartitionId(partition);
        target.setProcessDefinitionKey(0L);
        target.setProcessName("unknown");
        target.setProcessVersion(1L);
        target.setBpmnProcessId("unknown.bpmn");
        target.setStartDate(parseDate(source.getTimestamp()));
        target.setState("ACTIVE");
        target.setTreePath("PI_" + instanceKey);
        target.setIncident(false);
        target.setProcessInstanceKey(instanceKey);
        target.setJoinRelation(JOIN_RELATION_DTO);
        target.setParentFlowNodeInstanceKey(-1L);
        target.setParentProcessInstanceKey(-1L);
        return target;
    }

    public FlowNodeInstanceElementDto mapTargetFlowNodeViewNew(QueryResultDto source) {
        FlowNodeInstanceElementDto target = new FlowNodeInstanceElementDto();
        Long instanceKey = source.getValue().getProcessInstanceKey();

        target.setId(source.getKey());
        target.setKey(source.getKey());
        target.setPartitionId(partition);
        target.setProcessDefinitionKey(source.getValue().getProcessDefinitionKey());
        target.setFlowNodeId(source.getValue().getElementId());
        target.setType(source.getValue().getBpmnElementType());
        target.setBpmnProcessId(source.getValue().getBpmnProcessId());
        target.setStartDate(parseDate(source.getTimestamp()));
        target.setState("ACTIVE");
        target.setTreePath(source.getValue().getProcessInstanceKey() + "/" + source.getKey());
        target.setIncident(false);
        target.setProcessInstanceKey(instanceKey);
        target.setLevel(1);
        target.setPosition(source.getPosition());
        return target;
    }

    public FlowNodeInstanceElementDto mapTargetFlowNodeComplete(QueryResultDto source) {
        try {
            FlowNodeInstanceElementDto dto = getFlowNodeElement(source.getKey());
            if (dto == null) {
                dto = mapTargetFlowNodeViewNew(source);
            }
            dto.setEndDate(parseDate(source.getTimestamp()));
            dto.setState("COMPLETED");
            return dto;
        } catch (Exception ex) {
            log.error("Проблемы получения элемента: {} инстанса {} ", source.getKey(), source.getValue().getBpmnProcessId());
        }
        return null;
    }

    private FlowNodeInstanceElementDto getFlowNodeElement(Long id) {
        try {
            FlowNodeInstanceContainerDto dto = restTemplate.getForObject(createFlowNode, FlowNodeInstanceContainerDto.class,
                    id);
            return dto != null ? dto.get_source() : null;
        } catch (Exception ex) {
            log.info("НЕТ элемента в базе: {}", id);
            return null;
        }
    }

    public ListViewElementDto mapTargetListViewComplete(QueryResultDto sourceDto) {
        try {
            ListViewContainerDto dto = restTemplate.getForObject(createListView, ListViewContainerDto.class,
                    sourceDto.getKey());
            if (dto != null && dto.get_source() != null) {
                dto.get_source().setEndDate(parseDate(sourceDto.getTimestamp()));
                dto.get_source().setState("COMPLETED");
                return dto.get_source();
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.error("Проблемы получения инстанса: " + sourceDto.getKey());
        }
        return null;
    }

    private String parseDate(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                        .getDefault().toZoneId()) + "+0000";
    }

    private ProcessDto getProcess(Long processDefinitionId) {
        try {
            ProcessContainerDto dto = restTemplate.getForObject(getProcessDefinition, ProcessContainerDto.class,
                    processDefinitionId);
            if (dto != null) {
                return dto.get_source();
            }
        } catch (Exception ex) {
            log.error("Процесс не найден: {} ", ex);
        }
        return new ProcessDto();
    }
}
