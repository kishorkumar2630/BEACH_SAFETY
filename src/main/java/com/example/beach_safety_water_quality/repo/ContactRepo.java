package com.example.beach_safety_water_quality.repo;

import com.example.beach_safety_water_quality.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepo extends JpaRepository<Contact, Long> {
}
