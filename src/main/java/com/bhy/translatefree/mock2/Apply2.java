package com.bhy.translatefree.mock2;

import com.bhy.translatefree.translate.TranslateGoogleCn;
import lombok.extern.slf4j.Slf4j;

/**
 * @describe Apply2
 * @author oceanBin
 * @date 2020/07/04
 */
@Slf4j
public class Apply2 extends FAbstract {
    @Override
    public String cn2en(String cn) throws Exception{

        log.info("TranslateGoogleCn will do");
        return TranslateGoogleCn.getInstance().get().cn2en(cn);
    }
}
