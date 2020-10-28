package com.jdmm.quasar.util;

import redis.clients.jedis.Jedis;

public class RedisClient {

	private static Jedis jedis; 
	
	public RedisClient() {
		if(jedis == null) {
			jedis = new Jedis("quasar.redis.cache.windows.net",6380,true);
		}
	}
	
	/**
	 * Metodo para consultar propiedad de redis
	 * @param clave
	 * @return
	 */
	public String consultarPropiedad(String clave) {
		if(!jedis.isConnected()) {
			jedis.connect();
			jedis.auth("n0tjGozsKWgeD9+3g9x1b7XOCLtf4OcVbt7fEPzsc78=");
		}
		return jedis.get(clave);
	}
	
	/**
	 * Metodo para guardar una propiedad en redis
	 * @param clave
	 * @param valor
	 */
	public void guardarPropiedad(String clave,String valor) {
		jedis.set(clave, valor);
	}
	
}
