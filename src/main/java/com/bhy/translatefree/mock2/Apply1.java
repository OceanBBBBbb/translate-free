package com.bhy.translatefree.mock2;

import com.bhy.translatefree.translate.TranslateBaidu;
import lombok.extern.slf4j.Slf4j;

/**
 * @describe Apply1
 * @author oceanBin
 * @date 2020/07/04
 */
@Slf4j
public class Apply1 extends FAbstract {
    @Override
    public String cn2en(String cn) throws Exception{

        log.info("TranslateBaidu will do it");
        return TranslateBaidu.getInstance().get().cn2en(cn);
    }
}
