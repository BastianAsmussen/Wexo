package me.casper.wexo.controllers;

import me.casper.wexo.WEXOApplication;
import me.casper.wexo.api.Entry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class WebController {
	
	@GetMapping("/")
	public String index(Model model) {
		
		ArrayList<Entry> entries = WEXOApplication.getRestInstance().getActiveCache(1, 100);
		
		if (entries == null || entries.isEmpty()) {
			
			model.addAttribute("cause", "Der blev ikke fundet noget data i vores system!");
			
			return "error";
		}
		
		model.addAttribute("entries", WEXOApplication.getRestInstance().getActiveCache(1, 100));
		
		return "index";
	}
}
