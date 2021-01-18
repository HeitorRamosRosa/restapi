package pt.isec.tppd.restapi;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isec.tppd.restapi.communicationLogic.Server;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class AuthorizationFilter extends OncePerRequestFilter
{

    Server server;

    AuthorizationFilter(){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            server = new Server(9008);
        } catch (SQLException throwables) {
            System.out.println("Error creating server.");
        }
        server.start();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException
    {
        String token = httpServletRequest.getHeader("Authorization");
        String name = httpServletRequest.getHeader("Name");
        //no trabalho pratico tem que ser feita uma verificação a serio

        if(server.getUserToken(name).equals(token))
        {
            UsernamePasswordAuthenticationToken uPAT =
                    new UsernamePasswordAuthenticationToken("USER",null, null);
            SecurityContextHolder.getContext().setAuthentication(uPAT);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
