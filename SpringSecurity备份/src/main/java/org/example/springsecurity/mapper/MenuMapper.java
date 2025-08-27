package org.example.springsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springsecurity.domain.Menu;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {
    List<String> selectPermsByUserId(Long userId);
}
