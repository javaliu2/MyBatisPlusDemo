package org.suda;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.mapper.UserMapper;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class WrapperTest {

    @Resource
    private UserMapper userMapper;
    @Test
    void testQueryWrapper() {
        // 查询年龄大于20，金额大于80，按照id降序
        QueryWrapper<User> w = new QueryWrapper<User>();
        w.gt("age", 20);
//        w.gt("amount", 80);
        w.orderByDesc("id");
        List<User> users = userMapper.selectList(w);
        users.forEach(System.out::println);
    }

    @Test
    void testLambdaQueryWrapper() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(User::getAge, 20);
        wrapper.orderByDesc(User::getId);
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);
    }
}
