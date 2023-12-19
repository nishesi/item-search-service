package ru.shop.backend.search.chain.links;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.chain.SearchLink;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Order(0)
@Component
@RequiredArgsConstructor
public class SkuMatchingSearch implements SearchLink<CatalogueElastic> {
    private final ItemJpaRepository itemJpaRepository;
    private final ItemElasticRepository itemElasticRepository;

    @Override
    public List<CatalogueElastic> findAll(String text, Pageable pageable) {
        if (!isNumeric(text))
            return List.of();

        //TODO elasticsearch мог не успеть обновиться
        return itemJpaRepository.findBySku(text).stream().findFirst()
                .flatMap(itemElasticRepository::findByItemId)
                .map(item -> List.of(new CatalogueElastic(
                        item.getCatalogue(),
                        item.getCatalogueId(),
                        List.of(item),
                        item.getBrand()
                )))
                .orElseGet(List::of);
    }
}
