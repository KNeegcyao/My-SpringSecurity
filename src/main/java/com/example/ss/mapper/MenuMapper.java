package com.example.ss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ss.domain.Menu;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {
    List<String> selectPermsByUserId(Long id);
}

