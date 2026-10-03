package com.example.report_service;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "wardens")
public class Warden {

    @Id
    private Long id;
    private String lastName;
    private String firstName;
    private String patronymic;
    private LocalDate birthDate;
    private String position;
    private LocalDate hireDate;
    private LocalDate fireDate;
    private String status;

    protected Warden() {
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPosition() {
        return position;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public LocalDate getFireDate() {
        return fireDate;
    }

    public String getStatus() {
        return status;
    }
}