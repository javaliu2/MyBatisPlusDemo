package org.suda;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.suda.entity.User;
import org.suda.service.UserService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class LockTest {
    @Autowired
    private UserService userService;

    /**
     * 场景：用户a和b对同一个账户进行更新操作，最后账户余额是否正确（并发覆写）
     * output:
     * task_b, old_amount: 100task_b, new_amount: 130
     * task_a, old_amount: 100task_a, new_amount: 120
     * sql:
     * Preparing: UPDATE user_info SET name=?, email=?, amount=? WHERE id=? AND deleted=0
     * Preparing: UPDATE user_info SET name=?, email=?, amount=? WHERE id=? AND deleted=0
     * Parameters: Jone(String), test1@baomidou.com(String), 130(Integer), 1(Long)
     * Parameters: Jone(String), test1@baomidou.com(String), 120(Integer), 1(Long)
     * 观察以上结果可知，先执行tack_b，将amount更新为130，然后task_a对其覆写，更新为120
     * 最后结果120
     */
    @Test
    void testConcurrentOverwriting() throws InterruptedException {
        // 1、修改id为1用户的金额
        Long id = 1L;
        // 2、存钱，存的金额
        int amount_a = 20, amount_b = 30;
        // 3、开启两个线程，并发执行存钱业务
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Runnable task_a = () -> {
            try {
                Thread.currentThread().setName("task_a");
                userService.saveMoney(id, amount_a);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_a failed, reason: " + e.getMessage());
            }
        };
        Runnable task_b = () -> {
            try {
                Thread.currentThread().setName("task_b");
                userService.saveMoney(id, amount_b);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_b failed, reason: " + e.getMessage());
            }
        };

        executor.submit(task_a);
        executor.submit(task_b);

        executor.shutdown();  //: 不接受新的任务提交，但是保证将线程池中已提交任务跑完
//        executor.awaitTermination(5L, TimeUnit.SECONDS);  //: 主线程等待5秒，如果5秒内两个异步线程完成执行，那么主线程继续执行
        // 如果5秒内两个线程没有完成执行，那么他们继续执行。但是主线程也回来继续执行
        // 这样是为了保证，主线程查询获取到的是，两个线程任务完成后的结果。但是有风险，万一任务线程执行时间大于5秒呢
        // 所以说采取CountDownLatch的方式，万无一失
        latch.await();
        User user = userService.getById(id);
        System.out.println("in main thread, final amount of user: " + user.getAmount());
    }

    /**
     * 自定义乐观锁实现
     * 1、实体类有 version 属性，对应表有 version 字段
     * output:
     * Preparing: UPDATE user_info SET amount=?, version=? WHERE deleted=0 AND (id = ? AND version = ?)
     * Preparing: UPDATE user_info SET amount=?, version=? WHERE deleted=0 AND (id = ? AND version = ?)
     * Parameters: 120(Integer), 2(Integer), 1(Long), 1(Integer)
     * Parameters: 130(Integer), 2(Integer), 1(Long), 1(Integer)
     * Updates: 0  # task_a失败
     * Updates: 1
     * task_a乐观锁冲突，重试中...
     * # task_a的Retry
     * Preparing: SELECT id,name,email AS mail,deleted,amount,version FROM user_info WHERE id=? AND deleted=0
     * Parameters: 1(Long)
     * Total: 1
     * Preparing: UPDATE user_info SET amount=?, version=? WHERE deleted=0 AND (id = ? AND version = ?)
     * Parameters: 150(Integer), 3(Integer), 1(Long), 2(Integer)
     * Updates: 1
     * # final amount:
     * in main thread, final amount of user: 150
     */
    @Test
    void testCustomOptimisticLock() throws InterruptedException {
        Long id = 1L;
        int amount_a = 20, amount_b = 30, amount_c = 40;
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Runnable task_a = () -> {
            try {
                Thread.currentThread().setName("task_a");
                userService.saveMoneyWithCustomOptimisticLock(id, amount_a);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_a failed, reason: " + e.getMessage());
            }
        };
        Runnable task_b = () -> {
            try {
                Thread.currentThread().setName("task_b");
                userService.saveMoneyWithCustomOptimisticLock(id, amount_b);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_b failed, reason: " + e.getMessage());
            }
        };

        Runnable task_c = () -> {
            try {
                Thread.currentThread().setName("task_c");
                userService.saveMoneyWithCustomOptimisticLock(id, amount_c);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_c failed, reason: " + e.getMessage());
            }
        };

        executor.submit(task_a);
        executor.submit(task_b);
        executor.submit(task_c);
        executor.shutdown();
        latch.await();

        User user = userService.getById(id);
        System.out.println("in main thread, final amount of user: " + user.getAmount());
    }

    /**
     * 使用mbp提供的@Version
     * 1、在User中version属性上加@Version
     * 2、一直有错误：Parameter 'MP_OPTLOCK_VERSION_ORIGINAL' not found. Available parameters are [param1, et]
     * 3、恍然大悟，没有去配置，C++
     * 1) 方法上没有添加 @Bean 注解，C++
     * output:
     * Preparing: SELECT id,name,email AS mail,deleted,amount,version FROM user_info WHERE id=? AND deleted=0
     * Preparing: SELECT id,name,email AS mail,deleted,amount,version FROM user_info WHERE id=? AND deleted=0
     * Parameters: 1(Long)
     * Parameters: 1(Long)
     * Total: 1
     * Total: 1
     * In task_b, new_amount: 130
     * In task_b, version: 1
     * In task_a, new_amount: 120
     * In task_a, version: 1
     * Preparing: UPDATE user_info SET name=?, email=?, amount=?, version=? WHERE id=? AND version=? AND deleted=0
     * Preparing: UPDATE user_info SET name=?, email=?, amount=?, version=? WHERE id=? AND version=? AND deleted=0
     * Parameters: Jone(String), test1@baomidou.com(String), 120(Integer), 2(Integer), 1(Long), 1(Integer)
     * Parameters: Jone(String), test1@baomidou.com(String), 130(Integer), 2(Integer), 1(Long), 1(Integer)
     * Updates: 0  # task_a失败
     * Updates: 1  # task_b成功
     * Preparing: SELECT id,name,email AS mail,deleted,amount,version FROM user_info WHERE id=? AND deleted=0
     * Parameters: 1(Long)
     * Total: 1
     * In main thread, final amount of user: 120
     */
    @Test
    void testVersionAnnotation() throws InterruptedException {
        Long id = 1L;
        int amount_a = 20, amount_b = 30;
        CountDownLatch latch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Runnable task_a = () -> {
            try {
                Thread.currentThread().setName("task_a");
                userService.saveMoneyWithMbpOptimisticLock(id, amount_a);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_a failed, reason: " + e.getMessage());
            }
        };
        Runnable task_b = () -> {
            try {
                Thread.currentThread().setName("task_b");
                userService.saveMoneyWithMbpOptimisticLock(id, amount_b);
                latch.countDown();
            } catch (Exception e) {
                System.out.println("task_b failed, reason: " + e.getMessage());
            }
        };

        executor.submit(task_a);
        executor.submit(task_b);
        executor.shutdown();
        latch.await();

        User user = userService.getById(id);
        System.out.println("In main thread, final amount of user: " + user.getAmount());
    }
}
