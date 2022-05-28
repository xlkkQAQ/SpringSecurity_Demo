package com.xlkk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplicaiton {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplicaiton.class,args);
    }
}
