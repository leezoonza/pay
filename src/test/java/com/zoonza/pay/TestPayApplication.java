package com.zoonza.pay;

import org.springframework.boot.SpringApplication;

public class TestPayApplication {

    public static void main(String[] args) {
        SpringApplication.from(PayApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
