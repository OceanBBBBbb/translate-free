package com.bhy.translatefree.translate;

import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这个最好用强单例
 * 需要实现：控制每个渠道的QPS是1，即
 *
 * @author oceanBin on 2020/06/24
 */
@Slf4j
public enum TranslateApi {
  API_INSTANCE;

  static final int DEFAULT_MAX_TRY = 6;
  static final int MAX_QUEUE_WAIT = 2;
  static final TranslateClientFactory CLIENT_FACTORY = TranslateClientFactory.INSTANCE;
  static AtomicInteger received_thread_num = new AtomicInteger(0);
  static TranslateConcurrentCache cache = new TranslateConcurrentCache();


  public String tryCn2en(String text) {
    // 最多阻塞的请求数MAX_QUEUE_WAIT
    String s = text;
    if (received_thread_num.incrementAndGet() > MAX_QUEUE_WAIT
        || cache.getCacheSize() > MAX_QUEUE_WAIT) {
      received_thread_num.decrementAndGet();
      throw new RuntimeException("ErrorCode.TRANSLATE_BUSY");
    }
    try {
      if (cache.waitEmptyCAS()) {
        cache.putToCache(1000L, Thread.currentThread().getName() + text.hashCode(), 1L);
        s = tryCn2en(text, 1, DEFAULT_MAX_TRY);
      }
    } finally {
      received_thread_num.decrementAndGet();
    }
    return s;
  }

  /**
   * 多次重试获取中译英翻译
   *
   * @param text 待翻原文
   * @param initTimes 当前次数
   * @param maxTimes 最大次数（含）
   * @return 译文
   */
  public String tryCn2en(String text, int initTimes, int maxTimes) {
    String s = text;
    TranslateClientAbstract clientAuto = CLIENT_FACTORY.getClientAuto();
    try {
      s = clientAuto.cn2en(text);
    } catch (Exception e) {
      log.error("翻译失败,将重试:"+e.getMessage());
      CLIENT_FACTORY.addFailedTimes(clientAuto);
      if (initTimes < maxTimes) {
        return tryCn2en(text, ++initTimes, maxTimes);
      }
    }
    return s;
  }

  static class TranslateConcurrentCache {

    private volatile Map<String, Long> lCache = new ConcurrentHashMap<>(2);
    private static final int CORE_POOL_SIZE = 2;
    private static final long MAX_CACHE_TIME_MILLS = 1000L;
    ExecutorService pool =
        new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            CORE_POOL_SIZE,//<< 4
            2L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    public void putToCache(long expire_mills, String key, long value) {

      pool.execute(
          () -> {
            lCache.put(key, value);
            log.debug("key={}加入缓存", key);
            try {
              TimeUnit.MILLISECONDS.sleep(expire_mills);
            } catch (InterruptedException ignored) {
            }
            lCache.remove(key);
            log.debug("key={}被移除缓存", key);
          });
    }

    public int getCacheSize() {
      return lCache.size();
    }

    public boolean waitEmptyCAS() {
//      Unsafe.getUnsafe().compareAndSwapInt();
      while (lCache.size() > 0) {
        try {
          TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
      }
      return true;
    }
  }
}
