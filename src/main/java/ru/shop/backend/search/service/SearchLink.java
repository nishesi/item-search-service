package ru.shop.backend.search.service;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SearchLink<T> {
    Optional<T> find(String text, Pageable pageable);
}
