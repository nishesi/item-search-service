package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemRepository;

import java.util.*;
import java.util.stream.Collectors;

import static ru.shop.backend.search.util.StringUtils.convert;
import static ru.shop.backend.search.util.StringUtils.isContainErrorChar;

@Component
@RequiredArgsConstructor
public class LegacySearchLink implements SearchLink<List<CatalogueElastic>> {
    private final ItemRepository repo;

    private static boolean parseAndAssertNeedConvert(String text, List<String> words) {
        if (isContainErrorChar(text)) {
            words.addAll(List.of(convert(text).split("\\s")));
            return false;
        } else if (isContainErrorChar(convert(text))) {
            words.addAll(List.of(text.split("\\s")));
            return false;
        } else {
            words.addAll(List.of(text.split("\\s")));
            return true;
        }
    }

    private static List<CatalogueElastic> getMatchingOrGroupByCatalogue(List<ItemElastic> list, List<String> words, String brand) {
        String text = String.join(" ", words);
        Map<String, List<ItemElastic>> map = new HashMap<>();
        ItemElastic searchedItem = null;

        for (ItemElastic i : list) {
            if (text.equals(i.getName())) {
                searchedItem = i;
            }
            if (text.endsWith(i.getName()) && text.startsWith(i.getType())) {
                searchedItem = i;
            }
            if (!map.containsKey(i.getCatalogue())) {
                map.put(i.getCatalogue(), new ArrayList<>());
            }
            map.get(i.getCatalogue()).add(i);
        }

        if (brand.isEmpty())
            brand = null;

        if (searchedItem != null) {
            return List.of(new CatalogueElastic(
                    searchedItem.getCatalogue(),
                    searchedItem.getCatalogueId(),
                    List.of(searchedItem),
                    brand));
        }

        String finalBrand = brand;
        return map.keySet().stream()
                .map(c -> new CatalogueElastic(
                        c,
                        map.get(c).get(0).getCatalogueId(),
                        map.get(c),
                        finalBrand))
                .collect(Collectors.toList());
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
            return notStrongSearch(text, words, needConvert, brand, pageable);
        }

        return getMatchingOrGroupByCatalogue(list, words, brand);
    }

    private String tryFindBrand(List<String> words, boolean needConvert, Pageable pageable) {
        if (words.size() > 1) {
            for (String word : new ArrayList<>(words)) {
                var list = repo.findAllByBrand(word, pageable);
                if (list.isEmpty() && needConvert) {
                    list = repo.findAllByBrand(convert(word), pageable);
                }
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
        String text = String.join(" ", words);

        List<ItemElastic> local = List.of();
        for (String queryWord : new ArrayList<>(words)) {
            local = repo.findAllByType(queryWord, pageable);
            if (local.isEmpty() && needConvert) {
                local = repo.findAllByType(convert(queryWord), pageable);
            }
            if (!local.isEmpty()) {
                if (words.size() > 1)
                    words.remove(queryWord);
                type = local.stream()
                        .map(ItemElastic::getType)
                        .min(Comparator.comparingInt(String::length))
                        .get();
            }
        }
//        List<ItemElastic> local = repo.findAllByType(text, pageable);
//        if (local.isEmpty() && needConvert) {
//            local = repo.findAllByType(convert(text), pageable);
//        }
//        if (!local.isEmpty()) {
//            type = local.stream()
//                    .map(ItemElastic::getType)
//                    .min(Comparator.comparingInt(String::length))
//                    .get();
//        }

        list.addAll(local);
        return type;
    }

    private Long tryFindCatalogueId(String brand, List<String> words, boolean needConvert, Pageable pageable) {
        String text = String.join(" ", words);

        if (brand.isEmpty()) {
            var list = repo.findByCatalogue(text, pageable);
            if (list.isEmpty() && needConvert) {
                list = repo.findByCatalogue(convert(text), pageable);
            }
            if (!list.isEmpty()) {
                return list.get(0).getCatalogueId();
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
                    list = repo.find(text, pageable);
                    if (list.isEmpty()) {
                        list = repo.find(convert(text), pageable);
                    }
                } else {
                    list = repo.findAllByType(text, type, pageable);
                    if (list.isEmpty()) {
                        list = repo.findAllByType(convert(text), type, pageable);
                    }
                }
            } else {
                type += "?";
                if (type.isEmpty()) {
                    list = repo.find(text, catalogueId, type, pageable);
                    if (list.isEmpty()) {
                        list = repo.find(convert(text), catalogueId, type, pageable);
                    }
                } else {
                    list = repo.find(text, catalogueId, pageable);
                    if (list.isEmpty()) {
                        list = repo.find(convert(text), catalogueId, pageable);
                    }
                }
            }
        } else {
            if (type.isEmpty()) {
                list = repo.findAllByBrand(text, brand, pageable);
                if (list.isEmpty()) {
                    list = repo.findAllByBrand(convert(text), brand, pageable);
                }
            } else {
                type += "?";
                list = repo.findAllByTypeAndBrand(text, brand, type, pageable);
                if (list.isEmpty()) {
                    list = repo.findAllByTypeAndBrand(convert(text), brand, type, pageable);
                }
            }
        }
        return list;
    }

    private List<CatalogueElastic> notStrongSearch(String text, List<String> words, boolean needConvert, String brand, Pageable pageable) {
        text += "?";
        List<ItemElastic> list = repo.findAllNotStrong(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = repo.findAllNotStrong(convert(text), pageable);
        }
        return getMatchingOrGroupByCatalogue(list, words, brand);
    }
}
