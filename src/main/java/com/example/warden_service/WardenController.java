package com.example.warden_service;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/wardens")
public class WardenController {

    private final WardenService wardenService;

    public WardenController(WardenService wardenService) {
        this.wardenService = wardenService;
    }

    @PostMapping
    public Warden create(@RequestBody Warden warden) {
        return wardenService.create(warden);
    }

    @GetMapping
    public List<Warden> getAll() {
        return wardenService.getAll();
    }

    @GetMapping("/{id}")
    public Warden getById(@PathVariable Long id) {
        return wardenService.getById(id);
    }

    @PutMapping("/{id}")
    public Warden update(
            @PathVariable Long id,
            @RequestBody Warden warden) {
        return wardenService.update(id, warden);
    }

    @PutMapping("/{id}/fire")
    public Warden fire(@PathVariable Long id) {
        return wardenService.fire(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        wardenService.delete(id);
    }

    @GetMapping("/report")
    public Map<String, Long> getReport() {
        Map<String, Long> report = new HashMap<>();

        report.put("working", wardenService.countWorking());
        report.put("fired", wardenService.countFired());

        return report;
    }
}