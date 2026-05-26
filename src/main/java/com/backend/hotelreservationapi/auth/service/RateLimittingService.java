package com.backend.hotelreservationapi.auth.service;


import com.backend.hotelreservationapi.auth.dto.EmailRequestDto;
import com.backend.hotelreservationapi.auth.enums.OtpRequestStatusEnum;
import com.backend.hotelreservationapi.auth.exception.RateLimitExceededException;
import com.backend.hotelreservationapi.auth.util.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimittingService {

    private final Utility utility;

    public void checkLimits(EmailRequestDto email,String ip)  {

        try {

            utility.checkEmailLimit(email);
            utility.checkIpLimit(ip);
            utility.log(email.email(), ip, "OK", OtpRequestStatusEnum.ALLOWED);

        } catch (RateLimitExceededException ex) {
            utility.log(email.email(), ip, ex.getMessage(), OtpRequestStatusEnum.BLOCKED);

            throw ex;
        }
    }


}
