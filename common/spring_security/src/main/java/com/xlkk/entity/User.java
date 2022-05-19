package com.xlkk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String nickName;

    private String salt;

    private String token;

    private int isDelete;

    private Date gmtCreate;

    private Date gmtModified;

}
