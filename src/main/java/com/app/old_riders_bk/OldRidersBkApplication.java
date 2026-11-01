package com.app.old_riders_bk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class OldRidersBkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OldRidersBkApplication.class, args);
    }

}
