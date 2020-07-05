package com.bhy.translatefree.translate;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * 福析翻译,也只支持大陆
 *
 * @author oceanBin on 2020/07/01
 */
@Slf4j
public class TranslateFuxi extends TranslateClientAbstract{

    static final String LAN_EN = "en";
    static final String LAN_CN = "zh";
    static final String URL = "http://fanyi.pdf365.cn/v2/api/getTranslateResult?orginL=%s&targetL=%s&text=%s";


    //{"result":"Hello there"}
    @Override
    public String cn2en(String text) throws Exception {
        String url = String.format(URL,LAN_CN, LAN_EN, text);
        Response response = getReq(url);
        String result = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(result);
        log.info("TranslateFuxi cn2en done");
        return jsonObject.getString("result");
    }

    @Override
    public String en2cn(String text) throws Exception {
        return null;
    }

    @Override
    public String queueCn2en(String cn, boolean priority) throws Exception {
        return null;
    }

    public static void main(String[] args) throws Exception {
        String cn = new TranslateFuxi().cn2en("这里是中国");
        System.out.println(cn);
        String bd = TranslateBaidu.getInstance().get().queueCn2en("百度",false);
        System.out.println(bd);
    }
}
