package tech.asmussen;

import lombok.Data;

import java.util.Map;

/**
 * A class to represent the info cookie.
 *
 * @author Bastian Asmussen
 * @version 1.0.0
 */
@Data
public class InfoCookie {
	
	private final String name;
	private final long maxAge;
	
	private final Theme theme;
	
	private final Map<String, String> filters; // A list of filters to apply to the entries. (Filter -> Value)
	
	public enum Theme {
		
		LIGHT, DARK
	}
}
