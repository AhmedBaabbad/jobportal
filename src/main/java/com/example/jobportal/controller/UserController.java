package com.example.jobportal.controller;

import com.example.jobportal.model.Users;
import com.example.jobportal.model.UsersType;
import com.example.jobportal.service.UsersService;
import com.example.jobportal.service.UsersTypeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UserController {

    private final UsersTypeService usersTypeService;
    private final UsersService usersService;

    public UserController(UsersTypeService usersTypeService, UsersService usersService) {
        this.usersTypeService = usersTypeService;
        this.usersService = usersService;
    }

    @GetMapping("/register")
    public String register(Model model){
        List<UsersType> usersTypes=usersTypeService.getAll();
        model.addAttribute("getAllTypes", usersTypes);
        model.addAttribute("user", new Users());
        return "register";

    }

    @PostMapping("/register/new")
    public String useeRegisteration (@Valid Users users){
       usersService.createUser(users);
       return "dashboard";
    }
}
