package org.suda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.suda.entity.User;
import org.suda.mapper.UserMapper;
import org.suda.service.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public void saveMoney(Long id, int amount) {
        String threadName = Thread.currentThread().getName();
        User user = this.getById(id);
        Integer old_amount = user.getAmount();
//        System.out.print("thread: " + threadName);
        System.out.print(threadName + ", old_amount: " + old_amount);
        Integer new_amount = old_amount + amount;
        System.out.println(threadName + ", new_amount: " + new_amount);
        user.setAmount(new_amount);
        this.updateById(user);
    }

    @Override
    public void saveMoneyWithCustomOptimisticLock(Long id, int amount) {
        boolean success= false;
        int maxRetry = 5;
        while (!success && maxRetry-- > 0) {
            User user = this.getById(id);
            Integer old_amount = user.getAmount();
            Integer old_version = user.getVersion();

            Integer new_amount = old_amount + amount;
            Integer new_version = old_version + 1;  // 版本号加1

            // 自定义 update 条件: id + version
            QueryWrapper<User> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("id", id).eq("version", old_version);

            // 使用新对象的原因：如果有其他业务查询id为1的用户，并且将其数据修改。那么这里
//            user.setAmount(new_amount);
//            user.setVersion(new_version);
            // 会连带着更新其他属性，比如age，name等
            // 所以采取构建新对象，只更新该更新的字段更安全
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setAmount(new_amount);
            updateUser.setVersion(new_version);

            success = this.update(updateUser, updateWrapper);

            if (!success) {
                System.out.println(Thread.currentThread().getName() + "乐观锁冲突，重试中...");
                try {
                    Thread.sleep(100);  // 简单回退策略
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if(!success) {
            throw new RuntimeException("乐观锁重试失败，操作终止！");
        }
    }

    /**
     * mbp: MyBatisPlus
     * @param id
     * @param amount
     */
    @Override
    public void saveMoneyWithMbpOptimisticLock(Long id, int amount) {
        User user = this.getById(id);
        Integer old_amount = user.getAmount();
        Integer new_amount = old_amount + amount;
        System.out.println("In " + Thread.currentThread().getName() + ", new_amount: " + new_amount);
        System.out.println("In " + Thread.currentThread().getName() + ", version: " + user.getVersion());
//        User updateUser = new User();
//        updateUser.setId(id);
//        updateUser.setAmount(new_amount);
        // 由于使用新对象，没有设置version字段，所以导致没有触发乐观锁机制
        // ERROR：Parameter 'MP_OPTLOCK_VERSION_ORIGINAL' not found. Available parameters are [param1, et]
        // 因为mbp，自己会处理版本号，所以不存在其他业务修改了数据。但是我这里又数据回滚了。
        // 不会，因为这里回滚不了，因为版本号不对。所以放心大胆地使用查询出来的对象
//        updateUser.setVersion(user.getVersion());
        user.setAmount(new_amount);
        this.updateById(user);
    }
}
