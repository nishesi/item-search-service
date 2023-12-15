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

    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        List<CatalogueElastic> result = findByAnyOccurrence(text, pageable);
        if (result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    public List<CatalogueElastic> findByAnyOccurrence(String text, Pageable pageable) {
        String type = "";
        List<ItemElastic> list;
        String brand = "", text2 = text;
        Long catalogueId = null;
        boolean needConvert = true;
        if (isContainErrorChar(text)) {
            text = convert(text);
            needConvert = false;
        }
        if (needConvert && isContainErrorChar(convert(text))) {
            needConvert = false;
        }
        if (text.contains(" "))
            for (String queryWord : text.split("\\s")) {
                list = repo.findAllByBrand(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = repo.findAllByBrand(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "").trim().replace("  ", " ");
                    brand = list.get(0).getBrand();
                    break;

                }

            }
        list = repo.findAllByType(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = repo.findAllByType(convert(text), pageable);
        }
        if (!list.isEmpty()) {
            type = list.stream()
                    .map(ItemElastic::getType)
                    .min(Comparator.comparingInt(String::length))
                    .get();
        } else {
            for (String queryWord : text.split("\\s")) {
                list = repo.findAllByType(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = repo.findAllByType(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "");
                    type = list.stream()
                            .map(ItemElastic::getType)
                            .min(Comparator.comparingInt(String::length))
                            .get();
                }
            }
        }
        if (brand.isEmpty()) {
            list = repo.findByCatalogue(text, pageable);
            if (list.isEmpty() && needConvert) {
                list = repo.findByCatalogue(convert(text), pageable);
            }
            if (!list.isEmpty()) {
                catalogueId = list.get(0).getCatalogueId();
            }
        }
        text = text.trim();
        if (text.isEmpty() && !brand.isEmpty())
            return List.of(new CatalogueElastic(
                    list.get(0).getCatalogue(),
                    list.get(0).getCatalogueId(),
                    null,
                    brand));
        text += "?";
        if (brand.isEmpty()) {
            type += "?";
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

        if (list.isEmpty()) {
            if (text2.contains(" "))
                text = String.join(" ", text.split("\\s"));
            text2 += "?";
            list = repo.findAllNotStrong(text2, pageable);
            if (list.isEmpty() && needConvert) {
                list = repo.findAllByTypeAndBrand(convert(text2), brand, type, pageable);
            }
        }
        return getMatchingOrGroupByCatalogue(list, text, brand);
    }

    private static List<CatalogueElastic> getMatchingOrGroupByCatalogue(List<ItemElastic> list, String text, String brand) {
        text = text.replace("?", "");
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
}
