package org.suda;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.mapper.UserMapper;
import org.suda.service.UserService;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class IPageTest {
    @Resource
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Test
    void testIPage() {
        IPage<User> page = new Page<>(2, 3);
        userMapper.selectPage(page, null);
        List<User> records = page.getRecords();
        records.forEach(System.out::println);
    }

    @Test
    void testMyPage() {
        int pageNum = 2, pageSize = 5;
        List<User> users = userService.myGetPage(pageNum, pageSize);
        users.forEach(System.out::println);
    }
}
