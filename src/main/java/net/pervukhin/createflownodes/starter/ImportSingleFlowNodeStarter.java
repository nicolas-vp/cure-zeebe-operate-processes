package net.pervukhin.createflownodes.starter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.pervukhin.createflownodes.dto.elasticquery.ResultContainerDto;
import net.pervukhin.createflownodes.service.ZeebeRecordProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@ConditionalOnProperty(value = "app.import-single-flow-node", havingValue = "true", matchIfMissing = false)
@Service
public class ImportSingleFlowNodeStarter {
    @Value("${elasticsearch.get-flow-node-single}")
    private String url;

    @Value("${app.import-single-flow-node-id}")
    private Long nodeId;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ZeebeRecordProcessInstanceService zeebeRecordProcessInstanceService;

    private static final List<String> NEW_INTENTS = List.of("ELEMENT_ACTIVATED");

    @PostConstruct
    public void init() {
        importFlowNode(nodeId);
    }

    private void importFlowNode(Long id) {
        log.info("Импорт одно элемента инстанса: {}", id);
        ResultContainerDto result = restTemplate.getForObject(url, ResultContainerDto.class, id);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            if (result.getHits().getHits().size() > 0) {
                log.info("Нужный набор элементов найден");
                zeebeRecordProcessInstanceService.checkListViewElement(result.getHits().getHits().get(0).get_source());
                zeebeRecordProcessInstanceService.createFlowNodeInstance(
                        NEW_INTENTS.contains(result.getHits().getHits().get(0).get_source().getIntent())
                                ? zeebeRecordProcessInstanceService
                                .mapTargetFlowNodeViewNew(result.getHits().getHits().get(0).get_source())
                                : zeebeRecordProcessInstanceService
                                        .mapTargetFlowNodeComplete(result.getHits().getHits().get(0).get_source()));
                var resultValue = result.getHits().getHits().get(0).get_source().getValue();

                if (resultValue != null && resultValue.getFlowScopeKey() != null && ! Long.valueOf(-1).equals(resultValue.getFlowScopeKey())
                        && ! resultValue.getFlowScopeKey().equals(result.getHits().getHits().get(0).get_source().getKey())) {
                    log.info("Необходим импорт родителя: {}", resultValue.getFlowScopeKey());
                    importFlowNode(resultValue.getFlowScopeKey());
                }
            } else {
                log.info("Elasiс вернул пустой массив, будет создан элемент заглушка");
                zeebeRecordProcessInstanceService.createBlankFlowNode(id);
                zeebeRecordProcessInstanceService.createFlowNodeInstance(
                        zeebeRecordProcessInstanceService
                                .mapTargetFlowNodeViewNew(
                                        zeebeRecordProcessInstanceService.createBlankFlowNodeQueryResult(id)));
            }
        } else {
            log.info("Поиск был некорректным, проверьте url для поиска: {}", url);
        }
    }
}
