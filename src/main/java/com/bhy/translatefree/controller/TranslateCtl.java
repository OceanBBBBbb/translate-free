package com.bhy.translatefree.controller;

import com.bhy.translatefree.config.Rsp;
import com.bhy.translatefree.config.log.Log;
import com.bhy.translatefree.mock2.ApplyServer;
import com.bhy.translatefree.translate.TranslateApiNew;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author oceanBin on 2020/06/30
 */
@RestController
@RequestMapping("/translate")
@Validated
public class TranslateCtl {


//    @Log
//    @GetMapping("/cn2en")
//    public String cn2en(@RequestParam @NonNull String cn) throws Exception {
//
//        return TranslateApiNew.API_INSTANCE.tryCn2en(cn, false);
//    }

    @Log
    @GetMapping("/cn2en2")
    public Rsp cn2en2(@RequestParam @NonNull String cn) {

        // 轮询有个小问题就是会导致同一个原文可能会被翻译为不同的译文
        boolean b = cn.contains("3");
        String s = ApplyServer.API_INSTANCE.tryCn2en(cn, b);
        String code = "".equals(s) ? Rsp.FAIL : Rsp.OK;
        return Rsp.builder().code(code).result(Rsp.FAIL.equals(code)?"服务被撑爆啦":s).build();
    }
}
