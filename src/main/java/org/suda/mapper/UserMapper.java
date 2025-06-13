package org.suda.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.suda.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user_info` LIMIT #{offset}, #{limit}")
    List<User> mySelectPage(@Param("offset") int offset, @Param("limit") int limit);
}
