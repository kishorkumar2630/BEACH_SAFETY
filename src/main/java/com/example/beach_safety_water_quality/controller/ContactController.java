package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.Contact;
import com.example.beach_safety_water_quality.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/create")
    public ResponseEntity<Contact> create(@RequestBody Contact contact) {
        Contact createdContact = contactService.create(contact);
        return new ResponseEntity<>(createdContact, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Contact>> getAll() {
        List<Contact> contacts = contactService.getAll();
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Contact> getById(@PathVariable Long id) {
        Contact contact = contactService.getById(id);
        return ResponseEntity.ok(contact);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Contact> update(@PathVariable Long id, @RequestBody Contact contact) {
        Contact updatedContact = contactService.update(id, contact);
        return ResponseEntity.ok(updatedContact);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

