package com.bhy.translatefree.mock2;

import com.bhy.translatefree.translate.TranslateYoudao;
import lombok.extern.slf4j.Slf4j;

/**
 * @describe Apply2
 * @author oceanBin
 * @date 2020/07/04
 */
@Slf4j
public class Apply3 extends FAbstract {
    @Override
    public String cn2en(String cn) throws Exception{

        log.info("TranslateYoudao will do");
        return TranslateYoudao.getInstance().get().cn2en(cn);
    }
}
