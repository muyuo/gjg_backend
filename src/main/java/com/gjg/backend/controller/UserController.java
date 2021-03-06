package com.gjg.backend.controller;

import com.github.javafaker.Faker;
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
            BackendApplication.memory.indexMap.put(user.getId(), i);
        }
    }

    /*
     *
     * POST MAPPINGS
     *
     */

    @PostMapping("user/create")
    public @ResponseBody
    Response addUser(@RequestBody UserPost userObj) {
        Response response = new Response();

        try {
            if (userRepository.existsByDisplayName(userObj.display_name)) {
                response.setCode("500");
                response.setMessage("Display name already taken.");
                return response;
            }

            if (userObj.points < 0) {
                response.setCode("500");
                response.setMessage("Point must be greater than 0");
                return response;
            }

            if (userObj.display_name.trim().equals("")) {
                response.setCode("500");
                response.setMessage("Display name cannot be empty.");
                return response;
            }

            User newUser = new User();
            newUser.setId(UUID.fromString(userObj.user_id));
            newUser.setDisplayName(userObj.display_name);
            newUser.setPoints(userObj.points);
            newUser.setCountry(userObj.country);
            userRepository.save(newUser);


            newUser.setPoints(0);
            BackendApplication.memory.addUser(newUser);
            BackendApplication.memory.updatePointsOfUser(newUser.getId(), userObj.points, null);


            UserCreateRespond userRespond = new UserCreateRespond();
            userRespond.user_id = userObj.user_id;
            userRespond.country = userObj.country;
            userRespond.rank = BackendApplication.memory.indexMap.get(newUser.getId()) + 1;
            userRespond.display_name = userObj.display_name;
            userRespond.points = userObj.points;
            response.setCode("200");
            response.setMessage("ok");
            response.setData(userRespond);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode("500");
            response.setMessage("fail");
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

        response = BackendApplication.memory.updatePointsOfUser(UUID.fromString(scoreBody.user_id), scoreBody.score_worth, this);

        return response;
    }

    @PostMapping("/user/seed")
    public @ResponseBody
    Response seedUsers(@RequestBody UserSeedRequestBody seedRequestBody) {
        Response response = new Response();

        if (seedRequestBody.number_of_users < 1) {
            response.setCode("500");
            response.setMessage("number_of_user must be greater than 0");
            return response;
        }

        for (int i = 0; i < seedRequestBody.number_of_users; i++) {
            Faker faker = new Faker();
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setPoints(0);
            user.setCountry(seedRequestBody.country);
            user.setDisplayName(faker.name().fullName());

            while(userRepository.existsByDisplayName(user.getDisplayName())) {
                user.setDisplayName(user.getDisplayName() + i);
            }

            BackendApplication.memory.addUser(user);
            userRepository.save(user);
        }

        response.setCode("200");
        response.setMessage("ok");
        return response;
    }

    /*
     *
     * GET MAPPINGS
     *
     */

    @GetMapping("user/all")
    public @ResponseBody
    Response getAllUsers() {
        Response response = new Response();
        response.setCode("200");
        response.setData(BackendApplication.memory.getUsers());

        return response;
    }

    @GetMapping("user/profile/{id}")
    public @ResponseBody
    Response getUserById(@PathVariable String id) {
        Response response = new Response();
        Integer index = BackendApplication.memory.indexMap.get(UUID.fromString(id));

        if (index == null) {
            response.setCode("500");
            response.setMessage("User not found.");
            return response;
        }

        User user = BackendApplication.memory.getUsers().get(index);
        UserProfileRespond profileRespond = new UserProfileRespond();
        profileRespond.user_id = user.getId();
        profileRespond.display_name = user.getDisplayName();
        profileRespond.points = user.getPoints();
        profileRespond.rank = index + 1;

        response.setCode("200");
        response.setMessage("ok");
        response.setData(profileRespond);
        return response;
    }
}
