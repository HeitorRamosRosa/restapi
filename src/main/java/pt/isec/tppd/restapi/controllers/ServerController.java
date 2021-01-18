package pt.isec.tppd.restapi.controllers ;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.tppd.restapi.communicationLogic.Server;

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

}
