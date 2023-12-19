package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static ru.shop.backend.search.util.SearchUtils.findWithConvert;

@RequiredArgsConstructor
public abstract class TypeMatchingAbstractSearch {
    protected final ItemElasticRepository itemElasticRepository;

    protected String tryFindType(List<String> words, boolean needConvert, Pageable pageable) {
        String type = "";

        List<ItemElastic> local;
        for (Iterator<String> iterator = words.iterator(); iterator.hasNext(); ) {
            String word = iterator.next();
            local = findWithConvert(word, needConvert, t -> itemElasticRepository.findAllByType(t, pageable));
            if (!local.isEmpty()) {
                type = local.stream()
                        .map(ItemElastic::getType)
                        .min(Comparator.comparingInt(String::length))
                        .get();
                if (words.size() > 1)
                    iterator.remove();
            }
        }
        return type;
    }
}
