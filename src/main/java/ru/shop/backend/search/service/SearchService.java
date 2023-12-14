package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.*;
import ru.shop.backend.search.repository.ItemDbRepository;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ItemDbRepository repoDb;
    private final SearchChain searchChain;

    private static final Pageable PAGE_150 = PageRequest.of(0, 150);
    private static final Pageable PAGE_10 = PageRequest.of(0, 10);

    public synchronized SearchResult getSearchResult(Integer regionId, String text) {
        List<CatalogueElastic> result = searchChain.searchByText(text, PAGE_10);

        List<Item> items = repoDb.findByIds(regionId,
                        result.stream()
                                .flatMap(category -> category.getItems().stream())
                                .map(ItemElastic::getItemId)
                                .collect(Collectors.toList()))
                .stream()
                .map(arr -> new Item(
                        ((BigInteger) arr[2]).intValue(),
                        arr[1].toString(),
                        arr[3].toString(),
                        arr[4].toString(),
                        ((BigInteger) arr[0]).intValue(),
                        arr[5].toString()))
                .collect(Collectors.toList());
        Set<String> catUrls = new HashSet<>();
        String brand = null;
        if (!result.isEmpty())
            brand = result.get(0).getBrand();
        if (brand == null) {
            brand = "";
        }
        brand = brand.toLowerCase(Locale.ROOT);
        String finalBrand = brand;
        List<Category> categories = repoDb.findCatsByIds(
                        items.stream()
                                .map(Item::getItemId)
                                .collect(Collectors.toList()))
                .stream()
                .map(arr -> {
                    if (catUrls.contains(arr[2].toString()))
                        return null;
                    catUrls.add(arr[2].toString());
                    return
                            new Category(arr[0].toString(),
                                    arr[1].toString(),
                                    "/cat/" + arr[2].toString() + (finalBrand.isEmpty() ? "" : "/brands/" + finalBrand),
                                    "/cat/" + arr[3].toString(), arr[4] == null ? null : arr[4].toString());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new SearchResult(
                items,
                categories,
                !result.isEmpty() ? (List.of(new TypeHelpText(TypeOfQuery.SEE_ALSO,
                        ((result.get(0).getItems().get(0).getType() != null ? result.get(0).getItems().get(0).getType() : "") +
                                " " + (result.get(0).getBrand() != null ? result.get(0).getBrand() : "")).trim()))) : new ArrayList<>()
        );
    }

    public SearchResultElastic getSearchResultElastic(String text) {
        List<CatalogueElastic> list = searchChain.searchByText(text, PAGE_150);
        return new SearchResultElastic(list);
    }
}
