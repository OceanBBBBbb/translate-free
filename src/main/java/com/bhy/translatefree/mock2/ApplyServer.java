package com.bhy.translatefree.mock2;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @describe ApplyServer 真正实现的逻辑，可以插队，会帮你重试
 * @author oceanBin
 * @date 2020/07/05
 */
@Slf4j
public enum ApplyServer {
    API_INSTANCE;

    private static int MAX_JOBS = 1 << 2;
    private static final LinkedBlockingDeque<Callable<String>> queue = new LinkedBlockingDeque<>(1 << 3);

    public String tryCn2en(String cn, boolean priority) {

        // 无需优先权的，当队列长度达到上限，则直接打回，queue这一块的操作需要同步
        if (!priority && queue.size() >= MAX_JOBS) {
            log.info("不能再接待了");
            return "";
        }
        // 可以收着，但要排队
        try {
            if (priority) { // 要插队的
                queue.putFirst(new DoIt(cn));
            } else {
                queue.put(new DoIt(cn));
            }
        } catch (InterruptedException ignored) {
        }
        log.info("看看队列多长了"+queue.size());
        synchronized (queue) {
            String call = "这尼玛我自己心里也没底啊";
            // 入队,如果队列里有线程，挂起当前线程，释放锁，等待被唤醒，并加入队列
            log.info("执行去了");
            try {
                Callable<String> poll = queue.take();
                call = poll.call();
            } catch (Exception e) {
                log.error("queue.take() error,pass it",e);
            }
            return call;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class DoIt implements Callable<String> {

        private String text;

        @Override
        public String call() throws Exception {

            return trySomeTimes(0,6);
        }

        private String trySomeTimes(int i,int max) {
            String s = "";
            FAbstract client = FactoryApp.INSTANCE.getClient();
            try {
                s = client.cn2en(text);
            } catch (Exception e) {
                FactoryApp.INSTANCE.addFailedTimes(client);
                if(max > i){
                    log.error("异常了，别急还会重试:"+e.getMessage());
                    return trySomeTimes(++i,max);
                }else {
                    log.error("各个渠道都多次尝试了，已经无能为力了"+e.getMessage());
                }
            }
            return s;
        }
    }
}
