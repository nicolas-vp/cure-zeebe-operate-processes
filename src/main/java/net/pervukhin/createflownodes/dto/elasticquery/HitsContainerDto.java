package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

import java.util.List;

@Data
public class HitsContainerDto {
    private List<SourceDto> hits;

}
