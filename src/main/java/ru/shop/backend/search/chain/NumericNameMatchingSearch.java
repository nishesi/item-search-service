package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Order(5)
@Component
@RequiredArgsConstructor
public class NumericNameMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemRepository repo;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (isNumeric(text)) {
            List<ItemElastic> list1 = repo.findAllByName(".*" + text + ".*", PageRequest.of(0, 150));
            //TODO наверное здесь метод используется только для группировки по каталогам
            var catalogue = getMatchingOrGroupByCatalogue(list1, text);
            if (!catalogue.isEmpty()) {
                return Optional.of(catalogue);
            }
        }
        return Optional.empty();
    }

    public static List<CatalogueElastic> getMatchingOrGroupByCatalogue(List<ItemElastic> list, String text) {
        Map<String, List<ItemElastic>> map = new HashMap<>();
        ItemElastic searchedItem = null;

        for (ItemElastic i : list) {
            if (text.equals(i.getName())) {
                searchedItem = i;
            }
            //TODO бессмысленно, зачем name.startsWith(type)
            if (text.endsWith(i.getName()) && text.startsWith(i.getType())) {
                searchedItem = i;
            }
            if (!map.containsKey(i.getCatalogue())) {
                map.put(i.getCatalogue(), new ArrayList<>());
            }
            map.get(i.getCatalogue()).add(i);
        }

        if (searchedItem != null) {
            return List.of(new CatalogueElastic(
                    searchedItem.getCatalogue(),
                    searchedItem.getCatalogueId(),
                    List.of(searchedItem),
                    null));
        }

        return map.keySet().stream()
                .map(c -> new CatalogueElastic(
                        c,
                        map.get(c).get(0).getCatalogueId(),
                        map.get(c),
                        null))
                .collect(Collectors.toList());
    }
}
