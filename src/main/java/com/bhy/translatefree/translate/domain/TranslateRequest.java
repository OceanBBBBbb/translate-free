package com.cb.translate.domain;

import java.util.List;
import lombok.Data;

/**
 * @author luxingxiao
 */
@Data
public class TranslateRequest {
  private List<String> rawTexts;
  private String source;
  private String target;
}
