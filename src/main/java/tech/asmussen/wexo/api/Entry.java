package tech.asmussen.wexo.api;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.asmussen.wexo.WEXOApplication.LOGGER;

@Data
public class Entry {
	
	private final String id;
	
	private final String title;
	private final String description;
	private final String programType;
	
	private final int releaseYear;
	
	private final HashMap<String, List<Integer>> covers;
	private final HashMap<String, List<Integer>> backdrops;
	
	private final List<String> genres;
	
	private final List<String> actors;
	private final List<String> directors;
	
	private final List<String> trailers;
	
	public String getBestCover(int width, int height) {
		
		Map<String, List<Integer>> bestCovers = new HashMap<>(); // URL -> [width, height]
		
		final double aspectRatio = getAspectRatio(width, height);
		
		// Get the cover with the closest aspect ratio.
		String bestCover = null;
		double bestDifference = Double.MAX_VALUE;
		
		for (String cover : covers.keySet()) {
			
			List<Integer> dimensions = covers.get(cover);
			
			if (dimensions.size() != 2) {
				
				LOGGER.warn("Cover dimensions for {} are not 2!", cover);
				
				continue;
			}
			
			int coverWidth = dimensions.get(0);
			int coverHeight = dimensions.get(1);
			
			double coverAspectRatio = getAspectRatio(coverWidth, coverHeight);
			double difference = Math.abs(aspectRatio - coverAspectRatio);
			
			if (difference < bestDifference) {
				
				bestDifference = difference;
				bestCover = cover;
				
				bestCovers.put(cover, List.of(coverWidth, coverHeight));
			}
		}
		
		// Get the cover with the highest resolution.
		int bestWidth = 0;
		int bestHeight = 0;
		
		for (String cover : bestCovers.keySet()) {
			
			List<Integer> dimensions = bestCovers.get(cover);
			
			int coverWidth = dimensions.get(0);
			int coverHeight = dimensions.get(1);
			
			if (coverWidth > bestWidth && coverHeight > bestHeight) {
				
				bestWidth = coverWidth;
				bestHeight = coverHeight;
				
				bestCover = cover;
			}
		}
		
		return bestCover;
	}
	
	private double getAspectRatio(int width, int height) {
		
		return (double) width / (double) height;
	}
}
