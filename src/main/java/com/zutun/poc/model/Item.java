package com.zutun.poc.model;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    private String order;
    private String description;
    private Double depth;
    private Double width;
    private Double height;
    private Double weight;
    private Boolean stackable;
    private Vehicle vehicle;
    private List<String> observations = new ArrayList<>();

    public Double getVolume() {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.UP);
        var volume = this.getDepth() * this.getWidth() * this.getHeight();
        return Double.parseDouble(df.format(volume));
    }
}
