package com.jdmm.quasar.dto;

import java.util.List;

import lombok.Data;

@Data
public class SatelliteIn {
	private String name;
	private Double distance;
	private List<String> message;

}
