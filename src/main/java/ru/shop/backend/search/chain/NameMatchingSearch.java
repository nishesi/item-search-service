package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.shop.backend.search.util.SearchUtils.*;
import static ru.shop.backend.search.util.StringUtils.parseAndAssertNeedConvert;

@Order(25)
@RequiredArgsConstructor
public class NameMatchingSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemElasticRepository itemElasticRepository;

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        var list = findByName(text, pageable);
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list);
    }

    private List<CatalogueElastic> findByName(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);

        List<ItemElastic> list = findByCriteria(words, needConvert, pageable);
        var result = findExactMatching(list, words, "");
        return result.orElseGet(() -> groupByCatalogue(list, ""));
    }

    private List<ItemElastic> findByCriteria(List<String> words, boolean needConvert, Pageable pageable) {
        // '_' - prevent fuzzy search for last word
        String text = String.join(" ", words) + "_";
        return findWithConvert(text, needConvert, t -> itemElasticRepository.find(t, pageable));
    }
}
