package org.suda;


import com.baomidou.mybatisplus.core.toolkit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.mapper.UserMapper;
import org.suda.service.UserService;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class AppTest {
    @Resource
    private UserMapper userMapper;

    @Autowired
    private UserService userService;
    @Test
    void testSetup() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assert.isTrue(5 == userList.size(), "");
        userList.forEach(System.out::println);
    }

    @Test
    void testSave() {
        // 假设有一个 User 实体对象
        // 没有指定主键的时候，mbp会使用雪花算法生成全局唯一ID
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
//        output sql:
//        Preparing: INSERT INTO user ( id, name, email ) VALUES ( ?, ?, ? )
//        Parameters: 1930540468359221250(Long), John Doe(String), john.doe@example.com(String)
        boolean result = userService.save(user); // 调用 save 方法
        if (result) {
            System.out.println("User saved successfully.");
        } else {
            System.out.println("Failed to save user.");
        }
    }
}
