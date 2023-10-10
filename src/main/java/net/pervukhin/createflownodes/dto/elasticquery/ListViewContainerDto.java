package net.pervukhin.createflownodes.dto.elasticquery;

import lombok.Data;

@Data
public class ListViewContainerDto {
    private ListViewElementDto _source;
    private String _routing;

    public ListViewContainerDto() {
    }

    public ListViewContainerDto(ListViewElementDto _source, String _routing) {
        this._source = _source;
        this._routing = _routing;
    }
}
