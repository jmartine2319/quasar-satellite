package com.jdmm.quasar.logica;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;

import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.jdmm.quasar.util.RedisClient;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

public class LogicaSatellite {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogicaSatellite.class);

	/**
	 * Metodo que permite calcular la distancia
	 * @param nombreSatellite
	 * @return
	 * @throws ExcepcionQasar 
	 */
	public ResultadoUbicacion calcularDistancia(String nombreSatellite,SatelliteIn satellites) throws ExcepcionQasar {
		ResultadoUbicacion resultadoOut = new ResultadoUbicacion();
		LinkedHashMap<Integer,String> mensajeLimpio = limpiarMensaje(satellites.getMessage());
		ResultadoUbicacion resultadoMensaje = fusionarMensajes(mensajeLimpio);
		double[] ubicacion = new double[1];
		ubicacion[0]=satellites.getDistance();
		ResultadoUbicacion resultadoUbicacion = calcularUbicacion(ubicacion,nombreSatellite);
		if(resultadoMensaje.isExitoso() && resultadoUbicacion.isExitoso()) {
			resultadoOut.setExitoso(true);
			resultadoOut.setPosicionX(resultadoUbicacion.getPosicionX());
			resultadoOut.setPosicionY(resultadoUbicacion.getPosicionY());
			resultadoOut.setMensaje(resultadoMensaje.getMensaje());
		}else {
			if(!resultadoMensaje.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al descifrar los mensajes");
			}
			if(!resultadoUbicacion.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al calcular la ubicacion de los satellites");
			}
		}
		return resultadoOut;
	}
	
	/**
	 * Metodo para verificar que no llegue datos que no sean basura
	 * @param mensajes
	 * @return
	 */
	private static LinkedHashMap<Integer,String> limpiarMensaje(List<String> mensajes) {
	
		LinkedHashMap<Integer,String> mensajeDes = new LinkedHashMap<Integer,String>();
		for(int i=0;i<mensajes.size();i++) {
			
			String pattern = "[^A-Za-z0-9]";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(mensajes.get(i));
		    if (m.find( )) {
		    	mensajeDes.put(i,"");
		    }else {
		    	mensajeDes.put(i, mensajes.get(i));
		    }
			
		}
		return mensajeDes;
	}
	
	/**
	 * Metodo que fusiona los mensajes recibidos por los satellites
	 * @param mensajes
	 * @return
	 */
	private ResultadoUbicacion fusionarMensajes(LinkedHashMap<Integer,String> mensaje) {
		ResultadoUbicacion mensajeOut = new ResultadoUbicacion();
		mensajeOut.setExitoso(true);
		LinkedHashMap<Integer,String> mensajeDescifrado = new LinkedHashMap<Integer,String>();
		
		mensaje.forEach((key,value) -> {
			mensajeDescifrado.put(key, value);
		});
		
		StringBuffer mensajeDes = new StringBuffer();
		mensajeDescifrado.forEach((key,value) -> {
			
			if(key==0) {
				mensajeDes.append(value);
			}else {
				mensajeDes.append(" ");
				mensajeDes.append(value);
			}
			
		});
		mensajeOut.setMensaje(mensajeDes.toString());
		return mensajeOut;
	}
	
	/**
	 * Metodo para calcular la ubicacion usando la distancia respecto a los otros satelites
	 */
	private ResultadoUbicacion calcularUbicacion(double[] listaDistancias,String nombreSatellite) {
		ResultadoUbicacion resultado = new ResultadoUbicacion();
		resultado.setExitoso(true);
		try {

			double[] posicionSatellite = obtenerUbicacion(nombreSatellite);
			if(posicionSatellite.length==0) {
				LOGGER.error("no se pudo obtener las propiedades");
				resultado.setExitoso(false);
				return resultado;
			}
			double[][] posiciones = new double[][] { posicionSatellite};
	
			NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(posiciones, listaDistancias), new LevenbergMarquardtOptimizer());
			Optimum optimum = solver.solve();
	
			double[] centroid = optimum.getPoint().toArray();
	
			resultado.setPosicionX(centroid[0]);
			resultado.setPosicionY(centroid[1]);
		}catch(Exception e) {
			LOGGER.error("Ocurrio un error al calcularUbicacion ",e);
			resultado.setExitoso(false);
		}
		return resultado;
	}
	
	/**
	 * Metodo para obtener la ubicacion
	 * @param nombreSatellite
	 * @return
	 */
	private double[] obtenerUbicacion(String nombreSatellite) {
		RedisClient redis = new RedisClient();
		String propiedad = redis.consultarPropiedad(nombreSatellite);
		String[] ubicacionXY=propiedad.split(",");
		double[] ubicacionSatellite = new double[2];
		if(propiedad.equals("") || ubicacionXY.length<2) {
			return ubicacionSatellite;
		}
		ubicacionSatellite[0]=Double.parseDouble(ubicacionXY[0]);
		ubicacionSatellite[0]=Double.parseDouble(ubicacionXY[1]);
		return ubicacionSatellite;
	}
}
