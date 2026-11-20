package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.Contact;

import java.util.List;

public interface ContactService {

    Contact create(Contact contact);

    List<Contact> getAll();

    Contact getById(Long id);

    Contact update(Long id, Contact contact);

    void delete(Long id);
}

