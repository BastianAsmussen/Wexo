package me.casper.wexo.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import static me.casper.wexo.WEXOApplication.LOGGER;

public class REST {
	
	public static final int TOTAL_ITEMS = 10_000;
	public static final int MAX_ITEMS_PER_REQUEST = 1_000;
	
	public static final String BASE_URL = "https://feed.entertainment.tv.theplatform.eu/f/jGxigC/bb-all-pas?form=json&lang=da";
	
	private final ArrayList<Entry> activeCache = new ArrayList<>();
	private final ArrayList<Entry> fallbackCache = new ArrayList<>();
	
	private final Path cachePath;
	
	public REST(String path) {
		
		// Check if we have data in the fallback cache on disk.
		// If we do, load it into the active cache.
		// If we don't, load the data from the API into the active cache.
		Path finalPath = null;
		
		try {
			
			Path cachePath = Path.of(Objects.requireNonNull(REST.class.getResource(path), "Cache path is null!").toURI());
			
			if (!Files.exists(cachePath)) {
				
				LOGGER.warn("Cache file does not exist! Creating it...");
				
				Files.createFile(cachePath);
				
				LOGGER.warn("Cache file created!");
			}
			
			finalPath = cachePath;
			
		} catch (IOException | URISyntaxException | NullPointerException e) {
			
			LOGGER.error("Failed to load fallback cache path!", e);
			
			return;
			
		} finally {
			
			this.cachePath = finalPath;
		}
		
		LOGGER.info("Loading fallback cache data...");
		
		// Parse the fallback data into the fallback cache.
		try {
			
			Gson gson = new Gson();
			
			String data = new String(Files.readAllBytes(cachePath));
			
			if (data.isEmpty() || data.isBlank()) {
				
				LOGGER.warn("Fallback cache is empty, awaiting API response...");
				
				return;
			}
			
			JsonObject object = gson.fromJson(data, JsonObject.class);
			JsonArray entries = object.getAsJsonArray("entries");
			
			for (int i = 0; i < entries.size(); i++) {
				
				JsonElement rawEntry = entries.get(i);
				
				if (rawEntry == null || rawEntry.isJsonNull())
					continue;
				
				JsonObject entryObject = rawEntry.getAsJsonObject();
				
				Entry entry = defineCachedEntry(entryObject);
				
				if (entry == null)
					continue;
				
				fallbackCache.add(entry);
			}
			
		} catch (IOException | NullPointerException e) {
			
			LOGGER.error("Failed to parse cache data from disk!", e);
			
			return;
		}
		
		// Copy the fallback cache into the active cache.
		activeCache.addAll(fallbackCache);
		
		LOGGER.info("Fallback cache data loaded!");
	}
	
	public void fetch(int from, int to) {
		
		final String range = String.format("&range=%d-%d", from, to);
		
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
				.addHeader("Accept-Encoding", "gzip")
				.url(BASE_URL + range)
				.build();
		
		try (Response response = client.newCall(request).execute()) {
			
			if (response.code() != 200) {
				
				LOGGER.error("Failed to fetch data from API! (Status Code: {})", response.code());
				
				return;
			}
			
			// The response is compressed with GZIP, so we need to decompress it.
			StringBuilder data = new StringBuilder();
			
			GZIPInputStream inputStream = new GZIPInputStream(response.body().byteStream());
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				
				data.append(new String(buffer, 0, length));
			}
			
			inputStream.close();
			
			JsonArray entries = new Gson().fromJson(data.toString(), JsonObject.class).getAsJsonArray("entries");
			
			for (int i = 0; i < entries.size(); i++) {
				
				JsonObject entryObject = entries.get(i).getAsJsonObject();
				
				Entry entry = defineEntry(entryObject);
				
				if (entry == null)
					continue;
				
				activeCache.add(entry);
			}
			
		} catch (IOException | NullPointerException e) {
			
			LOGGER.error("Failed to fetch data from API!", e);
		}
	}
	
	public void write() {
		
		// Write the active cache to disk.
		Gson gson = new Gson();
		
		JsonElement tree = gson.toJsonTree(activeCache);
		
		if (tree == null || tree.isJsonNull())
			return;
		
		JsonObject wrapper = new JsonObject();
		wrapper.add("entries", tree);
		
		try {
			
			Files.write(cachePath, gson.toJson(wrapper).getBytes());
			
		} catch (IOException e) {
			
			LOGGER.error("Failed to write cache to disk!", e);
		}
	}
	
	public ArrayList<Entry> getActiveCache() {
		
		return activeCache;
	}
	
	public ArrayList<Entry> getActiveCache(int from, int to) {
		
		ArrayList<Entry> cache = new ArrayList<>();
		
		for (int i = from; i < to; i++) {
			
			cache.add(activeCache.get(i));
		}
		
		return cache;
	}
	
	public ArrayList<Entry> getFallbackCache() {
		
		return fallbackCache;
	}
	
	public Path getCachePath() {
		
		return cachePath;
	}
	
	private Entry defineEntry(JsonObject entry) throws NullPointerException {
		
		JsonElement rawTitle = entry.get("title");
		JsonElement rawDescription = entry.get("description");
		JsonElement rawProgramType = entry.get("plprogram$programType");
		
		JsonElement rawReleaseYear = entry.get("plprogram$year");
		
		String title =
				rawTitle == null || rawTitle.isJsonNull()
						? "N/A"
						: rawTitle.getAsString();
		String description =
				rawDescription == null || rawDescription.isJsonNull()
						? "N/A"
						: rawDescription.getAsString();
		String programType =
				rawProgramType == null || rawProgramType.isJsonNull()
						? "N/A"
						: rawProgramType.getAsString();
		
		int releaseYear =
				rawReleaseYear == null || rawReleaseYear.isJsonNull()
						? -1
						: rawReleaseYear.getAsInt();
		
		ArrayList<String> covers = new ArrayList<>();
		ArrayList<String> backdrops = new ArrayList<>();
		
		ArrayList<String> genres = new ArrayList<>();
		
		ArrayList<String> actors = new ArrayList<>();
		ArrayList<String> directors = new ArrayList<>();
		
		ArrayList<String> trailers = new ArrayList<>();
		
		// Handle the covers and backdrops.
		JsonObject thumbnails = entry.getAsJsonObject("plprogram$thumbnails");
		
		if (thumbnails == null || thumbnails.isJsonNull())
			return null;
		
		// For each thumbnail, check if it's a cover or a backdrop.
		// "thumbnails" is a nested object, so we need to iterate over the keys.
		for (String key : thumbnails.keySet()) {
			
			JsonObject thumbnail = thumbnails.getAsJsonObject(key);
			
			String url = thumbnail.get("plprogram$url").getAsString();
			
			if (url.contains("po") || url.contains("Poster")) covers.add(url);
			else if (url.contains("bd")) backdrops.add(url);
		}
		
		// Handle the genres.
		JsonArray tags = entry.getAsJsonArray("plprogram$tags");
		
		if (tags == null || tags.isJsonNull())
			return null;
		
		for (int i = 0; i < tags.size(); i++) {
			
			JsonObject tag = tags.get(i).getAsJsonObject();
			
			if (!tag.get("plprogram$scheme").getAsString().equalsIgnoreCase("genre"))
				continue;
			
			String genre = tag.get("plprogram$title").getAsString();
			
			genres.add(genre);
		}
		
		// Handle the actors and directors.
		JsonArray credits = entry.getAsJsonArray("plprogram$credits");
		
		if (credits == null || credits.isJsonNull())
			return null;
		
		for (int i = 0; i < credits.size(); i++) {
			
			JsonObject credit = credits.get(i).getAsJsonObject();
			
			String type = credit.get("plprogram$creditType").getAsString();
			String name = credit.get("plprogram$personName").getAsString();
			
			if (type.equalsIgnoreCase("actor"))
				actors.add(name);
			
			else if (type.equalsIgnoreCase("director"))
				directors.add(name);
		}
		
		// Handle the trailers.
		JsonArray media = entry.getAsJsonArray("plprogramavailability$media");
		
		if (media == null || media.isJsonNull())
			return null;
		
		for (int i = 0; i < media.size(); i++) {
			
			JsonObject mediaObject = media.get(i).getAsJsonObject();
			
			JsonElement rawUrl = mediaObject.get("plmedia$publicUrl");
			
			if (rawUrl == null || rawUrl.isJsonNull())
				continue;
			
			String url = rawUrl.getAsString();
			
			trailers.add(url);
		}
		
		return new Entry(title, description, programType, releaseYear, covers, backdrops, genres, actors, directors, trailers);
	}
	
	private Entry defineCachedEntry(JsonObject entry) throws NullPointerException {
		
		JsonElement rawTitle = entry.get("title");
		JsonElement rawDescription = entry.get("description");
		JsonElement rawProgramType = entry.get("programType");
		
		JsonElement rawReleaseYear = entry.get("releaseYear");
		
		String title =
				rawTitle == null || rawTitle.isJsonNull()
						? "N/A"
						: rawTitle.getAsString();
		String description =
				rawDescription == null || rawDescription.isJsonNull()
						? "N/A"
						: rawDescription.getAsString();
		String programType =
				rawProgramType == null || rawProgramType.isJsonNull()
						? "N/A"
						: rawProgramType.getAsString();
		
		int releaseYear =
				rawReleaseYear == null || rawReleaseYear.isJsonNull()
						? -1
						: rawReleaseYear.getAsInt();
		
		ArrayList<String> covers = new ArrayList<>();
		ArrayList<String> backdrops = new ArrayList<>();
		
		ArrayList<String> genres = new ArrayList<>();
		
		ArrayList<String> actors = new ArrayList<>();
		ArrayList<String> directors = new ArrayList<>();
		
		ArrayList<String> trailers = new ArrayList<>();
		
		// Handle the covers and backdrops.
		JsonArray coverArray = entry.getAsJsonArray("covers");
		JsonArray backdropArray = entry.getAsJsonArray("backdrops");
		
		if (coverArray == null || coverArray.isJsonNull())
			return null;
		
		if (backdropArray == null || backdropArray.isJsonNull())
			return null;
		
		for (int i = 0; i < coverArray.size(); i++) {
			
			String cover = coverArray.get(i).getAsString();
			
			covers.add(cover);
		}
		
		for (int i = 0; i < backdropArray.size(); i++) {
			
			String backdrop = backdropArray.get(i).getAsString();
			
			backdrops.add(backdrop);
		}
		
		// Handle the genres.
		JsonArray genresArray = entry.getAsJsonArray("genres");
		
		if (genresArray == null || genresArray.isJsonNull())
			return null;
		
		for (int i = 0; i < genresArray.size(); i++) {
			
			String genre = genresArray.get(i).getAsString();
			
			genres.add(genre);
		}
		
		// Handle the actors and directors.
		JsonArray actorsArray = entry.getAsJsonArray("actors");
		JsonArray directorsArray = entry.getAsJsonArray("directors");
		
		if (actorsArray == null || actorsArray.isJsonNull())
			return null;
		
		if (directorsArray == null || directorsArray.isJsonNull())
			return null;
		
		for (int i = 0; i < actorsArray.size(); i++) {
			
			String actor = actorsArray.get(i).getAsString();
			
			actors.add(actor);
		}
		
		for (int i = 0; i < directorsArray.size(); i++) {
			
			String director = directorsArray.get(i).getAsString();
			
			directors.add(director);
		}
		
		// Handle the trailers.
		JsonArray trailersArray = entry.getAsJsonArray("trailers");
		
		if (trailersArray == null || trailersArray.isJsonNull())
			return null;
		
		for (int i = 0; i < trailersArray.size(); i++) {
			
			String trailer = trailersArray.get(i).getAsString();
			
			trailers.add(trailer);
		}
		
		return new Entry(title, description, programType, releaseYear, covers, backdrops, genres, actors, directors, trailers);
	}
}
