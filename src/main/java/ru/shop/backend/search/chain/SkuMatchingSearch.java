package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
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
    private final ItemRepository repo;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        if (isNumeric(text)) {
            Optional<Integer> itemId = repoDb.findBySku(text).stream().findFirst();
            if (itemId.isPresent()) {
                try {
                    //TODO elasticsearch мог не успеть обновиться
                    var list1 = repo.findByItemId(itemId.get().toString(), PageRequest.of(0, 1));
                    return Optional.of(List.of(new CatalogueElastic(
                            list1.get(0).getCatalogue(),
                            list1.get(0).getCatalogueId(),
                            list1,
                            list1.get(0).getBrand())));
                } catch (Exception e) {
                    //
                }
            }
        }
        return Optional.empty();
    }

}
