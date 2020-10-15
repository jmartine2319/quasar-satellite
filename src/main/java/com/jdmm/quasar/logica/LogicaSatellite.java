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

import com.google.gson.Gson;
import com.jdmm.quasar.dto.ResultadoUbicacion;
import com.jdmm.quasar.dto.SatellitInSimple;
import com.jdmm.quasar.dto.SatelliteIn;
import com.jdmm.quasar.dto.SatellitesIn;
import com.jdmm.quasar.util.ExcepcionQasar;
import com.jdmm.quasar.util.RedisClient;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

public class LogicaSatellite {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogicaSatellite.class);

	/**
     * Mensaje para obtener los datos de la posicion y mensaje del satellite
     * @return
     * @throws ExcepcionQasar
     */
    public ResultadoUbicacion procesarOperacion(SatelliteIn satellites) throws ExcepcionQasar {
    	RedisClient redisClient = new RedisClient();
    	
    	SatellitesIn satellitesIn = new SatellitesIn();
		List<SatelliteIn> listaSatellite = new ArrayList<>();
		String nombresSatellites = redisClient.consultarPropiedad("satellites");
		LOGGER.info("Satellite actual "+satellites.getName()+" Satellite recuperado "+nombresSatellites);
		SatelliteIn satelliteInNuevo = recuperarSatellites(nombresSatellites);
		listaSatellite.add(satelliteInNuevo);				
		listaSatellite.add(satellites);
		satellitesIn.setSatellites(listaSatellite);
		
    	ResultadoUbicacion resultado = iniciarProcesoUbicacion(satellitesIn);
		if(resultado.isExitoso() && resultado.isExitoso()) {
			return resultado;
		}else {
			if(!resultado.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al descifrar los mensajes");
			}
			if(!resultado.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al calcular la ubicación");
				
			}
		}
		return resultado;
    }
    
    /**
     * Metodo para recuperar los datos del satellite almacenado en redis
     * @param nombreSatellite
     */
    private SatelliteIn recuperarSatellites(String nombreSatellite) {
    	SatelliteIn satelliteIn = new SatelliteIn();
    	RedisClient redisClient = new RedisClient();
    	Gson gson = new Gson();
    	LOGGER.info("Satellite recuperado json "+redisClient.consultarPropiedad(nombreSatellite+"-info"));
    	SatellitInSimple satellitInSimple = gson.fromJson(redisClient.consultarPropiedad(nombreSatellite+"-info"), SatellitInSimple.class);
        satelliteIn.setName(nombreSatellite);
        satelliteIn.setDistance(satellitInSimple.getDistance());
        satelliteIn.setMessage(satellitInSimple.getMessage());
    	return satelliteIn;
    }
	
	/**
	 * Metodo que lanza las operaciones para descigrar mensaje y obtener distancia
	 * @param satellites
	 * @return
	 * @throws ExcepcionQasar
	 */
	public ResultadoUbicacion iniciarProcesoUbicacion(SatellitesIn satellites) throws ExcepcionQasar {
		ResultadoUbicacion resultado = new ResultadoUbicacion();
		resultado.setExitoso(false);
		List<LinkedHashMap<Integer,String>> listaMensajes = new ArrayList<>();
		if(satellites.getSatellites().size()>3) {
			return resultado;
		}
		for(int i=0;i<satellites.getSatellites().size();i++) {
			LinkedHashMap<Integer,String> mensajes=limpiarMensaje(satellites.getSatellites().get(i).getMessage());
			listaMensajes.add(mensajes);
			//listaDistancias[i]=satellites.getSatellites().get(i).getDistance();
		}
		ResultadoUbicacion mensaje = fusionarMensajes(listaMensajes);
		ResultadoUbicacion distancia = calcularUbicacion(satellites);
		if(mensaje.isExitoso() && distancia.isExitoso()) {
			resultado.setExitoso(true);
			resultado.setPosicionX(distancia.getPosicionX());
			resultado.setPosicionY(distancia.getPosicionY());
			resultado.setMensaje(mensaje.getMensaje());
		}else {
			if(!mensaje.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al descifrar los mensajes");
			}
			if(!distancia.isExitoso()) {
				throw new ExcepcionQasar("Ocurrio un error al calcular la ubicacion de los satellites");
			}
		}
		return resultado;
	}

	/**
	 * Metodo para calcular la ubicacion usando la distancia respecto a los otros satelites
	 */
	private ResultadoUbicacion calcularUbicacion(SatellitesIn satellites) {
		ResultadoUbicacion resultado = new ResultadoUbicacion();
		resultado.setExitoso(true);
		try {
			double[][] posiciones = new double[satellites.getSatellites().size()][];
			for(int i=0;i<satellites.getSatellites().size();i++) {
				
				double[] satellite = obtenerUbicacion(satellites.getSatellites().get(i).getName());
				
				if(satellite==null || satellite.length==0) {
					LOGGER.error("No se pudo obtener la ubicacion de los satellites ");
					resultado.setExitoso(false);
					return resultado;
				}
				posiciones[i] = satellite;
				
				
			}
			double[] listaDistancias = new double[satellites.getSatellites().size()];
			for(int i=0;i<satellites.getSatellites().size();i++) {
				listaDistancias[i]=satellites.getSatellites().get(i).getDistance();
			}
	
			NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(posiciones, listaDistancias), new LevenbergMarquardtOptimizer());
			Optimum optimum = solver.solve();
	
			double[] centroid = optimum.getPoint().toArray();
	
			resultado.setPosicionX(centroid[0]);
			resultado.setPosicionY(centroid[1]);
		}catch(Exception e) {
			LOGGER.error("Ocurrio un error en calcularUbicacion ",e);
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
	private ResultadoUbicacion fusionarMensajes(List<LinkedHashMap<Integer,String>> mensajes) {
		ResultadoUbicacion mensajeOut = new ResultadoUbicacion();
		mensajeOut.setExitoso(true);
		LinkedHashMap<Integer,String> mensajeDescifrado = new LinkedHashMap<Integer,String>();
		int tamanoArreglo=0;
		for(int i =0;i<mensajes.size();i++) {
			tamanoArreglo = mensajes.size();
			if(i>0 && tamanoArreglo>0) {
				if(tamanoArreglo>mensajes.size() || tamanoArreglo<mensajes.size()) {
					mensajeOut.setExitoso(false);
					return mensajeOut;
				}
			}
			LinkedHashMap<Integer,String> mensaje= mensajes.get(i);
			if(i==0) {
				mensaje.forEach((key,value) -> {
					mensajeDescifrado.put(key, value);
				});
			}else {
				mensaje.forEach((key,value) -> {
					if(value.length()>0) {
						mensajeDescifrado.put(key, value);
					}
					
				});
			}
		}
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
}
