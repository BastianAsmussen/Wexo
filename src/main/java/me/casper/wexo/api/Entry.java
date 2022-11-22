package me.casper.wexo.api;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Entry {
	
	private final String title;
	private final String description;
	private final String programType;
	
	private final int releaseYear;
	
	private final ArrayList<String> covers;
	private final ArrayList<String> backdrops;
	
	private final ArrayList<String> genres;
	
	private final ArrayList<String> actors;
	private final ArrayList<String> directors;
	
	private final ArrayList<String> trailers;
}
