package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class League {
    private long id;
    private String name;
    private int weight;
    private int prematch;
    private int inplay;
    private int outright;
    private boolean top;
    private int topOrder;
}
