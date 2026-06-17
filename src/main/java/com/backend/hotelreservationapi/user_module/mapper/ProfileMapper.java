package com.backend.hotelreservationapi.user_module.mapper;


import com.backend.hotelreservationapi.user_module.dto.ProfileResponseDto;
import com.backend.hotelreservationapi.user_module.entity.ProfileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponseDto toDto(ProfileEntity profile);






}
