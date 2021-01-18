package pt.isec.tppd.restapi.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.tppd.restapi.models.User;

@RestController
@RequestMapping("user")
public class UserController
{
    @PostMapping("login") //localhost:8080/user/login
    public User login(@RequestBody User user)
    {
        user.setToken(user.getUsername() + "_123");
        //fazer um token correto faz parte do desafio do trabalho pratico
        user.setPassword("***");
        return user;

    }
}
