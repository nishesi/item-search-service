package ru.shop.backend.search.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "item")
public class ItemEntity {
    private String name;
    private String brand;
    private String catalogue;
    private String type;
    private String description;
    private long brandId;
    private long catalogueId;
    @Id
    private long itemId;
}
