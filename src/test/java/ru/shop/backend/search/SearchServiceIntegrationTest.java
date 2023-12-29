package ru.shop.backend.search;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.shop.backend.search.chain.SearchChain;
import ru.shop.backend.search.dto.CatalogueElastic;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.repository.ItemJpaRepository;
import ru.shop.backend.search.service.SearchService;
import ru.shop.backend.search.util.SimplePostgresContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SearchApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnableAutoConfiguration(exclude = {ElasticsearchRestClientAutoConfiguration.class})
public class SearchServiceIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new SimplePostgresContainer()
            .withInitScript("SearchService-test-schema.sql");

    @MockBean
    SearchChain searchChain;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    SearchService searchService;

    @MockBean
    ElasticsearchTemplate elasticsearchTemplate;
    @MockBean
    ItemElasticRepository itemElasticRepository;

    @BeforeAll
    static void setUp() {
        postgres.start();
    }

    @AfterAll
    static void destroy() {
        postgres.stop();
    }

    @Nested
    class test_method_getSearchResult {

        @Nested
        class test_catalogues {
            @Test
            void should_return_only_from_SearchChain() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 105L, List.of(
                                new ItemElastic(null, null, 10L, 105L, null, null, null, null)
                        ), null),
                        new CatalogueElastic(null, 106L, List.of(
                                new ItemElastic(null, null, 11L, 106L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var categories = searchService.getSearchResult(1, "any").getCategories();

                assertThat(categories)
                        .hasSize(2)
                        .anySatisfy(cat -> assertThat(cat)
                                .hasFieldOrPropertyWithValue("name", "cat_name_5"))
                        .anySatisfy(cat -> assertThat(cat)
                                .hasFieldOrPropertyWithValue("name", "cat_name_6"));
            }

            @Test
            void should_return_by_unique_url() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 110L, List.of(
                                new ItemElastic(null, null, 15L, 110L, null, null, null, null)
                        ), null),
                        new CatalogueElastic(null, 111L, List.of(
                                new ItemElastic(null, null, 16L, 111L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var categories = searchService.getSearchResult(1, "any").getCategories();

                assertThat(categories)
                        .hasSize(1)
                        .allSatisfy(cat -> assertThat(cat).satisfiesAnyOf(
                                category -> assertThat(category)
                                        .hasFieldOrPropertyWithValue("name", "cat_name_10"),
                                category -> assertThat(category)
                                        .hasFieldOrPropertyWithValue("name", "cat_name_11")));
            }

            @Test
            void should_return_url_with_brand() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 112L, List.of(
                                new ItemElastic(null, null, 17L, 112L, null, null, null, null)
                        ), "Apple")
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var categories = searchService.getSearchResult(1, "any").getCategories();

                assertThat(categories)
                        .hasSize(1)
                        .allSatisfy(cat -> assertThat(cat)
                                .hasFieldOrPropertyWithValue("name", "cat_name_12")
                                .hasFieldOrPropertyWithValue("url", "/cat/smartphones/brands/apple")
                                .hasFieldOrPropertyWithValue("parentUrl", "/cat/techniques")
                        );
            }

            @Test
            void should_return_url_without_brand() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 112L, List.of(
                                new ItemElastic(null, null, 17L, 112L, null, "", null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var categories = searchService.getSearchResult(1, "any").getCategories();

                assertThat(categories)
                        .hasSize(1)
                        .allSatisfy(cat -> assertThat(cat)
                                .hasFieldOrPropertyWithValue("name", "cat_name_12")
                                .hasFieldOrPropertyWithValue("url", "/cat/smartphones")
                                .hasFieldOrPropertyWithValue("parentUrl", "/cat/techniques")
                        );
            }
        }
    }

    @Nested
    class bug_fix {
        @Nested
        class test_items {
            @Test
            void should_return_all_from_searchChain() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 101L, List.of(
                                new ItemElastic(null, null, 1L, 101L, null, null, null, null),
                                new ItemElastic(null, null, 2L, 101L, null, null, null, null)
                        ), null),
                        new CatalogueElastic(null, 102L, List.of(
                                new ItemElastic(null, null, 3L, 102L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var items = searchService.getSearchResult(1, "any").getItems();

                assertThat(items)
                        .hasSize(3)
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 1L))
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 2L))
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 3L));
            }

            @Test
            void should_return_only_containing_in_remain_and_in_item() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 103L, List.of(
                                new ItemElastic(null, null, 4L, 103L, null, null, null, null),
                                new ItemElastic(null, null, 5L, 103L, null, null, null, null),
                                new ItemElastic(null, null, 6L, 103L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var items = searchService.getSearchResult(2, "any").getItems();

                assertThat(items)
                        .hasSize(1)
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 5L));
            }

            @Test
            void should_return_with_correct_region() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 103L, List.of(
                                new ItemElastic(null, null, 7L, 103L, null, null, null, null),
                                new ItemElastic(null, null, 8L, 103L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var items = searchService.getSearchResult(3, "any").getItems();

                assertThat(items)
                        .hasSize(2)
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 7L)
                                .hasFieldOrPropertyWithValue("price", 101001001000L))
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 8L)
                                .hasFieldOrPropertyWithValue("price", 30L));
            }

            @Test
            void should_return_with_correct_data() {
                var catalogues = List.of(
                        new CatalogueElastic(null, 104L, List.of(
                                new ItemElastic(null, null, 9L, 104L, null, null, null, null)
                        ), null)
                );
                when(searchChain.searchByText(any(), any())).thenReturn(catalogues);
                var items = searchService.getSearchResult(1, "any").getItems();

                assertThat(items)
                        .hasSize(1)
                        .anySatisfy(item -> assertThat(item)
                                .hasFieldOrPropertyWithValue("itemId", 9L)
                                .hasFieldOrPropertyWithValue("name", "Kit Kat")
                                .hasFieldOrPropertyWithValue("url", "kit-kat-1")
                                .hasFieldOrPropertyWithValue("image", "kit-kat-image.jpg")
                                .hasFieldOrPropertyWithValue("cat", "Chocolate Bar")
                                .hasFieldOrPropertyWithValue("price", 10L));
            }
        }
    }
}
