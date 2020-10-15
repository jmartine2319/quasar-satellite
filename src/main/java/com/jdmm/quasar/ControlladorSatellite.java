package com.jdmm.quasar;

import com.google.gson.Gson;
import com.jdmm.quasar.dto.ResultadoSatellitesOut;
import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.dto.Ubicacion;
import com.jdmm.quasar.logica.LogicaSatellite;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.jdmm.quasar.util.RedisClient;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.ArrayList;
import java.util.List;
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
        context.getLogger().info("Java HTTP trigger topsecretsplit.");
        BasicConfigurator.configure();
        try {
	        final String query = request.getQueryParameters().get("name");
	        final String satelliteString = request.getBody().orElse(query);
	        Gson gson = new Gson();
	        SatelliteIn satellites = gson.fromJson(satelliteString, SatelliteIn.class);
	        satellites.setName(nombreSatellite);
	        RedisClient redisClient = new RedisClient();
	        
	        if (nombreSatellite!=null && !nombreSatellite.equals("")) {
	        	context.getLogger().info("Se recibio el parametro "+nombreSatellite);
	        	int tamano = Integer.parseInt(redisClient.consultarPropiedad("tamanoSatellites"));
	        	if(tamano==0) {
	        		context.getLogger().info("No hay datos almacenados, se va almacenar "+nombreSatellite+" "+satellites.getDistance());
	        		redisClient.guardarPropiedad(nombreSatellite+"-info", satelliteString);
	        		redisClient.guardarPropiedad("tamanoSatellites", tamano+1+"");
	        		redisClient.guardarPropiedad("satellites", nombreSatellite);
	        		return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("La información no es suficiente para procesar la información").build();
	        	}else {
	        		ResultadoSatellitesOut resultadoSatellite = new ResultadoSatellitesOut();
	        		LogicaSatellite logicaSatellite = new LogicaSatellite();
	        		ResultadoUbicacion resultado=logicaSatellite.procesarOperacion(satellites);
	        		redisClient.guardarPropiedad("satellites", nombreSatellite);
	        		redisClient.guardarPropiedad(nombreSatellite+"-info", satelliteString);
	        		Ubicacion ubicacion = new Ubicacion();
	        		ubicacion.setX(resultado.getPosicionX());ubicacion.setY(resultado.getPosicionY());
	        		resultadoSatellite.setMessage(resultado.getMensaje());
	        		resultadoSatellite.setPosition(ubicacion);
	        		return request.createResponseBuilder(HttpStatus.OK).body(resultadoSatellite).build();
	        	}

	        } else {
	        	return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("La información no es suficiente para procesar la información").build();
	        }
        } catch (ExcepcionQasar e) {
			e.printStackTrace();
			return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("La información no es suficiente para procesar la información").build();
		}
    }
    
    
    
}
