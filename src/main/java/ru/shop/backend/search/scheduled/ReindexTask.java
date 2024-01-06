package ru.shop.backend.search.scheduled;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.converter.ItemConverter;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.repository.ItemJpaRepository;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexTask {
    private static final int PAGE_SIZE = 50_000;
    private final ItemJpaRepository itemJpaRepository;
    private final ItemElasticRepository itemElasticRepository;
    private final ItemConverter itemConverter;
    private final ExecutorService executorService = Executors.newFixedThreadPool(30);

    @Scheduled(fixedDelay = 12, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void reindex() throws ExecutionException, InterruptedException {
        log.info("генерация индексов по товарам запущена");

        int lastInd = (int) (itemJpaRepository.count() / PAGE_SIZE + 1);
        var tasks = IntStream.range(0, lastInd)
                .mapToObj(i -> executorService.submit(reindexPageTask(i)))
                .toList();
        for (Future<?> f : tasks)
            f.get();

        log.info("генерация индексов по товарам закончилась");
    }

    private Runnable reindexPageTask(int i) {
        return () -> {
            List<ItemElastic> list = itemJpaRepository
                    .findListBy(PageRequest.of(i, PAGE_SIZE))
                    .stream().parallel()
                    .map(itemConverter::toItemElastic)
                    .toList();
            itemElasticRepository.saveAll(list);
        };
    }
}
