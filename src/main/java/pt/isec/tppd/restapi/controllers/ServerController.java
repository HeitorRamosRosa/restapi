package pt.isec.tppd.restapi.controllers ;


import org.springframework.web.bind.annotation.*;
import pt.isec.tppd.restapi.communicationLogic.Server;
import pt.isec.tppd.restapi.models.User;

import java.io.IOException;
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
        user.setUserExists(server.checkIfClientExists(user.getUsername()));
        user.setRightPassword(server.checkIfPasswordMatches(user.getUsername(),user.getPassword()));
        user.setUserWasOnline(server.checkIfUserIsLoggedIn(user.getUsername()));
        if(user.isUserExists() == true && user.isPasswordRight() == true && user.isUserWasOnline() == false){
            server.logUser(user.getUsername(),user.getPassword(),server.generateLogInToken(user.getUsername(),user.getPassword()));
            user.setLoggedIn(true);
            user.setToken(user.getUsername() + "_123");
        }else{
            user.setToken("invalid");
        }
        return user;
    }
  
    @PostMapping("/messageServer") //localhost:8080/user/login
    public String messageServer (@RequestBody User user) throws IOException {
        server.messageToServerUsers(user.getUsername(),user.getMessage());
        return "Message sent to all clients in your server.";
    }
}
