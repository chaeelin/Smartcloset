package com.example.smartcloset.User.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 86400000; // 일반 로그인 만료기간 : 1일

    public String generateToken(String loginId) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        System.out.println("Generating token. Current Time: " + now + ", Expiration Time: " + expiration);

        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now) // 발급 시간 설정
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // 자동 로그인 test을 위한.
    public String extractLoginId(String token) {
        String loginId = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody()
                .getSubject();
        System.out.println("Extracted Login ID from token: " + loginId);
        return loginId;
    }

    // token 유효성 확인을 위한
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractLoginId(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        System.out.println("Validating token for username: " + username + ". Token is valid: " + isValid);
        return isValid;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getExpiration();
        boolean isExpired = expiration.before(new Date());
        System.out.println("Token expiration time: " + expiration + ". Is token expired: " + isExpired);
        return isExpired;
    }

    // 만료 시간을 출력하는 메서드 추가
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        Date expiration = claims.getExpiration();
        System.out.println("Extracted expiration date from token: " + expiration);
        return expiration;
    }

    // HttpServletRequest에서 JWT 토큰 추출 (추가)
    public String extractTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 부분을 제거한 토큰 반환
        }
        return null;
    }
}
