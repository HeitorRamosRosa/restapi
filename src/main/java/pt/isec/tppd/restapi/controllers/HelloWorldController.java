package pt.isec.tppd.restapi.controllers ;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController
{
    @GetMapping("/hello-world")
    public String helloWorld(@RequestParam(value = "name", required = false) String name)
    {
        if(name == null)
            return "Hello World!";
        else
            return "Hello "+name+"!";
    }

    @GetMapping("/hello-world2/{name}")
    public String helloWorld2(@PathVariable(value = "name") String name)
    {
        if(name == null)
            return "Hello World!";
        else
            return "Hello "+name+"!";
    }

}
