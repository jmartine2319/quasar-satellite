package com.jdmm.quasar.dto;

import lombok.Data;

@Data
public class ResultadoUbicacion {
	private boolean exitoso;
	private Double posicionX;
	private Double posicionY;
	private String mensaje;
}
