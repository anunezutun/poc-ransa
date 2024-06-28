package com.zutun.poc.model.v2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDto {
    private RequestDto requestDto;
    private FixingItem fixingItem;
    private Resume resume;
    private Boolean optimized;
}
