package me.casper.wexo.controllers;

import me.casper.wexo.WEXOApplication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
	
	@GetMapping("/")
	public String index(Model model) {
		
		model.addAttribute("data", WEXOApplication.getRestInstance().getData());
		
		return "main/index.html";
	}
}
