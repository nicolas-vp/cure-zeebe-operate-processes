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

@Slf4j
@ConditionalOnProperty(value = "app.import-single-flow-node", havingValue = "true", matchIfMissing = false)
@Service
public class ImportSingleFlowNodeStarter {
    @Value("${elasticsearch.get-flow-node-single}")
    private String url;

    @Value("${app.import-single-flow-node-id}")
    private Long id;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ZeebeRecordProcessInstanceService zeebeRecordProcessInstanceService;

    @PostConstruct
    public void init() {
        log.info("Импорт одно элемента инстанса: {}", id);
        ResultContainerDto result = restTemplate.getForObject(url, ResultContainerDto.class, id);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            if (result.getHits().getHits().size() > 0) {
                zeebeRecordProcessInstanceService.checkListViewElement(result.getHits().getHits().get(0).get_source());
            } else {
                log.info("Elasiс вернул пустой массив");
            }
        } else {
            log.info("Поиск был некорректным, проверьте url для поиска: {}", url);
        }
    }
}
