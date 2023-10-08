package net.pervukhin.createflownodes.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName="parents")
public class ModelDto {

    @JacksonXmlProperty(localName = "process")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ProcessDto> process;

    public List<ProcessDto> getProcess() {
        return process;
    }

    public void setProcess(List<ProcessDto> process) {
        this.process = process;
    }
}
