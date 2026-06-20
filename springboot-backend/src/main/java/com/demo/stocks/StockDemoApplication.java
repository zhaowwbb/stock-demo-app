package com.demo.stocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import com.demo.stocks.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockDemoApplication {

    @Autowired
    private UserService userService;    

    public static void main(String[] args) {
        SpringApplication.run(StockDemoApplication.class, args);
    }

    @PostConstruct
    public void init(){

        String result = userService.getUser("Tom");

        System.out.println(result);
    }    
}
