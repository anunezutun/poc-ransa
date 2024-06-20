package com.zutun.poc.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resume {
    private List<Assignation> assignations;
    private Integer totalItems;
    private Integer totalAssignations;
    private Integer totalErrorItems;
}
