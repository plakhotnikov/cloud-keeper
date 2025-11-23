package com.plakhotnikov.cloudkeeper.storage.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;


@NoArgsConstructor
@Getter
@Setter
public class Breadcrumb {

    private LinkedHashMap<String, String> pathItems;
    private String lastPart;

}
