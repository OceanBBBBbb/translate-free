package com.bhy.translatefree.mock2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @describe FactoryApp
 * @author oceanBin
 * @date 2020/07/04
 */
@Slf4j
public enum FactoryApp {

    INSTANCE;

    private static final List<ClientInfo> TYPE_SUPPLIER_MAP = new ArrayList<>();

    static {
        TYPE_SUPPLIER_MAP.add(new ClientInfo(new Apply1(), System.currentTimeMillis(), 1300L,0,0));// 需要400L
        TYPE_SUPPLIER_MAP.add(new ClientInfo(new Apply2(), System.currentTimeMillis(), 1000L,0,0));// 谷歌cn也得1秒了
        TYPE_SUPPLIER_MAP.add(new ClientInfo(new Apply3(), System.currentTimeMillis(), 500L,0,0));// 1200L,就是基本要等,请求间隔不要设置为大于请求耗时
    }

    private final static int CLIENT_NUM = TYPE_SUPPLIER_MAP.size();
    private final static int REMOVE_FACTOR = 10;// 失败次数过多的抛弃阈值，另一条件为同时比上位高出2倍
    private static AtomicInteger REQ_TIMES = new AtomicInteger();

    public FAbstract getClient() {
        // 这里其实就是第一个同步队列，插队应该发生在这里
        // 应该在这里排队，而不是在实现队列里排队。
        // 不不，插队应该在这之前，这里只能确保给你一个最优的客户端

        // 先轮询，获取顺位的客户端的占用情况
        int c_flag_last = REQ_TIMES.get() % CLIENT_NUM;
        int c_flag = REQ_TIMES.incrementAndGet() % CLIENT_NUM;
        // 但是要看看这个客户端的上次请求时间，如果是不需要等待的时间里，那就返回，如果不是，那就看下一个，直到可以找到一个在请求范围内的，
        // 这里需要同步操作(一个一个给，先来先给)，如何给后来的插队给呢？

        synchronized (TYPE_SUPPLIER_MAP) {
            ClientInfo clientInfo = TYPE_SUPPLIER_MAP.get(c_flag);
            if(clientInfo.getFailTimes() > REMOVE_FACTOR &&
                    clientInfo.getFailTimes() > TYPE_SUPPLIER_MAP.get(c_flag_last).getFailTimes() << 1){
                log.info("clientInfo={}失败次数过多，跳过",clientInfo);
                clientInfo = TYPE_SUPPLIER_MAP.get(REQ_TIMES.incrementAndGet() % CLIENT_NUM);
            }
            long timeDiff = System.currentTimeMillis() - clientInfo.getLastAccessTime();
            if (clientInfo.getLimitMills() - timeDiff > 0) {
//                log.info("这才需要休憩,来顺延下一个客户端");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
                return getClient();
            }
            clientInfo.setLastAccessTime(System.currentTimeMillis());
            clientInfo.setUsedTimes(clientInfo.getUsedTimes() + 1);
            log.info("will apply clientInfo: " + clientInfo);
            return clientInfo.client;
        }
    }

    // 这里先用数字代替一下...这里也要锁ClientInfo,需要做一下锁粒度细化
    public void addFailedTimes(FAbstract s) {

        synchronized (TYPE_SUPPLIER_MAP) {
            if (s instanceof Apply1) {
                TYPE_SUPPLIER_MAP.get(0).setFailTimes(TYPE_SUPPLIER_MAP.get(0).failTimes + 1);
            }else if(s instanceof Apply2){
                TYPE_SUPPLIER_MAP.get(1).setFailTimes(TYPE_SUPPLIER_MAP.get(1).failTimes + 1);
            }else{
                TYPE_SUPPLIER_MAP.get(2).setFailTimes(TYPE_SUPPLIER_MAP.get(2).failTimes + 1);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ClientInfo {

        private FAbstract client;
        private long lastAccessTime;
        private long limitMills;

        private long usedTimes;
        private int failTimes;
    }

}
