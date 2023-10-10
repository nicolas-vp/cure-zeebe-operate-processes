package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class JoinRelationDto {
    private String name;
    private Long parent;
    //private String routing;

    public JoinRelationDto() {
    }

    public JoinRelationDto(String name) {
        this.name = name;
    }

    public JoinRelationDto(String name, Long parent) {
        this.name = name;
        this.parent = parent;
        //this.routing = "processInstanceId";
    }
}
