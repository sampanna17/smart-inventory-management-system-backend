package com.smartinventorysystem.modules.productimage.entity;

import com.smartinventorysystem.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Product_Images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ImageID")
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String imageURL;

    @Column(name = "PublicID", nullable = false)
    private String publicId;
}
