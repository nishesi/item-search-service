package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchChain {
    private final List<SearchLink<List<CatalogueElastic>>> chain;

    public List<CatalogueElastic> searchByText(String text, Pageable pageable) {
        for (var link : chain) {
            var result = link.find(text, pageable);
            if (result.isPresent())
                return result.get();
        }
        return List.of();
    }
}
