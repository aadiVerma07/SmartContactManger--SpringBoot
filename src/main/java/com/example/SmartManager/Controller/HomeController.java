package com.example.SmartManager.Controller;

import com.example.SmartManager.Config.MyConfig;
import com.example.SmartManager.Dao.UserRepository;
import com.example.SmartManager.Entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
public class HomeController {
    @Autowired
    MyConfig myConfig;
    @Autowired
    private UserRepository userRepository;
    @RequestMapping("/home")
    public String home(Model m){
        m.addAttribute("title","Home-SmartContactManager");
        return "Home";
    }
    @RequestMapping("/about")
    public String about(Model m){
        m.addAttribute("title","About-SmartContactManager");
        return "About";
    }
    @RequestMapping("/signup")
    public String signup(Model m){
        m.addAttribute("title","SignUp-SmartContactManager");
        m.addAttribute("user",new User());
        return "SignUp";
    }
    @RequestMapping("/login")
    public String login(){
        return "login";
    }
    @PostMapping("/do-register")
    public  String processForm(@Valid @ModelAttribute("user") User user,BindingResult bindingResult,@RequestParam(value = "agree",defaultValue ="false") boolean agree,Model m ){
       try{
           if(!agree){
               System.out.println("You have not agree term and condition");
           }
           if(bindingResult.hasErrors()){
               System.out.println(bindingResult);
               m.addAttribute("user",user);
               return "Signup";
           }
           user.setRole("ROLE_USER");
           user.setEnabled(true);
           user.setImageUrl("default.png");
           user.setPassword(myConfig.passwordEncoder().encode(user.getPassword()));
           System.out.println("Password is : "+user.getPassword());
           User result=  this.userRepository.save(user);
           m.addAttribute("user",new User());
           return "Signup";
       }
       catch (Exception e){
           e.printStackTrace();
           m.addAttribute("user",user);
           return "Signup";
       }

    }
}
