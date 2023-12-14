package ru.shop.backend.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.shop.backend.search.model.ItemEntity;

import java.util.List;
import java.util.stream.Stream;

public interface ItemDbRepository  extends JpaRepository<ItemEntity, Long> {
    @Query(value = "select      " +
            "i.item_id,         " +
            "i.name,            " +
            "r.price,           " +
            "i.itemurl as url,  " +
            "i as image, i.type " +
            "from item as i join remain as r " +
            "   on r.item_id = i.item_id and r.region_id = :regionId " +
            "where i.item_id in  :ids", nativeQuery = true)
    List<Object[]> findByIds(Integer regionId, List<Long> ids);

    @Query(value = "select item_id from item_sku where sku = ?", nativeQuery = true)
    List<Integer> findBySku(String parseInt);

    @Query(value = "select i.* from item as i", nativeQuery = true)
    Stream<ItemEntity> findAllInStream();

    @Query(value = "select distinct       " +
            "c.name,                      " +
            "cp.name as parent_name,      " +
            "c.realcatname as url ,       " +
            "cp.realcatname as parent_url," +
            "c.image                      " +
            "from item as i               " +
            "   join catalogue as c using(catalogue_id) " +
            "       join catalogue cp on cp.catalogue_id  = c.parent_id " +
            "where i.item_id in :ids", nativeQuery = true)
    List<Object[]> findCatsByIds(List<Integer> ids);
}
