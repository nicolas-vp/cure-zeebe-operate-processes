package net.pervukhin.createflownodes.starter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.pervukhin.createflownodes.dto.ProcessDto;
import net.pervukhin.createflownodes.dto.elasticquery.JoinRelationDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.ListViewElementDto;
import net.pervukhin.createflownodes.dto.elasticquery.ProcessContainerDto;
import net.pervukhin.createflownodes.dto.elasticquery.QueryResultDto;
import net.pervukhin.createflownodes.dto.elasticquery.ResultContainerDto;
import net.pervukhin.createflownodes.service.ZeebeRecordProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ImportSingleInstanceStarter {
    @Value("${elasticsearch.get-process-single-instance}")
    private String url;

    @Value("${app.import-single-instance-id}")
    private Long id;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ZeebeRecordProcessInstanceService zeebeRecordProcessInstanceService;

    @PostConstruct
    public void init() {
        log.info("Импорт одно инстанса: {}", id);
        ResultContainerDto result = restTemplate.getForObject(url, ResultContainerDto.class, id);
        if (result != null && result.getHits() != null && result.getHits().getHits() != null) {
            if (result.getHits().getHits().size() > 0) {
                zeebeRecordProcessInstanceService.checkProcess(result.getHits().getHits().get(0).get_source());
            } else {
                log.info("Elastiс вернул пустой массив");
            }
        } else {
            log.info("Поиск был некорректным, проверьте url для поиска: {}", url);
        }
    }
}
