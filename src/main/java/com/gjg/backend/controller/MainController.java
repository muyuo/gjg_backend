package com.gjg.backend.controller;

import com.gjg.backend.model.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public Response hello() {
        Response response = new Response();
        response.setCode("200");
        response.setData("Hello World");
        return response;
    }
}
