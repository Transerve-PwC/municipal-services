package org.egov.assets.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialStoreMappingSearch {

    private List<String> ids;

    private String material;

    private String store;

    private Boolean active;

    private String glCode;

    private String sortBy;

    private Integer pageSize;

    private Integer offset;

    @NotNull
    private String tenantId;
}
