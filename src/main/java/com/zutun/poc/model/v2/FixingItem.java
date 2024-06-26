package com.zutun.poc.model.v2;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixingItem {
    List<Item> items;
    Restriction restriction;
}
