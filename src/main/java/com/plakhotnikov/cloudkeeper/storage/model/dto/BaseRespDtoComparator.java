package com.plakhotnikov.cloudkeeper.storage.model.dto;

import java.util.Comparator;

public class BaseRespDtoComparator implements Comparator<BaseRespDto> {
    @Override
    public int compare(BaseRespDto o1, BaseRespDto o2) {
        if (o1.isDir() && !o2.isDir()){
            return -1;
        } else if (!o1.isDir() && o2.isDir()) {
            return 1;
        } else {
            return Comparator.comparing(o -> ((BaseRespDto)o).getObjName().toLowerCase()).compare(o1, o2);
        }
    }
}
