package com.gjg.backend.controller;

import com.gjg.backend.BackendApplication;
import com.gjg.backend.Scanner;
import com.gjg.backend.model.Response;
import com.gjg.backend.model.Task;
import com.gjg.backend.model.User;
import com.gjg.backend.model.UserPost;
import com.gjg.backend.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUserToDb(Object user) {
        userRepository.save((User) user);
    }

    @PostMapping("/create")
    public @ResponseBody
    Response addUser(@RequestBody UserPost userObj) {
        Response response = new Response();

        try {
            User newUser = new User();
            newUser.setId(UUID.fromString(userObj.user_id));
            newUser.setDisplay_name(userObj.display_name);
            newUser.setPoints(userObj.points);

            BackendApplication.users.add(newUser);
            Scanner.addTask(new Task(newUser, this::addUserToDb, "create_user"));
            response.setCode("200");
            response.setMessage("ok");
            response.setData(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode("500");
            response.setMessage("Fail");
        }
        return response;
    }

    @GetMapping("/all")
    public @ResponseBody Response getAllUsers() {
        Response response = new Response();
        response.setCode("200");
        response.setData(BackendApplication.users);

        return response;
    }

    @PostConstruct
    public void readAllUsersOnStartup() {
        Iterator<User> iterator = userRepository.findAll().iterator();

        while (iterator.hasNext()) {
            User user = iterator.next();
            BackendApplication.users.add(user);
        }
    }
}
