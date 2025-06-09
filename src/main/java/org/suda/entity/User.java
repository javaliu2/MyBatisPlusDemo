package org.suda.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_info")
public class User {
//    @TableId(value="id", type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField(select = false)  // 不对该属性进行select查询
    private Integer age;
    @TableField("email")  // sql语句: SELECT id,name,age,email AS mail FROM user_info, 将字段名重命名为属性名
    private String mail;
    @TableField(exist = false)  // 该属性没有对应的字段
    private String other;
    public User(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.mail = email;
    }
    public User() {

    }
//    @TableLogic  // 已进行全局配置，这里不用使用注解
    private Integer deleted;

    private Integer amount;

    @Version
    private Integer version;
}
