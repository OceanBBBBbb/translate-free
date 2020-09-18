package com.bhy.translatefree;

import com.bhy.translatefree.translate.GoogleTranslateService;
import com.bhy.translatefree.translate.TranslateApi;
import com.bhy.translatefree.translate.domain.TranslateRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class TranslatefreeApplicationTests {

	@Test
	void contextLoads() {
		String en = TranslateApi.API_INSTANCE.tryCn2en("中华文化源远流长");
		log.info("翻译结果："+en);
	}


	@Autowired
	private GoogleTranslateService googleTranslateService;

	@Test
	void contextLoads2() {
		TranslateRequest translateRequest = new TranslateRequest();
		List<String> lstr = new ArrayList<>();
		lstr.add("中华文化源远流长");
		translateRequest.setRawTexts(lstr);
		translateRequest.setSource("ZH_CN");
		translateRequest.setTarget("US_EN");
		List<String> translate = googleTranslateService.translate(translateRequest);
		log.info("翻译结果："+ Arrays.toString(translate.toArray()));
	}

}
