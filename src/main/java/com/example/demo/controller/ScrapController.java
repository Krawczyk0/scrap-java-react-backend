package com.example.demo.controller;
import com.example.demo.model.ScrapModel;
import com.example.demo.service.ScrapService;
import jakarta.validation.Valid;
import lombok.experimental.Delegate;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scrap")
public class ScrapController {

    @Autowired
    ScrapService scrapService;

    @GetMapping("/get-all")
    public ResponseEntity<Map<String, Object>> getAll() {
        List<ScrapModel> data = scrapService.getAll();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Scrap successfully");
        response.put("code","200");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-by-title")
    public ResponseEntity<Map<String, Object>> getByTitle(@Valid @RequestBody Map<String,String> body) {

        String query = body.get("query");

        List<String> elements = scrapService.scrapByTitle(query);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Pobrano " + elements.size() + " wiadomości");
        response.put("status", "success");
        response.put("code","200");
        response.put("data", elements);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteNews(@PathVariable String id){

        String deleted= scrapService.deleteNews(id);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usunięto");
        response.put("code","200");
        response.put("data", deleted);

        return ResponseEntity.ok(response);
    }
}