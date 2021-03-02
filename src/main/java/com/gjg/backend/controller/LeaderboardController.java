package com.gjg.backend.controller;

import com.gjg.backend.BackendApplication;
import com.gjg.backend.model.LeaderboardResponse;
import com.gjg.backend.model.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @GetMapping("/{page}")
    public @ResponseBody
    Response getLeaderboard(@PathVariable int page) {
        Response response = new Response();

        if (page < 1) {
            page = 1;
        }

        LeaderboardResponse leaderboardResponse = BackendApplication.memory.getUsersByPage(page);
        response.setCode("200");
        response.setMessage("ok");
        response.setData(leaderboardResponse);

        return response;
    }

    @GetMapping("/{country_iso_code}/{page}")
    public @ResponseBody Response getLeaderboardByCountry(@PathVariable String country_iso_code, @PathVariable int page) {
        Response response = new Response();

        if (page < 1 ) {
            page = 1;
        }

        LeaderboardResponse leaderboardResponse = BackendApplication.memory.getLeaderboard(page, country_iso_code);
        response.setCode("200");
        response.setMessage("ok");
        response.setData(leaderboardResponse);

        return response;
    }

    @GetMapping("")
    public @ResponseBody
    Response getLeaderboard() {
        Response response = new Response();

        LeaderboardResponse leaderboardResponse = BackendApplication.memory.getUsersByPage(1);
        response.setCode("200");
        response.setMessage("ok");
        response.setData(leaderboardResponse);

        return response;
    }
}
