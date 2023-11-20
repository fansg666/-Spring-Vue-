package com.example.controller;

import com.example.eneity.Restbean;
import com.example.service.AuthorizeService;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Pattern;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    private final  String USERNAME_REGEX = "^[a-zA-Z0-9一-龥]+$";
    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    @Resource
    AuthorizeService service;
    @PostMapping("/valid-email")
    public Restbean<String> validateEmail(@Pattern (regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") @RequestParam("email") String email, HttpSession session){
        String s = service.sendValidateEmail(email,session.getId());
        if(s==null){
            return Restbean.success("邮件已发送，请注意查收");
        }else{
            return Restbean.failure(404,s);
        }
    }

    @PostMapping("/register")
    public Restbean<String> registerUser(@Pattern(regexp = USERNAME_REGEX) @Length(min = 2,max = 8) @RequestParam("username") String username,
                                         @Length(min = 6,max = 16) @RequestParam("password") String password,
                                         @Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                         @Length(min = 6,max = 6) @RequestParam("code") String code,
                                         HttpSession session){
        String s = service.validateAndRegister(username,password,email,code,session.getId());
        if(s==null){
            return Restbean.success("注册成功");
        }else{
            return Restbean.failure(400,s);
        }

    }
}
