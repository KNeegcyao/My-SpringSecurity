package com.example.ss;

import com.example.ss.domain.User;
import com.example.ss.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@SpringBootTest
class SsApplicationTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testUserMapper() {
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }
    @Test
    public void TestBCryptPasswordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);

        boolean matches = passwordEncoder.matches("123456",
                "$2a$10$PVeWykdFeWYa18pcFGyr0equVVAGe2w7WyQWbNVLHbHh.WHnjI4a2");
        System.out.println(matches);
    }

}
