package com.example.tennisclub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


import java.util.Optional;

@Component
public class EntityFinder {
    public <T, ID> T findByIdOrThrow(Optional<T> optional, ID id, String entityName) {
        return optional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        entityName + " with ID " + id + " not found"));
    }
}