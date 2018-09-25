package eu.europa.ec.cipa.adapter.utils;

import java.util.UUID;

public class RandomUtil {

	public static String generateUUID(){
		String result ="";
		result = UUID.randomUUID().toString();
		return result;
	}
	
}
