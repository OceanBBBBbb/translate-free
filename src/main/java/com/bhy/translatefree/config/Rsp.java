package com.bhy.translatefree.config;

import lombok.Builder;
import lombok.Data;

/**
 * @describe Rsp
 * @author oceanBin
 * @date 2020/07/05
 */
@Builder
@Data
public class Rsp {

    public static final String FAIL = "10001";
    public static final String OK = "0";

    private String code;
    private String result;
}
