package com.xlkk.security;

import com.xlkk.utils.MD5;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultPasswordEncoder implements PasswordEncoder {
    public DefaultPasswordEncoder() {
        this(-1);
    }
    public DefaultPasswordEncoder(int strength) {
    }

    @Override
    public String encode(CharSequence rawPassword) {
        //对字符号串进行md5加密
        return MD5.encrypt(rawPassword.toString());
    }

    /**
     * 进行密码比对
     * @param rawPassword 加密之后的密码
     * @param encodedPassword
     * @return
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        //该方法就是比对方法，判断加密后的密码和数据
        return encodedPassword.equals(MD5.encrypt(rawPassword.toString()));
    }
}
