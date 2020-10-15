package com.jdmm.quasar.dto;

import java.util.List;

import lombok.Data;

@Data
public class SatellitInSimple {
	private Double distance;
	private List<String> message;

}
