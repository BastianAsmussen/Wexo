package tech.asmussen.wexo.controllers;

import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tech.asmussen.InfoCookie;
import tech.asmussen.wexo.WEXOApplication;
import tech.asmussen.wexo.api.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static tech.asmussen.wexo.WEXOApplication.LOGGER;

@Controller
public class WebController {
	
	@GetMapping("/")
	public String index(Model model,
	                    
	                    @RequestParam(value = "start", defaultValue = "1") int start,
	                    @RequestParam(value = "end", defaultValue = "100") int end,
	                    @RequestParam(value = "search", defaultValue = "all") String search,
	                    @RequestParam(value = "genre", defaultValue = "all") String genre,
	                    @RequestParam(value = "year", defaultValue = "-1") int year,
	                    @RequestParam(value = "actor", defaultValue = "all") String actor,
	                    @RequestParam(value = "director", defaultValue = "all") String director,
	                    @RequestParam(value = "type", defaultValue = "all") String type)
	{
		/*
		// Handle the info cookie.
		InfoCookie info = new Gson().fromJson(infoJson, InfoCookie.class);
		
		if (info == null) {
			
			info = new InfoCookie(
					"info",
					60 * 60 * 24 * 365,
					InfoCookie.Theme.DARK,
					new HashMap<>()
			);
			
		} else {
			
			// If the cookie is set, update the lifetime.
			info = new InfoCookie(
					"info",
					60 * 60 * 24 * 365,
					info.getTheme(),
					info.getFilters()
			);
		}
		
		
		model.addAttribute("info", info);
		*/
		
		// Handle the entries on the site based on the given parameters.
		List<Entry> entries = WEXOApplication.getRestInstance().getActiveCache(start, end, search, genre, year, actor, director, type);
		
		HashMap<String, Integer> genres = new HashMap<>(); // A list of genres and the number of entries in each. (Genre -> Count)
		HashMap<String, String> coverArt = new HashMap<>(); // A list of URLs to use as cover art for each genre. (Genre -> URL)
		
		if (entries == null || entries.isEmpty()) {
			
			LOGGER.error("No entries found for the given parameters!");
			
			model.addAttribute("cause", "Der blev ikke fundet noget data i vores system!");
			
			return "error";
		}
		
		// For each entry, add the genre to the list of genres if it isn't already there.
		for (Entry entry : entries) {
			
			// For each genre in the entry (there can be multiple) add it to the HashMap.
			for (String entryGenre : entry.getGenres()) {
				
				// If the genre already exists, increment the count, otherwise add it to the map.
				if (genres.containsKey(entryGenre)) {
					
					genres.put(entryGenre, genres.get(entryGenre) + 1);
					
				} else {
					
					genres.put(entryGenre, 1);
				}
			}
			
			// If the genre doesn't have a cover art URL, add it.
			if (!coverArt.containsKey(entry.getGenres().get(0))) {
				
				coverArt.put(entry.getGenres().get(0), entry.getBestCover(2160, 3840));
			}
		}
		
		model.addAttribute("start", start);
		model.addAttribute("end", end);
		model.addAttribute("genre", genre);
		model.addAttribute("entries", entries);
		model.addAttribute("genres", genres);
		model.addAttribute("coverArt", coverArt);
		
		return "index";
	}
	
	@GetMapping("/entry/{id}")
	public String entry(Model model, @PathVariable(value = "id") String id) {
		
		Entry entry = WEXOApplication.getRestInstance().getEntry(id);
		
		if (entry == null) {
			
			model.addAttribute("cause", "Der blev ikke fundet noget data i vores system!");
			
			return "error";
		}
		
		model.addAttribute("entry", entry);
		
		return "entry";
	}
	
	public Optional<String> readCookie(HttpServletRequest request, String name) {
		
		return Arrays.stream(request.getCookies())
				.filter(cookie -> cookie.getName().equals(name))
				.map(Cookie::getValue)
				.findAny();
	}
	
	public void setInfoCookie(HttpHeaders headers, InfoCookie info) {
		
		ResponseCookie cookie = ResponseCookie.from("info", new Gson().toJson(info))
				.maxAge(info.getMaxAge())
				.path("/")
				.secure(true)
				.httpOnly(true)
				.build();
		
		headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
