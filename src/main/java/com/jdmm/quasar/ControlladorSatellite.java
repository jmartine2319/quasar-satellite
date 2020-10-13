package com.jdmm.quasar;

import com.google.gson.Gson;
import com.jdmm.quasar.dto.ResultadoSatellitesOut;
import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.logica.LogicaSatellite;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

import org.apache.log4j.BasicConfigurator;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ControlladorSatellite {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("topsecretsplit")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "topsecretsplit",
                methods = {HttpMethod.GET, HttpMethod.POST},
                route="topsecret_split/{satellite_name}",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
                @BindingName("satellite_name")String nombreSatellite,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        BasicConfigurator.configure();
        ResultadoSatellitesOut resultadoSatellite = new ResultadoSatellitesOut();
        try {
	        final String query = request.getQueryParameters().get("name");
	        final String satelliteString = request.getBody().orElse(query);
	        Gson gson = new Gson();
	        SatelliteIn satellites = gson.fromJson(satelliteString, SatelliteIn.class);
	        LogicaSatellite logicaSatellite = new LogicaSatellite();
	        
	        if (nombreSatellite!=null && !nombreSatellite.equals("")) {
	        	context.getLogger().info("Se recibio el parametro "+nombreSatellite);
				ResultadoUbicacion resultado = logicaSatellite.calcularDistancia(nombreSatellite,satellites);
				if(resultado.isExitoso() && resultado.isExitoso()) {
					resultado.setExitoso(true);
					resultado.setPosicionX(resultado.getPosicionX());
					resultado.setPosicionY(resultado.getPosicionY());
					resultado.setMensaje(resultado.getMensaje());
				}else {
					if(!resultado.isExitoso()) {
						throw new ExcepcionQasar("Ocurrio un error al descifrar los mensajes");
					}
					if(!resultado.isExitoso()) {
						throw new ExcepcionQasar("Ocurrio un error al calcular la ubicación");
						
					}
				}
				
	        	return request.createResponseBuilder(HttpStatus.OK).body(resultado).build();
	        } else {
	        	return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("La información no es suficiente para procesar la información").build();
	        }
        } catch (ExcepcionQasar e) {
			e.printStackTrace();
			return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("La información no es suficiente para procesar la información").build();
		}
    }
}
