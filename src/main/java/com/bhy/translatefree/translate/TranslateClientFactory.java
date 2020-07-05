package com.bhy.translatefree.translate;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 根据之前使用的客户端的执行情况，来决定给哪个客户端
 *
 * @author oceanBin on 2020/06/24
 */
@Slf4j
public enum  TranslateClientFactory {

  INSTANCE;
  // 第一个是失败次数，第二个是总次数
  static AtomicInteger [] init_youdao = {new AtomicInteger(1),new AtomicInteger(1)};
  static AtomicInteger [] init_baidu = {new AtomicInteger(1),new AtomicInteger(1)};
  static AtomicInteger [] init_google = {new AtomicInteger(1),new AtomicInteger(1)};
  static final Map<TranslateClientType, Supplier<TranslateClientAbstract>> TYPE_SUPPLIER_MAP =
      new HashMap<>();
  static Map<TranslateClientType, AtomicInteger []> FAIL_TIMES_MAP = new ConcurrentHashMap<>();

  static {
    TYPE_SUPPLIER_MAP.put(TranslateClientType.GOOGLE_CN, TranslateGoogleCn.getInstance());
    TYPE_SUPPLIER_MAP.put(TranslateClientType.YOUDAO, TranslateYoudao.getInstance());
    TYPE_SUPPLIER_MAP.put(TranslateClientType.BAIDU, TranslateBaidu.getInstance());
    FAIL_TIMES_MAP.put(TranslateClientType.YOUDAO, init_youdao);
    FAIL_TIMES_MAP.put(TranslateClientType.BAIDU, init_baidu);
    FAIL_TIMES_MAP.put(TranslateClientType.GOOGLE_CN, init_google);
  }

  // 智能型获取客户端,需要调用者将请求失败的客户端的failTime递增，这里讲返回失败次数最少的客户端
  public synchronized TranslateClientAbstract getClientAuto() {

    log.info("当前client的失败请求统计:");
    FAIL_TIMES_MAP.forEach((k,v)->{
      log.info("k-{},v={}/{}",k,v[0],v[1]);
    });
    AtomicReference<TranslateClientType> littlerOne = new AtomicReference<>();
    AtomicInteger tmp = new AtomicInteger(Integer.MAX_VALUE);
    FAIL_TIMES_MAP.forEach(
            // 计算权重，与失败次数相反，去掉失败次数最多的那个，
        (k, v) -> {
          if (v[0].get() < tmp.get()) {
            littlerOne.set(k);
            tmp.set(v[0].get());
          }
        });
    return getClientByType(littlerOne.get());
  }

  // 指定性获取翻译客户端
  public TranslateClientAbstract getClientByType(TranslateClientType type) {

    Supplier<TranslateClientAbstract> translateClientAbstractSupplier = TYPE_SUPPLIER_MAP.get(type);
    if (translateClientAbstractSupplier != null) {
      return translateClientAbstractSupplier.get();
    } else {
      throw new IllegalArgumentException("No such TranslateClientType :" + type);
    }
  }

  public void addFailedTimes(TranslateClientAbstract clientAuto) {
    if (clientAuto instanceof TranslateGoogleCn) {
      FAIL_TIMES_MAP.get(TranslateClientType.GOOGLE_CN)[0].incrementAndGet();
    } else if (clientAuto instanceof TranslateYoudao) {
      FAIL_TIMES_MAP.get(TranslateClientType.YOUDAO)[0].incrementAndGet();
//      FAIL_TIMES_MAP.put(
//          TranslateClientType.YOUDAO,
//          new AtomicInteger(FAIL_TIMES_MAP.get(TranslateClientType.YOUDAO).incrementAndGet()));
    } else {
      FAIL_TIMES_MAP.get(TranslateClientType.BAIDU)[0].incrementAndGet();
//      FAIL_TIMES_MAP.put(
//          TranslateClientType.BAIDU,
//          new AtomicInteger(FAIL_TIMES_MAP.get(TranslateClientType.BAIDU).incrementAndGet()));
    }
  }

  public enum TranslateClientType {
    GOOGLE_CN,
    YOUDAO,
    BAIDU,
    ;
  }
}
