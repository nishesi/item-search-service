package ru.shop.backend.search.util;

import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchUtils {
    public static List<CatalogueElastic> groupByCatalogue(List<ItemElastic> list, String brand) {
        return list.stream()
                .collect(Collectors.groupingBy(ItemElastic::getCatalogueId))
                .values().stream()
                .map(itemElastics -> {
                    var item = itemElastics.get(0);
                    return new CatalogueElastic(
                            item.getCatalogue(),
                            item.getCatalogueId(),
                            itemElastics,
                            brand);
                })
                .collect(Collectors.toList());
    }

    public static Optional<List<CatalogueElastic>> findExactMatching(List<ItemElastic> list, String text, String brand) {
        return list.stream()
                .filter(item -> Objects.equals(text, item.getName()) ||
                        text.startsWith(item.getType()) && text.endsWith(item.getName()))
                .findFirst()
                .map(item -> List.of(
                        new CatalogueElastic(
                                item.getCatalogue(),
                                item.getCatalogueId(),
                                List.of(item),
                                brand)));
    }
}
