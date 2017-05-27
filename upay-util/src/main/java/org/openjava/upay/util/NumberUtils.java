package org.openjava.upay.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberUtils
{
	private static Logger LOG = LoggerFactory.getLogger(NumberUtils.class);
	
	public static int convert2Int(String number, int defaultValue)
	{
		if (ObjectUtils.isEmpty(number)) {
			return defaultValue;
		}
		
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException nfe) {
			// Never ignore any exception
			LOG.error("Invalid number format", nfe);
			return defaultValue;
		}
	}
}
