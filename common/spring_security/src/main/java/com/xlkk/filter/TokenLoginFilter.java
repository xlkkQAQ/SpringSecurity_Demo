package com.xlkk.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlkk.entity.SecurityUser;
import com.xlkk.entity.User;
import com.xlkk.security.TokenManager;
import com.xlkk.utils.R;
import com.xlkk.utils.ResponseUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
/**
 * 认证过滤器
 */
//通过继承UsernamePasswordAuthenticationFilter实现它的几个方法来实现自己的逻辑
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {
    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;
    private AuthenticationManager authenticationManager;

    public TokenLoginFilter(TokenManager tokenManager, RedisTemplate redisTemplate, AuthenticationManager authenticationManager) {
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
        this.authenticationManager = authenticationManager;
        this.setPostOnly(false);
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("admin/acl/login","POST"));
    }

    //获取表单提交的用户名和密码
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword(),new ArrayList<>()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    //认证成功后调用的方法
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        //认证成功之后，得到认证成功之后的用户信息
        SecurityUser user = (SecurityUser) authResult.getPrincipal();
        //根据用户名生成token
        String token = tokenManager.createToken(user.getCurrentUserInfo().getUsername());
        //把用户名称和用户权限列表放到redis里面去
        redisTemplate.opsForValue().set(user.getCurrentUserInfo().getUsername(),user.getPermissionValueList());
        //将token返回
        ResponseUtil.out(response, R.ok().data("token",token));
    }

    //认证失败会调用的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ResponseUtil.out(response,R.error());
    }
}
