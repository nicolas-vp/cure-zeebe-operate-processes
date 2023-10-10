package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class QueryResultDto {
    private Long id;
    private Long key;
    private Long timestamp;
    private Long position;
    private String valueType;
    private String recordType;
    private String intent;
    private Long sequence;
    private ValueDto value;
}
