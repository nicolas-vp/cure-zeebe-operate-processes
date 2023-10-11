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
import java.util.concurrent.atomic.AtomicInteger;

@ConditionalOnProperty(value = "app.import-list-view", havingValue = "true")
@Service
@Slf4j
public class ImportAllProcessInstanceStarter {
    private static final List<String> TERMINAL_INTENTS = List.of("ELEMENT_ACTIVATED", "ELEMENT_COMPLETED",
            "ELEMENT_TERMINATED");
    private static final List<String> NEW_INTENTS = List.of("ELEMENT_ACTIVATED");

    @Value("${elasticsearch.get-process-instance}")
    private String getProcessInstance;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ZeebeRecordProcessInstanceService zeebeRecordProcessInstanceService;

    @PostConstruct
    public void init() {
        ResultContainerDto result = restTemplate.getForObject(getProcessInstance, ResultContainerDto.class);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            int targetQuantity = result.getHits().getHits().size();
            AtomicInteger count = new AtomicInteger();
            log.info("Извлечено {} записей", targetQuantity);
            result.getHits().getHits().forEach(item -> {
                if (item.get_source() != null && item.get_source().getValue() != null
                        && TERMINAL_INTENTS.contains(item.get_source().getIntent()) ) {
                    if ("PROCESS".equals(item.get_source().getValue().getBpmnElementType())) {
                        zeebeRecordProcessInstanceService.createListViewItem(
                                NEW_INTENTS.contains(item.get_source().getIntent())
                                        ? zeebeRecordProcessInstanceService.mapTargetListViewNew(item.get_source())
                                        : zeebeRecordProcessInstanceService.mapTargetListViewComplete(item.get_source()));
                        log.info("Выгружен инстанс {} {}/{}", item.get_source() != null
                                ? item.get_source().getValue().getProcessInstanceKey()
                                : "", count, targetQuantity);
                    } else {
                        if (item.get_source().getValue().getElementId() != null) {
                            zeebeRecordProcessInstanceService.checkProcess(item.get_source());
                            zeebeRecordProcessInstanceService.checkListViewElement(item.get_source());
                            zeebeRecordProcessInstanceService.createFlowNodeInstance(
                                    NEW_INTENTS.contains(item.get_source().getIntent())
                                            ? zeebeRecordProcessInstanceService.mapTargetFlowNodeViewNew(item.get_source())
                                            : zeebeRecordProcessInstanceService.mapTargetFlowNodeComplete(item.get_source()));
                            log.info("Выгружен элемент {} {}/{}", item.get_source() != null
                                    ? item.get_source().getKey()
                                    : "", count, targetQuantity);
                        }
                    }
                }
                count.getAndIncrement();
            });
        } else {
            log.info("Нет данных из zeebe-record");
        }
    }
}
