package com.gjg.backend.controller;

import com.gjg.backend.BackendApplication;
import com.gjg.backend.Scanner;
import com.gjg.backend.model.*;
import com.gjg.backend.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUserToDb(Object user) {
        userRepository.save((User) user);
    }

    @PostMapping("user/create")
    public @ResponseBody
    Response addUser(@RequestBody UserPost userObj) {
        Response response = new Response();

        try {
            User newUser = new User();
            newUser.setId(UUID.fromString(userObj.user_id));
            newUser.setDisplay_name(userObj.display_name);
            newUser.setPoints(userObj.points);

            BackendApplication.memory.addUser(newUser);
            Scanner.addTask(new Task(newUser, this::saveUserToDb, "create_user"));
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

    @PostMapping("score/submit")
    public @ResponseBody
    Response updateScore(@RequestBody ScoreRequestBody scoreBody) {
        Response response = new Response();
        Integer index = BackendApplication.memory.indexMap.get(UUID.fromString(scoreBody.user_id));
        if (index == null) {
            response.setCode("500");
            response.setMessage("User not found.");
            return response;
        }

        if (scoreBody.score_worth < 0) {
            response.setCode("500");
            response.setMessage("Score worth cannot be less than 0");
            return response;
        }

        Scanner.addTask(new Task(BackendApplication.memory.getUsers().get(index), this::saveUserToDb, "Score update"));
        BackendApplication.memory.updatePointsOfUser(UUID.fromString(scoreBody.user_id), scoreBody.score_worth);

        response.setCode("200");
        response.setMessage("ok");

        return response;
    }

    @GetMapping("user/all")
    public @ResponseBody Response getAllUsers() {
        Response response = new Response();
        response.setCode("200");
        response.setData(BackendApplication.memory.getUsers());

        return response;
    }

    @PostConstruct
    public void readAllUsersOnStartup() {
        Iterator<User> iterator = userRepository.findAll().iterator();

        while (iterator.hasNext()) {
            User user = iterator.next();
            BackendApplication.memory.addUser(user);
        }

        BackendApplication.memory.getUsers().sort(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return (int) (o2.getPoints() - o1.getPoints());
            }
        });

        for (int i = 0; i < BackendApplication.memory.getUsers().size(); i++) {
            User user = BackendApplication.memory.getUsers().get(i);
            user.setRank(i + 1);
            BackendApplication.memory.indexMap.put(user.getId(), i);
        }
    }
}