
package com.example.warden_service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WardenService {

    private final WardenRepository wardenRepository;

    public WardenService(WardenRepository wardenRepository) {
        this.wardenRepository = wardenRepository;
    }

    public Warden create(Warden warden) {
        return wardenRepository.save(warden);
    }

    public List<Warden> getAll() {
        return wardenRepository.findAll();
    }

    public Warden getById(Long id) {
        return wardenRepository.findById(id)
                .orElseThrow(() -> new WardenNotFoundException("Warden not found"));
    }

    public Warden update(Long id, Warden updatedWarden) {
        Warden warden = getById(id);

        warden.setFullName(updatedWarden.getFullName());
        warden.setBirthDate(updatedWarden.getBirthDate());
        warden.setPosition(updatedWarden.getPosition());
        warden.setHireDate(updatedWarden.getHireDate());
        warden.setFireDate(updatedWarden.getFireDate());
        warden.setStatus(updatedWarden.getStatus());

        return wardenRepository.save(warden);
    }

    public Warden fire(Long id) {
        Warden warden = getById(id);

        warden.setStatus("FIRED");
        warden.setFireDate(java.time.LocalDate.now());

        return wardenRepository.save(warden);
    }

    public void delete(Long id) {
        Warden warden = getById(id);
        wardenRepository.delete(warden);
    }

    public long countWorking() {
    return wardenRepository.countByStatus("WORKING");
    }

    public long countFired() {
        return wardenRepository.countByStatus("FIRED");
    }
}