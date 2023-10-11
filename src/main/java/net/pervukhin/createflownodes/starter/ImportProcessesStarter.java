package net.pervukhin.createflownodes.starter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import net.pervukhin.createflownodes.dto.FlowNode;
import net.pervukhin.createflownodes.dto.ResultDto;
import net.pervukhin.createflownodes.dto.SourceProcessDto;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(value = "app.import-process", havingValue = "true")
@Service
public class ImportProcessesStarter {
    private static final Logger logger = LoggerFactory.getLogger(ImportProcessesStarter.class);

    @Value("${app.folder}")
    private String folderToScan;

    @Value("${app.partition}")
    private Integer partition;

    @Value("${elasticsearch.create-process}")
    private String elasticUrl;

    @Autowired
    private XmlMapper xmlMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    private final List<String> nodeTypes = List.of("exclusiveGateway", "serviceTask", "userTask", "parallelGateway",
            "callActivity", "startEvent", "boundaryEvent", "intermediateCatchEvent", "endEvent");

    @PostConstruct
    public void init() throws IOException {
        File actual = new File(folderToScan);
        for( File f : actual.listFiles()){
            logger.info("Parsing: {}", f.getAbsolutePath());
            getFile(f.getAbsolutePath());
        }
    }

    public void getFile(String file) throws IOException {
        String content = FileUtils.readFileToString(new File(file), StandardCharsets.UTF_8);
        SourceProcessDto sourceProcessDto = objectMapper.readValue(content, SourceProcessDto.class);

        var parsedBpmnFile = xmlMapper.readTree(sourceProcessDto.getResource());

        List<FlowNode> flowNodes = new ArrayList<>();
        checkProcess(flowNodes, parsedBpmnFile.get("process"));

        ResultDto resultDto = new ResultDto(
                sourceProcessDto.getProcessDefinitionKey(),
                Long.parseLong(sourceProcessDto.getProcessDefinitionKey()),
                partition,
                ! "null".equals(getProcessName(parsedBpmnFile.get("process")))
                        ? getProcessName(parsedBpmnFile.get("process")) : sourceProcessDto.getBpmnProcessId(),
                sourceProcessDto.getVersion(),
                sourceProcessDto.getBpmnProcessId(),
                sourceProcessDto.getResource(),
                sourceProcessDto.getResourceName(),
                flowNodes
        );

        postToElastic(resultDto);
        logger.info("Process {} deployed", sourceProcessDto.getBpmnProcessId());
    }

    private String getProcessName(JsonNode process) {
        return String.valueOf(process.get("name"));
    }

    private void postToElastic(ResultDto resultDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity(resultDto, headers);

        try {
            restTemplate.exchange(elasticUrl, HttpMethod.POST, requestEntity, Object.class, resultDto.getId());
        }catch (HttpClientErrorException e) {
            logger.error("{}",e.getResponseBodyAsString());
        }
    }

    private void checkProcess(List<FlowNode> flowNodes, JsonNode node) {
        if (node != null) {
            if (node.getClass().equals(com.fasterxml.jackson.databind.node.ArrayNode.class)) {
                for (var nodeItem : node) {
                    checkProcess(flowNodes, nodeItem);
                }
            } else {
                for (String nodeType : nodeTypes) {
                    chekNodeItem(flowNodes, node.get(nodeType));
                }
                checkProcess(flowNodes, node.get("subProcess"));
            }
        }
    }

    private void chekNodeItem(List<FlowNode> flowNodes, JsonNode node) {
        if (node != null) {
            if (node.getClass().equals(com.fasterxml.jackson.databind.node.ArrayNode.class)) {
                for (JsonNode jsonNode : node) {
                    chekNodeItem(flowNodes, jsonNode);
                }
            } else {
                flowNodes.add(exportActivity(node));
            }
        }
    }

    private FlowNode exportActivity(JsonNode jsonNode) {
        String id = jsonNode.get("id") != null ? jsonNode.get("id").textValue() : null;
        String name = jsonNode.get("name") != null ? jsonNode.get("name").textValue() : null;
        return new FlowNode(id, name);
    }
}
