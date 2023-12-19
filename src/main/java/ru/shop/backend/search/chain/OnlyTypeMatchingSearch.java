package ru.shop.backend.search.chain;

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.shop.backend.search.util.SearchUtils.*;
import static ru.shop.backend.search.util.StringUtils.parseAndAssertNeedConvert;

@Order(20)
@Component
public class OnlyTypeMatchingSearch extends TypeMatchingAbstractSearch implements SearchLink<List<CatalogueElastic>> {
    public OnlyTypeMatchingSearch(ItemElasticRepository repository) {
        super(repository);
    }

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        List<CatalogueElastic> list = findByType(text, pageable);
        return Optional.empty();
    }

    private List<CatalogueElastic> findByType(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);

        String type = tryFindType(words, needConvert, new ArrayList<>(), pageable);
        if (type.isEmpty())
            return List.of();

        List<ItemElastic> list = findByCriteria(words, type, pageable);

        var result = findExactMatching(list, words, "");
        return result.orElseGet(() -> groupByCatalogue(list, ""));
    }

    private List<ItemElastic> findByCriteria(List<String> words, String type, Pageable pageable) {
        // '_' - prevent fuzzy search for last word
        String text = String.join(" ", words) + "_";
        return findWithConvert(text, true, t -> itemElasticRepository.findAllByType(t, type, pageable));
    }
}
