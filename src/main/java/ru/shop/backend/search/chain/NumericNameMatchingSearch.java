package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Order(5)
@Component
@RequiredArgsConstructor
public class NumericNameMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemRepository repo;

    private static List<CatalogueElastic> groupByCatalogue(List<ItemElastic> list) {
        return list.stream()
                .collect(Collectors.groupingBy(ItemElastic::getCatalogueId))
                .values().stream()
                .map(itemElastics -> {
                    var item = itemElastics.get(0);
                    return new CatalogueElastic(
                            item.getCatalogue(),
                            item.getCatalogueId(),
                            itemElastics,
                            "");
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (isNumeric(text)) {
            List<ItemElastic> list = repo.findAllByNameContaining(text, pageable);
            if (!list.isEmpty())
                return Optional.of(groupByCatalogue(list));
        }
        return Optional.empty();
    }
}
