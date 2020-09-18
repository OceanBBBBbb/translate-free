package com.bhy.translatefree.translate;

import com.bhy.translatefree.translate.domain.TranslateRequest;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.threeten.bp.Duration;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author oceanBin
 */
@Slf4j
@Service
public class GoogleTranslateService {

  public List<String> translate(TranslateRequest translateRequest) {
    try {

      RetrySettings retrySettings = RetrySettings.newBuilder()
          .setMaxRetryDelay(Duration.ofMillis(1000L))
          .setTotalTimeout(Duration.ofMillis(10000L))
          .setInitialRetryDelay(Duration.ofMillis(25L))
          .setRetryDelayMultiplier(1.0)
          .setInitialRpcTimeout(Duration.ofMillis(10000L))
          .setRpcTimeoutMultiplier(1.0)
          .setMaxRpcTimeout(Duration.ofMillis(10000L))
          .setMaxAttempts(5)
          .build();
      Translate translate = TranslateOptions.newBuilder()
          .setCredentials(GoogleCredentials.getApplicationDefault()).setRetrySettings(retrySettings)
          .build().getService();

      // Translates some text into Russian
      List<Translation> result =
          translate.translate(
              translateRequest.getRawTexts(),
              TranslateOption.sourceLanguage(translateRequest.getSource()),
              TranslateOption.targetLanguage(translateRequest.getTarget()));

      return result.stream().map(Translation::getTranslatedText).collect(Collectors.toList());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new InternalError("翻译接口调用失败");
    }
  }
}
