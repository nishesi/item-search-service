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

@Order(10)
@Component
public class BrandMatchingSearch extends TypeMatchingAbstractSearch implements SearchLink<List<CatalogueElastic>> {
    public BrandMatchingSearch(ItemElasticRepository repository) {
        super(repository);
    }

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        var list = findByBrand(text, pageable);
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list);
    }

    private List<CatalogueElastic> findByBrand(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);

        String brand = tryFindBrand(words, needConvert, pageable);
        if (brand.isEmpty())
            return List.of();

        List<ItemElastic> list = new ArrayList<>();
        String type = tryFindType(words, needConvert, list, pageable);

        if (words.isEmpty()) {
            //TODO never happens
            return List.of(new CatalogueElastic(
                    list.get(0).getCatalogue(),
                    list.get(0).getCatalogueId(),
                    null,
                    brand));
        }

        // '_' - prevent fuzzy search for last word
        String processedText = String.join(" ", words) + "_";

        if (type.isEmpty()) {
            list = findWithConvert(processedText, needConvert,
                    t -> itemElasticRepository.findAllByBrand(t, brand, pageable));
        } else {
            String fType = type + "?";
            list = findWithConvert(processedText, needConvert,
                    t -> itemElasticRepository.findAllByTypeAndBrand(t, brand, fType, pageable));
        }

        Optional<List<CatalogueElastic>> result = findExactMatching(list, words, brand);
        if (result.isPresent())
            return result.get();
        return groupByCatalogue(list, brand);
    }

    private String tryFindBrand(List<String> words, boolean needConvert, Pageable pageable) {
        if (words.size() > 1) {
            for (String word : new ArrayList<>(words)) {
                var list = findWithConvert(word, needConvert,
                        t -> itemElasticRepository.findAllByBrand(t, pageable));
                if (!list.isEmpty()) {
                    words.remove(word);
                    return list.get(0).getBrand();
                }
            }
        }
        return "";
    }
}
