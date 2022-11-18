package me.casper.wexo;

import me.casper.util.Time;
import me.casper.wexo.api.REST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;

@SpringBootApplication
public class WEXOApplication {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(WEXOApplication.class);
	
	private static REST rest;
	
	public static void main(String[] args) {
		
		// TODO: If the Fallback Data.json file is empty on startup, wait for the data to be fetched before starting web server.
		// TODO: There's 10.000 items in the API, so we need to fetch them in batches.
		// TODO: We can fetch 1.000 items per request, so we need to make 10 requests.
		
		// Fetch all the shows every 15 minutes on a separate thread.
		Thread updateThread = new Thread(() -> {
			
			rest = new REST();
			
			final int updateInterval = 900_000; // 15 minutes in milliseconds.
			
			// Wait 1 second for the logger to be initialized.
			try { Thread.sleep(1_000); }
			catch (InterruptedException ignored) { }
			
			while (true) {
				
				final long startTime = System.currentTimeMillis();
				
				try {
					
					LOGGER.info("Updating show data...");
					
					rest.fetchLatestData(1, 100);
					
					LOGGER.info("Show data updated in {}!", Time.formatMillis(System.currentTimeMillis() - startTime));
					
					Thread.sleep(updateInterval);
					
				} catch (IOException | InterruptedException | NullPointerException e) {
					
					LOGGER.error("Failed to update JSON data! (Error Message: {})", e.getMessage());
					
				} catch (URISyntaxException e) {
					
					throw new RuntimeException(e);
				}
			}
		});
		
		updateThread.setName("Update Thread");
		updateThread.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Uncaught exception in thread {}! (Error Message: {})", t.getName(), e.getMessage()));
		updateThread.setPriority(Thread.MIN_PRIORITY);
		updateThread.setDaemon(true);
		updateThread.start();
		
		// Start the Spring Application on the main thread.
		SpringApplication.run(WEXOApplication.class, args);
	}
	
	public static REST getRestInstance() {
		
		return rest;
	}
}
