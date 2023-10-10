package net.pervukhin.createflownodes.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.pervukhin.createflownodes.dto.elasticquery.JoinRelationDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewElementDto;
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

@Slf4j
@ConditionalOnProperty(value = "app.import-single-flow-node", havingValue = "true", matchIfMissing = false)
@Service
public class ImportSingleFlowNode {
    @Value("${elasticsearch.get-flow-node-single}")
    private String url;

    @Value("${app.import-single-flow-node-id}")
    private Long id;

    @Value("${elasticsearch.create-list-view}")
    private String createListView;

    @Value("${app.partition}")
    private Long partition;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        log.info("Импорт одно элемента инстанса: {}", id);
        ResultContainerDto result = restTemplate.getForObject(url, ResultContainerDto.class, id);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            if (result.getHits().getHits().size() > 0) {
                checkListViewElement(result.getHits().getHits().get(0).get_source());
            } else {
                log.info("Elasiс вернул пустой массив");
            }
        } else {
            log.info("Поиск был некорректным, проверьте url для поиска: {}", url);
        }
    }

    private void checkListViewElement(QueryResultDto source) {
        if (getProcessInstance(source.getId()) == null) {
            createListViewItem(mapTargetListViewRelation(source));
        }
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
}
