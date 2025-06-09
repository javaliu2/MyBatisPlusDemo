package org.suda.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.suda.entity.User;

public interface UserService extends IService<User> {
    void saveMoney(Long id, int amount);
    void saveMoneyWithCustomOptimisticLock(Long id, int amount);
    void saveMoneyWithMbpOptimisticLock(Long id, int amount);
}
