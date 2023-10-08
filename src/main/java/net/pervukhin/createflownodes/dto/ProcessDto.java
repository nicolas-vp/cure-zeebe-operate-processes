package net.pervukhin.createflownodes.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;


@JacksonXmlRootElement(localName="parents")
public class ProcessDto {
    private String id;
    private String name;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> exclusiveGateway;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> serviceTask;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> userTask;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> parallelGateway;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> callActivity;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> startEvent;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> boundaryEvent;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> intermediateCatchEvent;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<FlowNode> endEvent;

    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ProcessDto> subProcess;

    public List<FlowNode> getExclusiveGateway() {
        return exclusiveGateway;
    }

    public void setExclusiveGateway(List<FlowNode> exclusiveGateway) {
        this.exclusiveGateway = exclusiveGateway;
    }

    public List<FlowNode> getServiceTask() {
        return serviceTask;
    }

    public void setServiceTask(List<FlowNode> serviceTask) {
        this.serviceTask = serviceTask;
    }

    public List<FlowNode> getUserTask() {
        return userTask;
    }

    public void setUserTask(List<FlowNode> userTask) {
        this.userTask = userTask;
    }

    public List<FlowNode> getParallelGateway() {
        return parallelGateway;
    }

    public void setParallelGateway(List<FlowNode> parallelGateway) {
        this.parallelGateway = parallelGateway;
    }

    public List<FlowNode> getCallActivity() {
        return callActivity;
    }

    public void setCallActivity(List<FlowNode> callActivity) {
        this.callActivity = callActivity;
    }

    public List<FlowNode> getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(List<FlowNode> startEvent) {
        this.startEvent = startEvent;
    }

    public List<FlowNode> getBoundaryEvent() {
        return boundaryEvent;
    }

    public void setBoundaryEvent(List<FlowNode> boundaryEvent) {
        this.boundaryEvent = boundaryEvent;
    }

    public List<FlowNode> getIntermediateCatchEvent() {
        return intermediateCatchEvent;
    }

    public void setIntermediateCatchEvent(List<FlowNode> intermediateCatchEvent) {
        this.intermediateCatchEvent = intermediateCatchEvent;
    }

    public List<FlowNode> getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(List<FlowNode> endEvent) {
        this.endEvent = endEvent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<ProcessDto> getSubProcess() {
        return subProcess;
    }

    public void setSubProcess(List<ProcessDto> subProcess) {
        this.subProcess = subProcess;
    }
}
