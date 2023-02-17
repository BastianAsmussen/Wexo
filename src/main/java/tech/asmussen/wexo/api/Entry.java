package tech.asmussen.wexo.api;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

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
		
		// Get the cover closest to the requested dimensions.
		String bestCover = null;
		
		for (String url : covers.keySet()) {
			
			int coverWidth = covers.get(url).get(0);
			int coverHeight = covers.get(url).get(1);
			
			if (coverWidth >= width && coverHeight >= height) {
				
				if (bestCover == null) {
					
					bestCover = url;
					
				} else {
					
					int bestWidth = covers.get(bestCover).get(0);
					int bestHeight = covers.get(bestCover).get(1);
					
					if (coverWidth < bestWidth && coverHeight < bestHeight) {
						
						bestCover = url;
					}
				}
			}
		}
		
		// If no cover was found, return the first one.
		if (bestCover == null) {
			
			LOGGER.warn("No cover found for entry {} with dimensions {}x{}!", id, width, height);
			
			if (!covers.isEmpty()) {
				
				bestCover = covers.keySet().iterator().next();
				
			} else {
				
				LOGGER.warn("No covers found for entry {}!", id);
			}
		}
		
		return bestCover;
	}
}
