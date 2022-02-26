package com.vancone.excode.generator;

import com.vancone.excode.generator.config.property.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Tenton Lien
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.vancone.excode" })
//@EnableEurekaClient
@EnableConfigurationProperties(value = { SystemConfig.class })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}