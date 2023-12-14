package ru.shop.backend.search.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.shop.backend.search.model.SearchResult;
import ru.shop.backend.search.model.SearchResultElastic;
import ru.shop.backend.search.service.SearchService;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {
    private final SearchService service;

    @Override
    public SearchResult find(String text, int regionId) {
        return service.getSearchResult(regionId, text);
    }

    @Override
    public SearchResultElastic finds(String text, int regionId) {
        return service.getSearchResultElastic(text);
    }
}
