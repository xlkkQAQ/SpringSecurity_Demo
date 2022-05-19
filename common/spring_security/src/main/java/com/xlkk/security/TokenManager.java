package com.xlkk.security;

import io.jsonwebtoken.CompressionCodec;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenManager {
    //token有效时长
    private long tokenEncpiration = 24*60*60*1000;

    //编码秘钥
    private String tokenSignKey = "123456";

    //使用jwt，根据用户名生成token
    public String createToken(String username){
        String token = Jwts.builder().setSubject(username) //需要加密的对象
                .setExpiration(new Date(System.currentTimeMillis()+tokenEncpiration)) //设置过期时间
                .signWith(SignatureAlgorithm.HS512,tokenSignKey) //设置编码秘钥
                .compressWith(CompressionCodecs.GZIP).compact();
        return token;
    }

    //根据token字符串得到用户信息
    public String getUserInfoFromToken(String token){
        String userInfo = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token).getBody().getSubject();
        return userInfo;
    }
    //删除token
    public void removeToken(String token){}


}
