package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Order(0)
@Component
@RequiredArgsConstructor
public class SkuMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemJpaRepository itemJpaRepository;
    private final ItemElasticRepository itemElasticRepository;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (!isNumeric(text))
            return Optional.empty();

        //TODO elasticsearch мог не успеть обновиться
        return itemJpaRepository.findBySku(text).stream().findFirst()
                .flatMap(itemElasticRepository::findByItemId)
                .map(item -> List.of(new CatalogueElastic(
                        item.getCatalogue(),
                        item.getCatalogueId(),
                        List.of(item),
                        item.getBrand()
                )));
    }
}
