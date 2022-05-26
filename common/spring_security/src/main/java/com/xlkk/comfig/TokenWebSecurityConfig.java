package com.xlkk.comfig;

import com.xlkk.filter.TokenAuthFilter;
import com.xlkk.filter.TokenLoginFilter;
import com.xlkk.security.DefaultPasswordEncoder;
import com.xlkk.security.TokenLogoutHandler;
import com.xlkk.security.TokenManager;
import com.xlkk.security.UnauthEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TokenWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;
    private DefaultPasswordEncoder defaultPasswordEncoder;
    private UserDetailsService userDetailsService;

    @Autowired
    public TokenWebSecurityConfig(TokenManager tokenManager, RedisTemplate redisTemplate, DefaultPasswordEncoder defaultPasswordEncoder, UserDetailsService userDetailsService) {
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling()
                .authenticationEntryPoint(new UnauthEntryPoint())//没有权限访问的话，进入到该处理器
                .and().csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and().logout().logoutUrl("adming/acl/index/logout")//设置退出登录的路径
                .addLogoutHandler(new TokenLogoutHandler(tokenManager,redisTemplate)).and()//设置退出登录的处理器
                .addFilter(new TokenLoginFilter(tokenManager, redisTemplate, authenticationManager()))
                .addFilter(new TokenAuthFilter(authenticationManager(),tokenManager,redisTemplate)).httpBasic();

    }
    //调用UserDetailService和密码处理
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(defaultPasswordEncoder);
    }
    //不进行认证的路径，可以直接访问
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/**");
    }
}
