package com.bhy.translatefree;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *
 *
 * @author oceanBin on 2020/07/02
 */
public class TestSemaphore {


    @Test
    public void ts() {
        // 线程池.SynchronousQueue是一个无容量的阻塞队列
        ExecutorService exec = Executors.newCachedThreadPool();

        // 只能5个线程同时访问
        final Semaphore semp = new Semaphore(2);

        // 模拟20个客户端访问
        for (int index = 0; index < 20; index++) {
            final int NO = index;
            Runnable run = () -> {
                try {
                    // 获取许可
                    semp.acquire();
                    System.out.println("Accessing: " + NO);
                    Thread.sleep((long) (Math.random() * 10000));
                    // 访问完后，释放
                    semp.release();
                } catch (InterruptedException e) {
                }
            };
            exec.execute(run);
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 退出线程池
        exec.shutdown();
    }

}
