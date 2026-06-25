package com.backend.hotelreservationapi.user_module.dto.request;

import com.backend.hotelreservationapi.user_module.enums.PropertyApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateApplicationStatusRequestDto {

    @NotNull(message = "Invalid application status")
    PropertyApplicationStatus applicationStatus;

    @NotBlank(message = "Comment should not be empty")
    @Size(message="Comment  too short", min = 3)
    String comment;


}
