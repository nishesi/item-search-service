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

@Order(15)
@Component
public class CatalogueMatchingSearch extends TypeMatchingAbstractSearch implements SearchLink<List<CatalogueElastic>> {
    public CatalogueMatchingSearch(ItemElasticRepository repository) {
        super(repository);
    }

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        List<CatalogueElastic> list = findByCatalogue(text, pageable);
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list);
    }

    private List<CatalogueElastic> findByCatalogue(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);
        Long catalogueId = tryFindCatalogueId(words, needConvert, pageable);
        if (catalogueId == null)
            return List.of();

        String type = tryFindType(words, needConvert, new ArrayList<>(), pageable);

        List<ItemElastic> list = findByCriteria(words, type, catalogueId, pageable);

        Optional<List<CatalogueElastic>> result = findExactMatching(list, words, "");
        return result.orElseGet(() -> groupByCatalogue(list, ""));

    }

    private Long tryFindCatalogueId(List<String> words, boolean needConvert, Pageable pageable) {
        for (String word : new ArrayList<>(words)) {
            var list = findWithConvert(word, needConvert, t -> itemElasticRepository.findByCatalogue(t, pageable));
            if (!list.isEmpty()) {
                words.remove(word);
                return list.get(0).getCatalogueId();
            }
        }
        return null;
    }

    private List<ItemElastic> findByCriteria(List<String> words, String type, Long catalogueId, Pageable pageable) {
        // '_' - prevent fuzzy search for last word
        String text = String.join(" ", words) + "_" + " " + type;
        String fType = type + "?";
        if (type.isEmpty()) {
            return findWithConvert(text, true,
                    t -> itemElasticRepository.find(t, catalogueId, fType, pageable));
        } else {
            return findWithConvert(text, true,
                    t -> itemElasticRepository.find(t, catalogueId, pageable));
        }
    }
}
