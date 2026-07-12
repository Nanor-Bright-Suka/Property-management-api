package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.dto.TokenPairDto;
import com.backend.hotelreservationapi.user_module.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationTokenService{

  private final JwtService jwtService;

  public TokenPairDto generateTokens(UserEntity user) {

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateAndStoreRefreshToken(user);

    return new TokenPairDto(
            accessToken,
            refreshToken
    );
  }

}
