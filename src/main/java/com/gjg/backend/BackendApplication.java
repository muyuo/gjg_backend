package com.gjg.backend;

import com.gjg.backend.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static List<User> users = new LinkedList<>();

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
