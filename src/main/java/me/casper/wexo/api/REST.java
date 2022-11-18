package me.casper.wexo.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static me.casper.wexo.WEXOApplication.LOGGER;

public class REST {
	
	public static final int TOTAL_ITEMS = 10_000;
	public static final int MAX_ITEMS_PER_REQUEST = 1_000;
	
	public static final String ALL_DATA_URL = "https://feed.entertainment.tv.theplatform.eu/f/jGxigC/bb-all-pas?form=json&lang=da";
	
	public static final Path FALLBACK_DATA_PATH;
	
	static {
		
		try {
			
			FALLBACK_DATA_PATH = Path.of(Objects.requireNonNull(REST.class.getResource("/api/data/Fallback Data.json"), "Fallback Data path is null!").toURI());
			
		} catch (URISyntaxException e) {
			
			throw new RuntimeException(e);
		}
	}
	
	private String fallbackData;
	private String data;
	
	public REST() {
		
		try {
			
			fallbackData = new String(Files.readAllBytes(FALLBACK_DATA_PATH));
			
		} catch (IOException e) {
			
			LOGGER.error("Failed to load/create the fallback data! (Error Message: {})", e.getMessage());
			
			System.exit(1);
		}
		
		data = fallbackData;
	}
	
	public String getData() {
		
		return data;
	}
	
	public String getFallbackData() {
		
		return fallbackData;
	}
	
	public void setFallbackData(String fallbackData) {
		
		/*
		If the current fallback data isn't empty,
		the new fallback data isn't empty, or equal to the current fallback data,
		then set the new fallback data to the current fallback data.
		 */
		if (!this.fallbackData.isEmpty() && fallbackData.equals(this.fallbackData) || fallbackData.isEmpty()) {
			
			LOGGER.warn("The new fallback data is {}, aborting...",
					fallbackData.isEmpty() ? "empty" : "the same as the current one");
			
			return;
		}
		
		JsonObject jsonObject = new Gson().fromJson(fallbackData, JsonObject.class);
		
		if (jsonObject.get("title").getAsString().contains("Exception")) {
			
			LOGGER.error("The new fallback data is invalid! (Error Message: {})", jsonObject.get("description").getAsString());
			
			return;
		}
		
		try {
			
			Files.write(FALLBACK_DATA_PATH, fallbackData.getBytes());
			
			this.fallbackData = fallbackData;
			
			LOGGER.info("Successfully wrote the new fallback data to the disk!");
			
		} catch (IOException e) {
			
			LOGGER.error("Failed to write the new fallback data to the disk! (Error Message: {})", e.getMessage());
		}
	}
	
	public void fetchLatestData(int from, int to) throws IOException, URISyntaxException {
		
		if (from < 0 || to < 0) {
			
			LOGGER.error("The \"from\" and \"to\" values must be positive!");
			
			return;
		}
		
		if (from > to) {
			
			LOGGER.error("The \"from\" value must be less than to the \"to\" value!");
			
			return;
		}
		
		if (to - from > MAX_ITEMS_PER_REQUEST) {
			
			LOGGER.error("The range size must be less than or equal to {}!", MAX_ITEMS_PER_REQUEST);
			
			return;
		}
		
		if (to > TOTAL_ITEMS) {
			
			LOGGER.error("The \"to\" value must be less than or equal to {}!", TOTAL_ITEMS);
			
			return;
		}
		
		final String range = String.format("&range=%d-%d", from, to);
		
		// Request JSON series from API.
		String json;
		
		OkHttpClient client = new OkHttpClient();
		
		Request request =
				new Request.Builder()
						.url(ALL_DATA_URL + range)
						.get()
						.build();
		
		try (Response response = client.newCall(request).execute()) {
			
			json = response.body().string();
			
			// Save the JSON to a file.
			setFallbackData(json);
			
		} catch (IOException e) {
			
			json = getFallbackData();
			
			LOGGER.error("Failed to fetch the latest data from the API, using fallback data instead... (Error Message: {})", e.getMessage());
		}
		
		data = json;
	}
}
