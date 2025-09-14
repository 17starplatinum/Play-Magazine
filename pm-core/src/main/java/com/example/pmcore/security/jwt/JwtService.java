package com.example.pmcore.security.jwt;

import com.example.backend.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class JwtService {

    /**
     * Извлечение имени пользователя из токена
     *
     * @param token токен
     * @return имя пользователя
     */
    public String extractUserName(String token) throws ExpiredJwtException {
        return extractClaim(token, Claims::getSubject);
    }

    public final JwtConfig jwtConfig;

    /**
     * Генерация токена
     *
     * @param userDetails данные пользователя
     * @return токен
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("email", customUserDetails.getEmail());
            claims.put("role", customUserDetails.getRole());
        }
        return generateToken(claims, userDetails);
    }

    /**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @param userDetails данные пользователя
     * @return true, если токен валиден
     */
    public boolean isTokenValid(String token, UserDetails userDetails) throws ExpiredJwtException {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Извлечение данных из токена
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(new Date().getTime() + jwtConfig.getJwtExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getJwtSigningKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}