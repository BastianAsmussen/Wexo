package tech.asmussen.util;

public final class Time {
	
	/**
	 * Formats a time in milliseconds to a human-readable format.
	 *
	 * @param millis The time in milliseconds.
	 * @return The time in a human-readable format.
	 */
	public static String formatTime(long millis) {
		
		if (millis < 1_000) return millis + "ms";
		
		final double seconds = millis / 1_000d;
		if (seconds < 60) return String.format("%.2fs", seconds);
		
		final double minutes = seconds / 60;
		if (minutes < 60) return String.format("%.2fm", minutes);
		
		final double hours = minutes / 60;
		if (hours < 24) return String.format("%.2fh", hours);
		
		final double days = hours / 24;
		return String.format("%.2fd", days);
	}
	
	/**
	 * Returns the estimated time remaining for a task.
	 *
	 * @param elapsed The time elapsed in milliseconds since the task started.
	 * @param loaded  The number of items loaded.
	 * @param total   The total number of items to load.
	 * @return The estimated time remaining in milliseconds.
	 */
	public static long getEstimatedTime(long elapsed, int loaded, int total) {
		
		return (elapsed / loaded) * (total - loaded);
	}
	
	/**
	 * Get the percentage of a task that has been completed.
	 *
	 * @param loaded The number of items loaded.
	 * @param total  The total number of items to load.
	 * @return The percentage of the task that has been completed.
	 */
	public static double getPercentage(int loaded, int total) {
		
		return (double) (loaded / total) * 100;
	}
}
