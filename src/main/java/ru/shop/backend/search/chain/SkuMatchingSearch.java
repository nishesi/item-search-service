package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.repository.ItemDbRepository;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Order(0)
@Component
@RequiredArgsConstructor
public class SkuMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemDbRepository repoDb;
    private final ItemRepository elasticRepository;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (!isNumeric(text))
            return Optional.empty();

        //TODO elasticsearch мог не успеть обновиться
        return repoDb.findBySku(text).stream().findFirst()
                .flatMap(elasticRepository::findByItemId)
                .map(item -> List.of(new CatalogueElastic(
                        item.getCatalogue(),
                        item.getCatalogueId(),
                        List.of(item),
                        item.getBrand()
                )));
    }
}
