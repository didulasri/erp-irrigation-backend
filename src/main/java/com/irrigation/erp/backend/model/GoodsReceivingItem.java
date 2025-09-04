package com.irrigation.erp.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "goods_receiving_item")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class GoodsReceivingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grn_id")
    private GRN grn;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private Integer quantity;


    private String unit;



}
