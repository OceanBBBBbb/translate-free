package com.bhy.translatefree.translate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 谷歌中国的翻译实现,已知海外访问不稳定,大陆访问稳定
 *
 * @author oceanBin on 2020/06/23
 */
@Slf4j
public class TranslateGoogleCn extends TranslateClientAbstract {

    // 单例-函数式
    private static volatile Supplier<TranslateClientAbstract> instance;
    private TranslateGoogleCn(){}
    public static Supplier<TranslateClientAbstract> getInstance(){
        if(null == instance){
            synchronized (TranslateGoogleCn.class){
                if(null == instance){
                    instance = TranslateGoogleCn::new;
                }
            }
        }
        return instance;
    }

    // us_en或zh_cn
    static final String GOOGLE_EN = "us_en";
    static final String GOOGLE_CN_URL =
            "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=%s&q=%s";

    @Override
    public String queueCn2en(String cn, boolean priority) throws Exception {
        return null;
    }

    @Override
    public String cn2en(String text) throws Exception {
        String url = String.format(GOOGLE_CN_URL, GOOGLE_EN, text);
        Response response = getReq(url);
        String result = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray sentences = JSONArray.parseArray(jsonObject.getString("sentences"));
        JSONObject jsonObject1 = (JSONObject) sentences.get(0);
        log.info("GOOGLE_CN cn2en done");
        return jsonObject1.getString("trans");
    }

    @Override
    public String en2cn(String text) throws Exception {
        return null;
    }

    public static void main(String[] args) throws Exception {
        String n = TranslateGoogleCn.getInstance().get().cn2en("你妈妈");
        System.out.println(n);
    }

//
//    enum QueueModel {
//
//        INSTANCE;
//
//        private static final long limit_time = 200L; // 请求间隔时间
//        private static AtomicLong last_req_time = new AtomicLong(0L);
//        private static LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>(1<<4);
//        private static ReentrantLock lock = new ReentrantLock();
//        static TranslateGoogleCn googleCn = (TranslateGoogleCn) TranslateGoogleCn.getInstance().get();
//
//        public String translate(String cn, boolean priority) throws Exception {
//            if (queue.size() > 5 && !priority) {// 不能插队的过来膨胀了,太多了会等太久。
//                log.error("对不起我们满员了");
//                return "没有进行翻译就返回了" + cn;
//            }
//            // 先全都加入队列
//            try {
//                if (priority) { // 要插队的
//                    queue.putFirst(cn);
//                } else {
//                    queue.put(cn);
//                }
//            } catch (InterruptedException e) {
//                log.error("如队列失败了，那就返回繁忙处理不来了,任务结束");
//                return "";
//            }
//            // 然后上锁，从队列里拿一个出来，尝试消费
//            lock.lock();
//            long last_req = last_req_time.get();
//            String en = cn;
//            if (0L == last_req) {// 说明我是第一个
//                log.info("我是系统里第一个用mockTrans2的");
//                try {
//                    String take = queue.take();
//                    en = translate(take);
//                } catch (InterruptedException e) {
//                    log.error("这个获取异常不应该吧", e);
//                }
//            } else {// 说明已经有人之前请求过了，记录了上次请求的时间
//                long howLong = System.currentTimeMillis() - last_req;// 看看已经过去多久了
//                log.info("howLong={},last_req={}", howLong, last_req);
//                if (howLong < limit_time) {// 如果过去的时候小于请求间隔要求，那就休憩一下
//                    try {
//                        Thread.sleep(limit_time - howLong);
//                    } catch (InterruptedException ignored) {
//                    }
//                    log.info("我休息完了限制时间");
//                } else {
//                    log.info("不必休息了{}", howLong);
//                }
//                log.info("开始请求了");
//                // 然后再进行下一个请求
//                try {
//                    String take = queue.take();
//                    en = translate(take);
//                } catch (InterruptedException e) {
//                    log.error("这个获取异常不应该吧", e);
//                }
//            }
//            lock.unlock();
//            return en;
//        }
//
//        private String translate(String cn) {
//            String s = "";
//            try {
//                s = googleCn.cn2en(cn);
//            } catch (Exception e) {
//                log.error("googleCn失败");
//                throw new RuntimeException("googleCn失败");
//            }finally {
//                last_req_time.set(System.currentTimeMillis());
//            }
//            return s;
//        }
//    }

}
