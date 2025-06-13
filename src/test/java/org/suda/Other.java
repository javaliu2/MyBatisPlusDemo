package org.suda;

import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.service.UserService;

@SpringBootTest
public class Other {

    @Autowired
    private UserService userService;

    /**
     * Context: 设置主键TYPE为INPUT，但是主动不设置主键值
     * Code:
     * @TableId(value="id", type = IdType.INPUT)
     * private Long id;
     */
    @Test
    void test_TYPE_INPUT() {
        User user = new User();
        // error: org.springframework.dao.DataIntegrityViolationException:
//        Cause: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException:Column 'id' cannot be null
//        user.setId(100L);
        user.setName("xs");
        userService.save(user);
    }
}
