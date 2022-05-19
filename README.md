# SpringSecurity_Demo
## 1.搭建项目整体框架
![img.png](E:\desktop\codeAndnote\code\aci_parent\STATIC\img.png)
其中总体是一个spring项目，而下面的模块都是maven

## 2.导入相关依赖
依赖统一由模块管理
~~~xml
<properties>
        <java.version>8</java.version>
        <mybatis-plus.version>3.5.0</mybatis-plus.version>
        <velocity.version>2.3</velocity.version>
        <swagger.version>2.7.0</swagger.version>
        <jwt.version>0.7.0</jwt.version>
        <fastjson.version>1.2.28</fastjson.version>
        <gson.version>2.8.2</gson.version>
        <json.version>20170516</json.version>
        <cloud-alibaba.version>0.2.2.RELEASE</cloud-alibaba.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <!--Spring Cloud-->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Hoxton.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!--mybatis-plus 持久层-->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- velocity 模板引擎, Mybatis Plus 代码生成器需要 -->
            <dependency>
                <groupId>org.apache.velocity</groupId>
                <artifactId>velocity-engine-core</artifactId>
                <version>${velocity.version}</version>
            </dependency>

            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>${gson.version}</version>
            </dependency>
            <!--swagger-->
            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-swagger2</artifactId>
                <version>${swagger.version}</version>
            </dependency>
            <!--swagger ui-->
            <dependency>
                <groupId>io.springfox</groupId>
                <artifactId>springfox-swagger-ui</artifactId>
                <version>${swagger.version}</version>
            </dependency>
            <!-- JWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt</artifactId>
                <version>${jwt.version}</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version>${fastjson.version}</version>
            </dependency>
            <dependency>
                <groupId>org.json</groupId>
                <artifactId>json</artifactId>
                <version>${json.version}</version>
            </dependency>


        </dependencies>
    </dependencyManagement>
~~~
然后其它子模块需要用到时，直接调用父模块的依赖即可

## 3.详细编写设计
先完成common模块的设计
### service_base
这里都是相关工具类的编写
![img_1.png](E:\desktop\codeAndnote\code\aci_parent\STATIC\img_1.png)
然后就是一些有关redis和swagger的配置类

### springsecurity

#### 密码处理工具类DefaultPasswordEncoder
使用MD5进行加密以及密码比对
~~~java
@Component
public class DefaultPasswordEncoder implements PasswordEncoder {
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
~~~

#### 退出处理器TokenLogoutHandler
生成token就需要使用到jwt。
jwt就是将用户信息生成很长的字符串，字符之间通过“.”分隔符分为三个子串
每个子串表示一个功能模块：jwt头、有效载荷和签名
于是我们想要创建一个TokenManager类来生成token
~~~java
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
~~~
退出处理器
就是需要删除token，包括请求头上的以及redis中的token
~~~java

@Component
//退出处理器
public class TokenLogoutHandler implements LogoutHandler {
    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;

    public TokenLogoutHandler(TokenManager tokenManager, RedisTemplate redisTemplate) {
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //从header里面获取token
        String token = request.getHeader("token");
        if(token!=null){
            //移出token
            tokenManager.removeToken(token);
            //通过token获取用户名
            String username = tokenManager.getUserInfoFromToken(token);
            redisTemplate.delete(username);
        }
        ResponseUtil.out(response, R.ok());
    }
}
~~~

#### 未认证授权处理
这里我们是继承了AuthenticationEntryPoint接口，它有一个commence方法
也就是说当未授权的时候，就会自动跳转到这个方法，我们就可以发送响应失败
~~~java
public class UnauthEntryPoint implements AuthenticationEntryPoint {
//当未授权的时候会执行这个方法
@Override
public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
ResponseUtil.out(response, R.error());
}
}
~~~
