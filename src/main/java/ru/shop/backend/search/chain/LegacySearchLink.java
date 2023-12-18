package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.shop.backend.search.util.SearchUtils.*;
import static ru.shop.backend.search.util.StringUtils.*;

@Component
@RequiredArgsConstructor
public class LegacySearchLink implements SearchLink<List<CatalogueElastic>> {
    private final ItemRepository repo;

    private static <T> List<T> findWithConvert(String text, boolean needConvert, Function<String, List<T>> function) {
        List<T> list = function.apply(text);
        if (list.isEmpty() && needConvert)
            return function.apply(convert(text));
        return list;
    }

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        List<CatalogueElastic> result = findByAnyOccurrence(text, pageable);
        if (result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    private List<CatalogueElastic> findByAnyOccurrence(String text, Pageable pageable) {
        List<String> words = new ArrayList<>();
        boolean needConvert = parseAndAssertNeedConvert(text, words);
        words.remove("");

        String brand = tryFindBrand(words, needConvert, pageable);

        List<ItemElastic> list = new ArrayList<>();
        String type = tryFindType(words, needConvert, list, pageable);

        if (words.isEmpty() && !brand.isEmpty()) {
            //TODO never happens
            return List.of(new CatalogueElastic(
                    list.get(0).getCatalogue(),
                    list.get(0).getCatalogueId(),
                    null,
                    brand));
        }

        Long catalogueId = tryFindCatalogueId(brand, words, needConvert, pageable);

        list = findByCriteria(words, brand, type, catalogueId, pageable);

        if (list.isEmpty()) {
            return notStrongSearch(text, needConvert, brand, pageable);
        }

        var result = findExactMatching(list, text, brand);
        if (result.isPresent())
            return result.get();

        return groupByCatalogue(list, brand);
    }

    private String tryFindBrand(List<String> words, boolean needConvert, Pageable pageable) {
        if (words.size() > 1) {
            for (String word : new ArrayList<>(words)) {
                var list = findWithConvert(word, needConvert, t -> repo.findAllByBrand(t, pageable));
                if (!list.isEmpty()) {
                    words.remove(word);
                    return list.get(0).getBrand();
                }
            }
        }
        return "";
    }

    private String tryFindType(List<String> words, boolean needConvert, List<ItemElastic> list, Pageable pageable) {
        String type = "";

        List<ItemElastic> local = List.of();
        for (String word : new ArrayList<>(words)) {
            local = findWithConvert(word, needConvert, t -> repo.findAllByType(t, pageable));
            if (!local.isEmpty()) {
                if (words.size() > 1)
                    words.remove(word);
                type = local.stream()
                        .map(ItemElastic::getType)
                        .min(Comparator.comparingInt(String::length))
                        .get();
            }
        }

        list.addAll(local);
        return type;
    }

    private Long tryFindCatalogueId(String brand, List<String> words, boolean needConvert, Pageable pageable) {
        if (brand.isEmpty()) {
            for (String word : new ArrayList<>(words)) {
                var list = findWithConvert(word, needConvert, t -> repo.findByCatalogue(t, pageable));
                if (!list.isEmpty()) {
                    words.remove(word);
                    return list.get(0).getCatalogueId();
                }
            }
        }
        return null;
    }

    private List<ItemElastic> findByCriteria(List<String> words, String brand, String type, Long catalogueId, Pageable pageable) {
        // '_' - prevent fuzzy search for last word
        String text = String.join(" ", words) + "_";
        List<ItemElastic> list;

        if (brand.isEmpty()) {
            if (catalogueId == null) {
                if (type.isEmpty()) {
                    list = findWithConvert(text, true, t -> repo.find(t, pageable));
                } else {
                    list = findWithConvert(text, true, t -> repo.findAllByType(t, type, pageable));
                }
            } else {
                text += " " + type;
                String fType = type + "?";
                if (type.isEmpty()) {
                    list = findWithConvert(text, true, t -> repo.find(t, catalogueId, fType, pageable));
                } else {
                    list = findWithConvert(text, true, t -> repo.find(t, catalogueId, pageable));
                }
            }
        } else {
            if (type.isEmpty()) {
                list = findWithConvert(text, true, t -> repo.findAllByBrand(t, brand, pageable));
            } else {
                String fType = type + "?";
                list = findWithConvert(text, true, t -> repo.findAllByTypeAndBrand(t, brand, fType, pageable));
            }
        }
        return list;
    }

    private List<CatalogueElastic> notStrongSearch(String text, boolean needConvert, String brand, Pageable pageable) {
        text += "_";
        var list = findWithConvert(text, needConvert, t -> repo.findAllNotStrong(t, pageable));
        return groupByCatalogue(list, brand);
    }
}
