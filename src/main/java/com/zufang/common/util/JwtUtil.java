package com.zufang.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {

    // 密钥（使用固定的密钥字符串，至少32位）
    private static final String SECRET_KEY_STRING = "zufangSystemSecretKey123456789012345678901234567890";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    
    // token有效期（24小时）
    private static final long EXPIRATION = 86400000L;
    
    // token前缀
    public static final String TOKEN_PREFIX = "Bearer ";
    
    // 存放token的Header Key
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 生成token
     * @param userId 用户id
     * @param username 用户名
     * @param role 角色
     * @return token
     */
    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 生成token
     * @param claims 要包含在token中的数据
     * @return token
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析token
     * @param token token
     * @return Claims
     */
    public static Map<String, Object> parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", claims.get("userId"));
        result.put("username", claims.get("username"));
        result.put("role", claims.get("role"));
        result.put("exp", claims.getExpiration());
        
        return result;
    }

    /**
     * 判断token是否过期
     * @param token token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取用户ID
     * @param token token
     * @return 用户ID
     */
    public static String getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            return claims.get("userId").toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户名
     * @param token token
     * @return 用户名
     */
    public static String getUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            return claims.get("username").toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取角色
     * @param token token
     * @return 角色
     */
    public static String getRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            return claims.get("role").toString();
        } catch (Exception e) {
            return null;
        }
    }
} 