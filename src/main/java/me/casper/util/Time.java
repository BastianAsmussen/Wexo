package me.casper.util;

public final class Time {
	
	public static String formatMillis(long millis) {
		
		return String.format("%.3f seconds", millis / 1_000d);
	}
}
