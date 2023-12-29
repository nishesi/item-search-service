package ru.shop.backend.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.shop.backend.search.model.ItemEntity;
import ru.shop.backend.search.model.ItemWithPrice;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ItemJpaRepository extends JpaRepository<ItemEntity, Long> {
    @Query(value = """
            select i.item_id as itemId,
                   i.name,
                   r.price,
                   i.url,
                   i.image,
                   i.type,
                   i.catalogue_id as catalogueId
            from item i join remain r
               on r.item_id = i.item_id and r.region_id = :regionId
            where i.item_id in :ids""", nativeQuery = true)
    List<ItemWithPrice> findAllByRegionIdAndIdIn(Integer regionId, List<Long> ids);

    @Query(value = """
            select i.*,
                   c.name as catalogue,
                   b.name as brand
            from (select i.*
                  from item i
                  where item_id = (select item_id
                                   from item_sku
                                   where sku = :sku)) i
                inner join catalogue c using(catalogue_id)
                    inner join brand b using (brand_id)
            """, nativeQuery = true)
    Optional<ItemEntity> findBySku(String sku);

    @Query(value = """
            select i.*,
                   c.name as catalogue,
                   b.name as brand
            from item i
                inner join catalogue c using(catalogue_id)
                    inner join brand b using (brand_id)
            """, nativeQuery = true)
    Stream<ItemEntity> streamAllBy();
}
