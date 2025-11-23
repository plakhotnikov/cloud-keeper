package com.plakhotnikov.cloudkeeper.storage.model;


import lombok.Getter;
import org.springframework.util.unit.DataSize;

@Getter
public class StorageInfo {
    private static final String GREEN_STYLE = "bg-success";
    private static final String YELLOW_STYLE = "bg-warning";
    private static final String RED_STYLE = "bg-danger";

    private final long usedMb;
    private final long capacityMb;
    private final int usedPercentage;
    private final String colorStyle;

    public StorageInfo(DataSize used, DataSize capacity) {
        this.usedMb = used.toMegabytes();
        this.capacityMb = capacity.toMegabytes();
        this.usedPercentage = (int) Math.floorDiv(usedMb * 100, capacityMb);
        this.colorStyle = defineColorStyle(usedPercentage);
    }

    private String defineColorStyle(int percentage){
        if (percentage < 50) {
            return GREEN_STYLE;
        } else if (percentage < 90){
            return YELLOW_STYLE;
        } else {
            return RED_STYLE;
        }
    }
}
