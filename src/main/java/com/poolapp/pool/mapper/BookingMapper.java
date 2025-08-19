package com.poolapp.pool.mapper;

import com.poolapp.pool.dto.BookingDTO;
import com.poolapp.pool.dto.requestDTO.RequestBookingDTO;
import com.poolapp.pool.model.Booking;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SessionMapper.class}, builder = @Builder(disableBuilder = true),
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", source = "sessionDTO")
    Booking toEntity(BookingDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", source = "requestSessionDTO")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Booking toEntity(RequestBookingDTO dto);

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "sessionDTO", source = "session")
    BookingDTO toDto(Booking booking);

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "sessionDTO", source = "session")
    List<BookingDTO> toDtoList(List<Booking> bookings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "session", source = "sessionDTO")
    void updateBookingFromDto(@MappingTarget Booking booking, BookingDTO dto);
}
