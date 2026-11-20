package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.Contact;
import com.example.beach_safety_water_quality.repo.ContactRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepo contactRepo;

    public ContactServiceImpl(ContactRepo contactRepo) {
        this.contactRepo = contactRepo;
    }

    @Override
    public Contact create(Contact contact) {
        contact.setId(null);
        return contactRepo.save(contact);
    }

    @Override
    public List<Contact> getAll() {
        return contactRepo.findAll();
    }

    @Override
    public Contact getById(Long id) {
        return contactRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + id));
    }

    @Override
    public Contact update(Long id, Contact contact) {
        Contact existingContact = contactRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + id));

        existingContact.setName(contact.getName());
        existingContact.setType(contact.getType());

        return contactRepo.save(existingContact);
    }

    @Override
    public void delete(Long id) {
        Contact existingContact = contactRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + id));
        contactRepo.delete(existingContact);
    }
}

