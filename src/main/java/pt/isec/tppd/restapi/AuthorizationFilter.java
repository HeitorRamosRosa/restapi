package pt.isec.tppd.restapi;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException
    {
        String token = httpServletRequest.getHeader("Authorization");

        //no trabalho pratico tem que ser feita uma verificação a serio

        if(token != null && token.endsWith("_123"))
        {
            UsernamePasswordAuthenticationToken uPAT =
                    new UsernamePasswordAuthenticationToken("USER",null, null);
            SecurityContextHolder.getContext().setAuthentication(uPAT);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
