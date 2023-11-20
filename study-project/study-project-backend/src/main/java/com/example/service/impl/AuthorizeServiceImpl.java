package com.example.service.impl;

import com.example.eneity.Account;
import com.example.mapper.UserMapper;
import com.example.service.AuthorizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {//用来权限校验
    @Resource
    StringRedisTemplate template;
    @Value("${spring.mail.username}")
    String from;
    @Resource
     UserMapper mapper;
    @Resource
    MailSender mailSender;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        if (username == null){
            throw new UsernameNotFoundException("用户名不能为空");
        }
        Account account = mapper.findAccountByNameOrEmail(username);
        if (account == null){
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    @Override
    public String sendValidateEmail(String email,String sessionId) {
        /*1.先生成验证码
        2.把邮箱和对应的验证码直接放到redis里面（过期时间3分钟，如果此时重新要求发送邮件，
        那么，只要剩余时间低于2分钟，就可以重新发送一次，重复此流程）
        3.发送验证码到指定邮箱
        4.如果发送失败，把redis里面的刚刚插入的删除
        5.用户在注册时，再从redis里面取出对应键值对，然后查看验证码是否一致
         */
        String key = "email:"+sessionId + ":" + email;
        if(Boolean.TRUE.equals(template.hasKey(key))){
            Long expire = Optional.ofNullable(template.getExpire(key,TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120){
                return "请求频繁，请稍后再试！";
            }
        }
        if (mapper.findAccountByNameOrEmail(email)!=null){
            return "此邮箱已被其他用户注册";
        }
        Random random = new Random();
        int code = random.nextInt(899999)+100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的验证邮件");
        message.setText("验证码是: "+code);
        try {
            mailSender.send(message);
            template.opsForValue().set(key,String.valueOf(code),3,TimeUnit.MINUTES);
            return null;
        }catch (MailException e){
            e.printStackTrace();
            return "邮件发送失败，请检查邮件地址是否有效";
        }


    }

    @Override
    public String validateAndRegister(String username, String password, String email, String code,String sessionId) {
        String key = "email:" + sessionId + ":" + email;
        if(Boolean.TRUE.equals(template.hasKey(key))){
            String s = template.opsForValue().get(key);
            if(s==null){
                return "验证码失效，请重新请求";
            }
            if(s.equals(code)){
                password = encoder.encode(password);
                if(mapper.createAccount(username,password,email)>0){
                    return null;
                }else{
                    return "内部错误，请联系管理员！";
                }
            }else{
                return "验证码错误！";
            }
        }else {
            return "清先请求一份验证码邮件";
        }
    }
}
