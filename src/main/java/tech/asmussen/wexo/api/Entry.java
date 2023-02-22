package tech.asmussen.wexo.api;

import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
		
		// The covers are not sorted, we need to fetch the one with the highest resolution that is closest to the aspect ratio.
		// Covers are order like so: URL -> [Width, Height]
		
		// The best cover is the one with the highest resolution that is closest to the aspect ratio.
		String bestCover = null;
		
		int bestCoverWidth = 0;
		int bestCoverHeight = 0;
		
		// The aspect ratio of the requested image.
		double requestedAspectRatio = getAspectRatio(width, height);
		
		// For each cover in the entry, check if it is the best cover.
		for (String cover : covers.keySet()) {
			
			// Get the width and height of the cover.
			int coverWidth = covers.get(cover).get(0);
			int coverHeight = covers.get(cover).get(1);
			
			// Get the aspect ratio of the cover.
			double coverAspectRatio = getAspectRatio(coverWidth, coverHeight);
			
			// If the cover is larger than the best cover, and the aspect ratio is in the correct range, set it as the best cover.
			final double aspectRatioRange = 0.25;
			
			if (coverWidth > bestCoverWidth
					&& coverHeight > bestCoverHeight
					&& coverAspectRatio > (requestedAspectRatio - aspectRatioRange)
					&& coverAspectRatio < (requestedAspectRatio + aspectRatioRange))
			{
				bestCover = cover;
				
				bestCoverWidth = coverWidth;
				bestCoverHeight = coverHeight;
			}
		}
		
		return bestCover;
	}
	
	private double getAspectRatio(int width, int height) {
		
		return (double) width / (double) height;
	}
}
