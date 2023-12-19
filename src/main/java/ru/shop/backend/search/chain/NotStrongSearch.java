package ru.shop.backend.search.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;

import java.util.List;
import java.util.Optional;

import static ru.shop.backend.search.util.SearchUtils.findWithConvert;
import static ru.shop.backend.search.util.SearchUtils.groupByCatalogue;

@Component
@RequiredArgsConstructor
public class NotStrongSearch implements SearchLink<List<CatalogueElastic>> {
    private final ItemElasticRepository itemElasticRepository;
    @Override
    public Optional<List<CatalogueElastic>> find(String text, Pageable pageable) {
        var list = findByNotStrongSearch(text, pageable);
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list);
    }
    private List<CatalogueElastic> findByNotStrongSearch(String text, Pageable pageable) {
        text += "_";
        var list = findWithConvert(text, true,
                t -> itemElasticRepository.findAllNotStrong(t, pageable));
        return groupByCatalogue(list, "");
    }
}
