package net.pervukhin.createflownodes.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.pervukhin.createflownodes.dto.ProcessDto;
import net.pervukhin.createflownodes.dto.elasticquery.JoinRelationDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewElementDto;
import net.pervukhin.createflownodes.dto.elasticquery.ProcessContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.QueryResultDto;
import net.pervukhin.createflownodes.dto.elasticquery.ResultContainerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j
@ConditionalOnProperty(value = "app.import-single-instance", havingValue = "true", matchIfMissing = false)
@Service
public class ImportSingleInstance {
    @Value("${elasticsearch.get-process-single-instance}")
    private String url;

    @Value("${app.import-single-instance-id}")
    private Long id;

    @Value("${elasticsearch.create-list-view}")
    private String createListView;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final JoinRelationDto JOIN_RELATION_DTO = new JoinRelationDto("processInstance");

    @Value("${elasticsearch.create-process}")
    private String getProcessDefinition;

    @Value("${app.partition}")
    private Long partition;

    @PostConstruct
    public void init() {
        log.info("Импорт одно инстанса: {}", id);
        ResultContainerDto result = restTemplate.getForObject(url, ResultContainerDto.class, id);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            if (result.getHits().getHits().size() > 0) {
                checkProcess(result.getHits().getHits().get(0).get_source());
            } else {
                log.info("Elasiс вернул пустой массив");
            }
        } else {
            log.info("Поиск был некорректным, проверьте url для поиска: {}", url);
        }
    }

    private void checkProcess(QueryResultDto source) {
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

    private ListViewElementDto mapTargetListViewNew(QueryResultDto source) {
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

    private String parseDate(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                .getDefault().toZoneId()) + "+0000";
    }

    private ListViewElementDto getProcessInstance(Long processInstanceId) {
        try {
            ListViewContainerDto dto = restTemplate.getForObject(createListView, ListViewContainerDto.class,
                    processInstanceId);
            return dto.get_source();
        } catch (Exception ex) {
            log.info("НЕТ инстанса процесса в базе: {}", processInstanceId);
            return null;
        }
    }

    private void createListViewItem(ListViewElementDto target) {
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
