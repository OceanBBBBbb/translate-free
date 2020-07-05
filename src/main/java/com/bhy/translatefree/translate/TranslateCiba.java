package com.bhy.translatefree.translate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * 词霸翻译渠道,只支持大陆
 *
 * @author oceanBin on 2020/07/01
 */
@Slf4j
public class TranslateCiba extends TranslateClientAbstract{

    static final String LAN_EN = "zh";
    static final String LAN_CN = "en-US";
    static final String URL = "http://fy.iciba.com/ajax.php?a=fy&f=%s&t=%s&w=%s";


    //{"status":1,"content":
    //{"from":"zh","to":"en-US",
    //"vendor":"ciba","out":" Love",
    //"ciba_use":"\u6765\u81ea\u673a\u5668\u7ffb\u8bd1\u3002","ciba_out":"","err_no":0}}
    @Override
    public String cn2en(String text) throws Exception {
        String url = String.format(URL,LAN_EN, LAN_CN, text);
        Response response = getReq(url);
        String result = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject content = JSONObject.parseObject(jsonObject.getString("content"));
        log.info("TranslateCiba cn2en done");
        return content.getString("out");
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
        String cn = new TranslateCiba().cn2en("这里是中国");
        System.out.println(cn);
    }
}
