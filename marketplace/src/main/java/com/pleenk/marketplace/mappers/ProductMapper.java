package com.pleenk.marketplace.mappers;

import com.pleenk.marketplace.dto.ProductResponseDTO;
import com.pleenk.marketplace.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductResponseDTO toDTO(Product product);
}