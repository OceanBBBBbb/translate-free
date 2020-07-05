package com.bhy.translatefree.translate;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** @author oceanBin on 2020/06/23 */
@Slf4j
public abstract class TranslateClientAbstract {
//
//  @Getter
//  @Setter
//  private long limitTimeMills = 1000L;
//
//  @Getter
//  @Setter
//  private LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>(1<<3);
//
//  @Getter
//  @Setter
//  private ReentrantLock lock;
//
//  @Getter
//  @Setter
//  private int maxQueueSize = 1<<2;

  // 特定的OK_HTTP_CLIENT，统一用较短的过期时间,使整个请求结果稳定且又不至于等待过长(>1s)时间
  private static final OkHttpClient OK_HTTP_CLIENT;
  public static int TRANSLATE_API_TIME_OUT_MS = 300;//
//  private static AtomicLong LAST_REQ_TIME = new AtomicLong(0L);


  static {
//    try {
//      SslUtil.ignoreSsl();
//    } catch (Exception e) {
//      log.error("ignoreSsl error:", e);
//    }
    OK_HTTP_CLIENT =
        new OkHttpClient.Builder()
            .connectTimeout(TRANSLATE_API_TIME_OUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(TRANSLATE_API_TIME_OUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(TRANSLATE_API_TIME_OUT_MS, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build();
  }
  private final static ExecutorService channel_1_thread = Executors.newSingleThreadExecutor();

  public final Response getReq(String url) throws Exception {
    final Request request = new Request.Builder().get().url(url).build();
    Call call = OK_HTTP_CLIENT.newCall(request);
    return call.execute();
  }


  /**
   * 汉译英
   * @param cn 汉语
   * @param priority 是否优先
   * @return en
   */
  public abstract String queueCn2en(String cn, boolean priority) throws Exception;

  // 这里好像大部分可以自动识别语言了。。可能没必要分汉2英
  public abstract String cn2en(String text) throws Exception;

  public abstract String en2cn(String text) throws Exception;
//
//  // 为啥移到父类就会同步失效了呢
//  enum QueueModel {
//
//    INSTANCE;
//
//    private static final long limit_time = 1000L; // 请求间隔时间
//    private static AtomicLong last_req_time = new AtomicLong(0L);
//    private static LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>(1<<4);
//    private static ReentrantLock lock = new ReentrantLock();
//
//    public String translate(TranslateClientAbstract client, String cn, boolean priority) throws Exception {
//      if (queue.size() > 2 && !priority) {// 不能插队的过来膨胀了,太多了会等太久。
//        log.error("对不起我们满员了");
//        return "没有进行翻译就返回了" + cn;
//      }
//      // 先全都加入队列
//      try {
//        if (priority) { // 要插队的
//          queue.putFirst(cn);
//        } else {
//          queue.put(cn);
//        }
//      } catch (InterruptedException e) {
//        log.error("如队列失败了，那就返回繁忙处理不来了,任务结束");
//        return "";
//      }
//      // 然后上锁，从队列里拿一个出来，尝试消费
//      lock.lock();
//      long last_req = last_req_time.get();
//      String en = cn;
//      if (0L == last_req) {// 说明我是第一个
//        log.info("我是系统里第一个用mockTrans2的");
//        try {
//          String take = queue.take();
//          en = client.cn2en(take);
//          last_req_time.set(System.currentTimeMillis());
//        } catch (InterruptedException e) {
//          log.error("这个获取异常不应该吧", e);
//        }
//      } else {// 说明已经有人之前请求过了，记录了上次请求的时间
//        long howLong = System.currentTimeMillis() - last_req;// 看看已经过去多久了
//        log.info("howLong={},last_req={}", howLong, last_req);
//        if (howLong < limit_time) {// 如果过去的时候小于请求间隔要求，那就休憩一下
//          try {
//            Thread.sleep(limit_time - howLong);
//          } catch (InterruptedException ignored) {
//          }
//          log.info("我休息完了限制时间");
//        } else {
//          log.info("不必休息了{}", howLong);
//        }
//        log.info("开始请求了");
//        // 然后再进行下一个请求
//        try {
//          String take = queue.take();
//          en = client.cn2en(take);
//          last_req_time.set(System.currentTimeMillis());
//        } catch (InterruptedException e) {
//          log.error("这个获取异常不应该吧", e);
//        }
//      }
//      lock.unlock();
//      return en;
//    }
//
////    private String translate(String cn) {
////      String s = "";
////      try {
////        s = baidu.cn2en(cn);
////        last_req_time.set(System.currentTimeMillis());
////      } catch (Exception e) {
////        log.error("百度失败", e);
////        // TODO 抛出异常
////      }
////      return s;
////    }
//  }
}
