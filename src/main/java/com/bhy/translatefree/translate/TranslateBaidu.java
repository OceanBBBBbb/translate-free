package com.bhy.translatefree.translate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bhy.translatefree.translate.baidu.TransApi;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 百度API翻译，稳定性好，缺点只有1QPS，用量限制字数，否则收费
 *
 * @author oceanBin on 2020/06/23
 */
@Slf4j
public class TranslateBaidu extends TranslateClientAbstract {

  // 原则上要禁止调用者绕过TranslateApi直接创建渠道客户端，需要通过工厂指定来使用指定的翻译渠道。而不是直接创建，
  // 所以这里控制单例不必做到防反射
  // 单例-函数式
  private static volatile Supplier<TranslateClientAbstract> instance;
  private TranslateBaidu(){}
  public static Supplier<TranslateClientAbstract> getInstance(){
    if(null == instance){
      synchronized (TranslateBaidu.class){
        if(null == instance){
          instance = TranslateBaidu::new;
//          instance.get().setLimitTimeMills(1000L);
//          instance.get().setQueue(new LinkedBlockingDeque<>(8));
//          instance.get().setLock(new ReentrantLock());
//          instance.get().setMaxQueueSize(4);
        }
      }
    }
    return instance;
  }

  // 百度API配置信息
  private static final String TO_LANGUAGE_EN = "en";
  // TODO 这里换上你在百度翻译平台的账号，这个是免费一月200W翻译量够可以了。
  private static final String APP_ID = "your app id";
  private static final String SECURITY_KEY = "you s key";
  private static final TransApi api = new TransApi(APP_ID, SECURITY_KEY);

  // 限量每日50000个字符,看一看最近32天的请求数据
  private static final int MAX_LENGTH = 50000;
  private static final int MAX_RECORD = 1 << 5;
  private static AtomicInteger usedLength = new AtomicInteger(0);
  private static ConcurrentHashMap<LocalDate, AtomicInteger> reqData = new ConcurrentHashMap<>(MAX_RECORD);

  // 限频，使用可插队的双向链表Deque,大于容量则阻塞
//  private static final int MAX_NORMAL_QUEUE = 4;// 非优先任务当总队长度到达此值直接放弃
//  private static final long LIMIT_TIME_MILLS = 1000L; // 请求间隔时间
//  private static AtomicLong LAST_REQ_TIME = new AtomicLong(0L);
//  private static LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>(8);
//  private static ReentrantLock lock = new ReentrantLock();

//  /**
//   * 汉译英
//   * @param cn 汉语
//   * @param priority 是否优先
//   * @return en
//   */
//  @Override
//  public String queueCn2en(String cn, boolean priority) throws Exception {
//    // 子类让父类执行？
//    // 入队
//    inQueue(queue,MAX_NORMAL_QUEUE,priority,cn);
//    long last = LAST_REQ_TIME.get();
////    return syncTranslate(lock,last,queue);
//    lock.lock();
//    String en;
//    if(0L==last){// 说明我是第一个
//      log.info("我是系统里第一个用mockTrans2的");
//      String take = queue.take();
//      en = cn2en(take);
//    }else{// 说明已经有人之前请求过了，记录了上次请求的时间
//      long howLong = System.currentTimeMillis() - last;// 看看已经过去多久了
//      log.info("howLong={},last_req={}",howLong,last);
//      if(howLong < LIMIT_TIME_MILLS){// 如果过去的时候小于请求间隔要求，那就休憩一下
//        try {
//          Thread.sleep(LIMIT_TIME_MILLS -howLong);
//        } catch (InterruptedException ignored) {
//        }
//        log.info("我休息完了限制时间");
//      }else{
//        log.info("不必休息了{}",howLong);
//      }
//      log.info("开始请求了");
//      // 然后再进行下一个请求
//      String take = queue.take();
//      en = cn2en(take);
//    }
//    lock.unlock();
//    return en;
//  }
//
//  protected void inQueue(LinkedBlockingDeque<String> queue, int maxLimit, boolean priority, String text){
//    if(queue.size()>maxLimit && !priority){
//      log.error("对不起我们满员了");
//      throw new RuntimeException("没有进行翻译就返回了"+text);
//    }
//    try {
//      if(priority){ // 要插队的
//        queue.putFirst(text);
//      }else{
//        queue.put(text);
//      }
//    } catch (InterruptedException e) {
//      log.error("如队列失败了，那就返回繁忙处理不来了,任务结束");
//      throw new RuntimeException("");
//    }
//  }
//
//  protected String syncTranslate(ReentrantLock reentrantLock, long last, LinkedBlockingDeque<String> queue) throws Exception{
//    // 然后上锁，从队列里拿一个出来，尝试消费
//    lock.lock();
//    String en;
//    if(0L==last){// 说明我是第一个
//      log.info("我是系统里第一个用mockTrans2的");
//      String take = queue.take();
//      en = cn2en(take);
//    }else{// 说明已经有人之前请求过了，记录了上次请求的时间
//      long howLong = System.currentTimeMillis() - last;// 看看已经过去多久了
//      log.info("howLong={},last_req={}",howLong,last);
//      if(howLong < LIMIT_TIME_MILLS){// 如果过去的时候小于请求间隔要求，那就休憩一下
//        try {
//          Thread.sleep(LIMIT_TIME_MILLS -howLong);
//        } catch (InterruptedException ignored) {
//        }
//        log.info("我休息完了限制时间");
//      }else{
//        log.info("不必休息了{}",howLong);
//      }
//      log.info("开始请求了");
//      // 然后再进行下一个请求
//      String take = queue.take();
//      en = cn2en(take);
//    }
//    lock.unlock();
//    return en;
//  }


//  @Override
//  public String queueCn2en(String cn, boolean priority) throws Exception {
//    return QueueModel.INSTANCE.translate(cn,priority);
//  }

  @Override
  public String queueCn2en(String cn, boolean priority) throws Exception {
    return QueueModel.INSTANCE.translate(this,cn,priority);
  }

  @Override
  public String cn2en(String text) throws Exception {
    checkLength(text.length());
    String result = api.getTransResult(text, "auto", TO_LANGUAGE_EN);
    JSONObject jsonObject = JSONObject.parseObject(result);
    JSONArray sentences = JSONArray.parseArray(jsonObject.getString("trans_result"));
    JSONObject jsonObject1 = (JSONObject) sentences.get(0);
    log.info("Baidu cn2en done");
    return jsonObject1.getString("dst");
  }

  private void checkLength(int length) {

    LocalDate todayDate = LocalDate.now();
    if (!reqData.containsKey(todayDate)) {
      // the new day
      if (reqData.size() + 1 > MAX_RECORD) {
        log.info(
            "Baidu translate reqData size is too large,then will be clear,before data = [{}]",
            reqData);
        reqData.clear();
      }
      usedLength.set(0);
      reqData.put(todayDate, usedLength);
    }
    int nowLength = usedLength.get() + length;
    if (nowLength > MAX_LENGTH) {
      throw new IllegalArgumentException("Baidu translate exceeded the upper limit");
    }
    usedLength.set(nowLength);
    reqData.put(todayDate, usedLength);
    log.info("Baidu translate word size = {}", usedLength);
  }

  @Override
  public String en2cn(String text) throws Exception {
    return null;
  }

    enum QueueModel {

    INSTANCE;

    private static final long limit_time = 1000L; // 请求间隔时间
    private static AtomicLong last_req_time = new AtomicLong(0L);
    private static LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>(1<<4);
    private static ReentrantLock lock = new ReentrantLock();
    static TranslateBaidu baidu = (TranslateBaidu) TranslateBaidu.getInstance().get();

    public String translate(TranslateClientAbstract client, String cn, boolean priority) throws Exception {
      if (queue.size() > 2 && !priority) {// 不能插队的过来膨胀了,太多了会等太久。
        log.error("对不起我们满员了");
        return "没有进行翻译就返回了" + cn;
      }
      // 先全都加入队列
      try {
        if (priority) { // 要插队的
          queue.putFirst(cn);
        } else {
          queue.put(cn);
        }
      } catch (InterruptedException e) {
        log.error("如队列失败了，那就返回繁忙处理不来了,任务结束");
        return "";
      }
      // 然后上锁，从队列里拿一个出来，尝试消费
      lock.lock();
      long last_req = last_req_time.get();
      String en = cn;
      if (0L == last_req) {// 说明我是第一个
        log.info("我是系统里第一个用mockTrans2的");
        try {
          String take = queue.take();
          en = translate(take);
          last_req_time.set(System.currentTimeMillis());
        } catch (InterruptedException e) {
          log.error("这个获取异常不应该吧", e);
        }
      } else {// 说明已经有人之前请求过了，记录了上次请求的时间
        long howLong = System.currentTimeMillis() - last_req;// 看看已经过去多久了
        log.info("howLong={},last_req={}", howLong, last_req);
        if (howLong < limit_time) {// 如果过去的时候小于请求间隔要求，那就休憩一下
          try {
            Thread.sleep(limit_time - howLong);
          } catch (InterruptedException ignored) {
          }
          log.info("我休息完了限制时间");
        } else {
          log.info("不必休息了{}", howLong);
        }
        log.info("开始请求了");
        // 然后再进行下一个请求
        try {
          String take = queue.take();
          en = translate(take);
          last_req_time.set(System.currentTimeMillis());
        } catch (InterruptedException e) {
          log.error("这个获取异常不应该吧", e);
        }
      }
      lock.unlock();
      return en;
    }
      private String translate(String cn) {
        String s = "";
        try {
          s = baidu.cn2en(cn);
        } catch (Exception e) {
          log.error("baidu失败");
          throw new RuntimeException("baidu失败");
        }finally {
          last_req_time.set(System.currentTimeMillis());
        }
        return s;
      }
  }
}
