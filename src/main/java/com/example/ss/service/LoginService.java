package com.example.ss.service;

import com.example.ss.domain.ResponseResult;
import com.example.ss.domain.User;

public interface LoginService{
     ResponseResult login(User user);

     ResponseResult logout();
}