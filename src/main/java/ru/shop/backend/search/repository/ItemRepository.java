package ru.shop.backend.search.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.shop.backend.search.model.ItemElastic;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<ItemElastic, Integer> {


    @Query("{" +
            "  \"multi_match\": {" +
            "    \"fields\": [" +
            "      \"type^2\"," +
            "      \"name^2\"," +
            "      \"description\"" +
            "    ]," +
            "    \"operator\": \"AND\"," +
            "    \"query\": \"?0\"," +
            "    \"fuzziness\": 1," +
            "    \"boost\": \"1\"," +
            "    \"analyzer\": \"russian\"" +
            "  }" +
            "}")
    List<ItemElastic> find(String name, Pageable pageable);

    @Query("{" +
            "  \"match\": {" +
            "    \"type\": {" +
            "      \"query\": \"?0\"," +
            "      \"fuzziness\": \"2\"" +
            "    }" +
            "  }" +
            "}")
    List<ItemElastic> findAllByType(String name, Pageable pageable);

    @Query("{" +
            "  \"match\": {" +
            "    \"brand\": {" +
            "      \"query\": \"?0\"," +
            "      \"fuzziness\": \"1\"," +
            "      \"boost\": \"1\"" +
            "    }" +
            "  }" +
            "}")
    List<ItemElastic> findAllByBrand(String name, Pageable pageable);

    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {" +
            "        \"multi_match\": {" +
            "          \"query\": \"?0\"," +
            "          \"fuzziness\": \"1\"," +
            "          \"boost\": \"1\"," +
            "          \"analyzer\": \"russian\"," +
            "          \"operator\": \"AND\"," +
            "          \"fields\": [" +
            "            \"name^4\"," +
            "            \"description\"," +
            "            \"type\"" +
            "          ]" +
            "        }" +
            "      }" +
            "    ]," +
            "    \"filter\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"brand\": \"?1\"" +
            "        }" +
            "      }" +
            "    ]" +
            "  }" +
            "}")
    List<ItemElastic> findAllByBrand(String text, String brand, Pageable pageable);

@Query("{" +
        "  \"match\": {" +
        "    \"fulltext\": {" +
        "      \"query\": \"?0\"," +
        "      \"fuzziness\": \"2\"" +
        "    }" +
        "  }" +
        "}")
    List<ItemElastic> findAllNotStrong(String text, Pageable pageable);

    @Query("{" +
            "  \"match\": {" +
            "    \"catalogue\": {" +
            "      \"query\": \"?0\"," +
            "      \"fuzziness\": \"1\"," +
            "      \"boost\": \"1\"" +
            "    }" +
            "  }" +
            "}")
    List<ItemElastic> findByCatalogue(String text, Pageable pageable);

    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {" +
            "        \"multi_match\": {" +
            "          \"query\": \"?0\"," +
            "          \"fuzziness\": \"1\"," +
            "          \"boost\": \"1\"," +
            "          \"analyzer\": \"russian\"," +
            "          \"operator\": \"AND\"," +
            "          \"fields\": [" +
            "            \"name^4\"," +
            "            \"description\"," +
            "            \"type\"" +
            "          ]" +
            "        }" +
            "      }" +
            "    ]," +
            "    \"filter\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"type\": \"?1\"" +
            "        }" +
            "      }" +
            "    ]" +
            "  }" +
            "}")
    List<ItemElastic> findAllByType(String text, String type, Pageable pageable);
    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {" +
            "        \"multi_match\": {" +
            "          \"query\": \"?0\"," +
            "          \"fuzziness\": 2," +
            "          \"boost\": \"1\"," +
            "          \"analyzer\": \"russian\"," +
            "          \"operator\": \"AND\"," +
            "          \"fields\": [" +
            "            \"name^4\"," +
            "            \"description\"," +
            "            \"type\"" +
            "          ]" +
            "        }" +
            "      }" +
            "    ]," +
            "    \"filter\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"brand\": \"?1\"" +
            "        }" +
            "      }" +
            "    ]" +
            "  }" +
            "}")
    List<ItemElastic> findAllByTypeAndBrand(String text, String brand, String type, Pageable pageable);

    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"type\": {" +
            "            \"query\": \"?0\"," +
            "            \"fuzziness\": \"2\"," +
            "            \"boost\": \"1\"" +
            "          }" +
            "        }" +
            "      }" +
            "    ]," +
            "    \"filter\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"catalogue_id\": \"?1\"" +
            "        }" +
            "      }" +
            "    ]" +
            "  }" +
            "}")
    List<ItemElastic> find(String text, Long catalogueId, Pageable pageable);
    @Query("{" +
            "  \"bool\": {" +
            "    \"must\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"type\": {" +
            "            \"query\": \"?0\"," +
            "            \"fuzziness\": \"2\"" +
            "          }" +
            "        }" +
            "      }" +
            "    ]," +
            "    \"filter\": [" +
            "      {" +
            "        \"match\": {" +
            "          \"type\": \"?2\"" +
            "        }" +
            "      }," +
            "      {" +
            "        \"match\": {" +
            "          \"catalogue_id\": \"?1\"" +
            "        }" +
            "      }" +
            "    ]" +
            "  }" +
            "}")
    List<ItemElastic> find(String text, Long catalogueId, String type, Pageable pageable);
    @Query("{" +
            "  \"term\": {" +
            "    \"item_id\": \"?0\"" +
            "  }" +
            "}")
    List<ItemElastic> findByItemId(String itemId, PageRequest of);

    @Query("{" +
            "  \"regexp\": {" +
            "    \"name\": \"?0\"" +
            "  }" +
            "}")
    List<ItemElastic> findAllByName(String name, Pageable pageable);
}