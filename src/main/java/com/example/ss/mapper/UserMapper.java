package com.example.ss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ss.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{

}
