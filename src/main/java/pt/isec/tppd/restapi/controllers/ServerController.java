package pt.isec.tppd.restapi.controllers ;


import org.springframework.web.bind.annotation.*;
import pt.isec.tppd.restapi.communicationLogic.Server;
import pt.isec.tppd.restapi.models.User;

import java.rmi.RemoteException;
import java.sql.SQLException;

@RestController
public class ServerController
{
    Server server;
    ServerController(){
        try {
            server = new Server(9008);
        } catch (SQLException throwables) {
            System.out.println("Error creating server");
        }
        server.start();
    }
    @GetMapping("/serverhi")
    public String serverHi()
    {
        return server.getApiTest();
    }

    @GetMapping("/lastMessages")
    public String getLastMessages(){
        return server.getLastMessages();
    }

    @PostMapping("/login") //localhost:8080/user/login
    public User login(@RequestBody User user)
    {
        user.setToken(user.getUsername() + "_123");
        //fazer um token correto faz parte do desafio do trabalho pratico
        user.setPassword("***");
        return user;

    }

}
