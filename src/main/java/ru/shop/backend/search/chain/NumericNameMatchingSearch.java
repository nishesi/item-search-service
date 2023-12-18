package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static ru.shop.backend.search.util.SearchUtils.groupByCatalogue;

@Order(5)
@Component
@RequiredArgsConstructor
public class NumericNameMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemElasticRepository itemElasticRepository;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (isNumeric(text)) {
            List<ItemElastic> list = itemElasticRepository.findAllByNameContaining(text, pageable);
            if (!list.isEmpty())
                return Optional.of(groupByCatalogue(list, ""));
        }
        return Optional.empty();
    }
}
