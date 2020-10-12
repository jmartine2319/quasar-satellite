package com.jdmm.quasar.util;

import redis.clients.jedis.Jedis;

public class RedisClient {

	private static Jedis jedis; 
	public String consultarPropiedad(String clave) {
		if(jedis == null) {
			jedis = new Jedis("quasar.redis.cache.windows.net",6380,true);
		}
		if(!jedis.isConnected()) {
			jedis.connect();
			jedis.auth("Z3+XAqKjqYG38mPSUHUH0+Go2nRLp3MOq7DreyIilrs=");
		}
		return jedis.get(clave);
	}
	
}
