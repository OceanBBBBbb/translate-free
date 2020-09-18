package com.bhy.translatefree.translate.domain;

import lombok.Data;

import java.util.List;


/**
 * @author oceanBin
 */
@Data
public class TranslateRequest {
  private List<String> rawTexts;
  private String source;
  private String target;
}
