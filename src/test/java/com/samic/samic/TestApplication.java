package com.samic.samic;
import org.springframework.boot.SpringApplication;


public class TestApplication {

    public static void main(String[] args){
        SpringApplication.from(Application::main)
                .with(TestContainerConfiguration.class)
                .run(args);
    }
}
