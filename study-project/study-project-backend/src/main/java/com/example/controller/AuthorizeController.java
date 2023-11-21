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
    @PostMapping("/valid-register-email")
    public Restbean<String> validateRegisterEmail(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                                  HttpSession session){
        String s = service.sendValidateEmail(email, session.getId(), false);
        if (s == null)
            return Restbean.success("邮件已发送，请注意查收");
        else
            return Restbean.failure(400, s);
    }

    @PostMapping("/valid-reset-email")
    public Restbean<String> validateResetEmail(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                               HttpSession session){
        String s = service.sendValidateEmail(email, session.getId(), true);
        if (s == null)
            return Restbean.success("邮件已发送，请注意查收");
        else
            return Restbean.failure(400, s);
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
    /*
    1.发验证邮件
    2.验证码是否正确，正确就在session中从在一个标记
    3.用户发起重置密码请求，如果从在标记，就成功重置
     */

    @PostMapping("/start-reset")
    public Restbean<String> startReset( @Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                        @Length(min = 6,max = 6) @RequestParam("code") String code,
                                        HttpSession session){
        String s = service.validateOnly(email,code,session.getId());
        if(s==null){
            session.setAttribute("reset-password",email);
            return Restbean.success();
        }else {
            return Restbean.failure(400,s);
        }

    }
    @PostMapping("/do-reset")
    public Restbean<String> restPassword( @Length(min = 6,max = 16) @RequestParam("password") String password,
                                          HttpSession session){
        String email = (String) session.getAttribute("reset-password");
        if(email == null){
            return Restbean.failure(401,"请先完成邮箱验证");
        }else if(service.resetPassword(password,email)) {
            session.removeAttribute("reset-password");
            return Restbean.success("密码重置成功");
        }else{
            return Restbean.failure(500,"内部错误，请联系管理员");
        }
    }
}
