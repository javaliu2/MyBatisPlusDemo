package org.suda;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.mapper.UserMapper;
import org.suda.service.UserService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    @Test
    void saveBatch() {
        List<User> users = Arrays.asList(
                new User("Alice", 23, "alice@example.com"),
                new User("Bob", 24, "bob@example.com"),
                new User("John", 25, "john@example.com")
        );
        boolean result = userService.saveBatch(users);
        if (result) {
            System.out.println("Users saved successfully.");
        } else {
            System.out.println("Failed to save users.");
        }
    }

    @Test
    void saveOrUpdate() {
        // 假设有一个 User 实体对象，其中 id 是 TableId 注解的属性
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        boolean result = userService.saveOrUpdate(user); // 调用 saveOrUpdate 方法
        if (result) {
            System.out.println("User updated or saved successfully.");
        } else {
            System.out.println("Failed to update or save user.");
        }
    }
    @Test
    void remove() {
        // 假设有一个 QueryWrapper 对象，设置删除条件为 name = 'John Doe'
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "John Doe");
        boolean result = userService.remove(queryWrapper); // 调用 remove 方法
        if (result) {
            System.out.println("Record deleted successfully.");
        } else {
            System.out.println("Failed to delete record.");
        }
    }

    @Test
    void listMaps() {
        // 查询所有用户，并将结果映射为 Map
        List<Map<String, Object>> userMaps = userService.listMaps(); // 调用 listMaps 方法
        for (Map<String, Object> userMap : userMaps) {
            System.out.println("User Map: " + userMap);
        }
    }
}
