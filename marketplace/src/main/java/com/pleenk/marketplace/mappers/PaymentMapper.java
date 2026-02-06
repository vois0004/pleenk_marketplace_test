package com.pleenk.marketplace.mappers;
import com.pleenk.marketplace.dto.PaymentResponseDTO;
import com.pleenk.marketplace.entities.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    PaymentResponseDTO toDTO(Payment payment);
}