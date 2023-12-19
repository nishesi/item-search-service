package ru.shop.backend.search.chain.links;

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.chain.SearchLink;
import ru.shop.backend.search.chain.TypeMatchingAbstractSearch;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.shop.backend.search.util.SearchUtils.*;
import static ru.shop.backend.search.util.StringUtils.*;

@Order(10)
@Component
public class BrandMatchingSearch extends TypeMatchingAbstractSearch implements SearchLink<CatalogueElastic> {
    public BrandMatchingSearch(ItemElasticRepository repository) {
        super(repository);
    }

    @Override
    public List<CatalogueElastic> findAll(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);

        String brand = tryFindBrand(words, needConvert, pageable);
        if (brand.isEmpty())
            return List.of();

        if (words.isEmpty()) {
            var list = itemElasticRepository.findAllByBrand(brand, pageable);
            return groupByCatalogue(list, brand);
        }

        String type = tryFindType(words, needConvert, pageable);

        List<ItemElastic> list = findItems(brand, type, words, needConvert, pageable);

        Optional<List<CatalogueElastic>> result = findExactMatching(list, words, brand);
        return result.orElseGet(() -> groupByCatalogue(list, brand));
    }

    private String tryFindBrand(List<String> words, boolean needConvert, Pageable pageable) {
        for (var iterator = words.iterator(); iterator.hasNext(); ) {
            String word = iterator.next();
            var list = itemElasticRepository.findAllByBrand(createQuery(word, needConvert), pageable);
            if (!list.isEmpty()) {
                iterator.remove();
                return list.get(0).getBrand();
            }
        }
        return "";
    }

    private List<ItemElastic> findItems(String brand, String type, List<String> words, boolean needConvert, Pageable pageable) {
        // '_' - prevent fuzzy search for last word
        String processedText = String.join(" ", words) + "_";
        if (type.isEmpty()) {
            return findWithConvert(processedText, needConvert,
                    t -> itemElasticRepository.findAllByBrand(t, brand, pageable));
        }
        String fType = type + "?";
        return findWithConvert(processedText, needConvert,
                t -> itemElasticRepository.findAllByTypeAndBrand(t, brand, fType, pageable));
    }
}
