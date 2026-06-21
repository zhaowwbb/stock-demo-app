package com.demo.stocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import com.demo.stocks.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class StockDemoApplication {
    private static final Logger log =
            LoggerFactory.getLogger(StockDemoApplication.class);

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SpringApplication app = new SpringApplication(StockDemoApplication.class);
        app.setApplicationStartup(new BufferingApplicationStartup(2048));
        app.run(args);
        log.info(
                "Startup Time = " +
                        (System.currentTimeMillis() - start) + " ms");
    }

    @PostConstruct
    public void init() {

        String result = userService.getUser("Tom");

        log.info(result);
    }
}
