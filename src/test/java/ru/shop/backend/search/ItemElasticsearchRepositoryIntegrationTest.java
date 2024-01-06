package ru.shop.backend.search;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.scheduled.ReindexTask;
import ru.shop.backend.search.util.SimpleElasticsearchContainer;
import ru.shop.backend.search.util.SimplePostgresContainer;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// if run any test case separately, make sure, that scheduled task is done
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SearchApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemElasticsearchRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final ElasticsearchContainer elastic = new SimpleElasticsearchContainer();

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new SimplePostgresContainer()
            .withInitScript("ItemRepository-test-schema.sql");

    @Autowired
    ElasticsearchOperations template;

    @Autowired
    ItemElasticRepository itemElasticRepository;

    @Autowired
    ReindexTask reindexTask;


    @BeforeAll
    static void setUp() {
        elastic.start();
        postgres.start();
    }

    @AfterAll
    static void destroy() {
        elastic.stop();
        postgres.stop();
    }

    @Test
    @Order(0)
    void reindex() throws ExecutionException, InterruptedException {
        reindexTask.reindex();
    }

    @Test
    void should_return_first_result() {
        assertThatNoException()
                .isThrownBy(() -> {
                    Optional<ItemElastic> result = itemElasticRepository.findByItemId(1L);
                    assertThat(result)
                            .isPresent()
                            .get()
                            .hasFieldOrPropertyWithValue("itemId", 1L);
                });
    }
}
