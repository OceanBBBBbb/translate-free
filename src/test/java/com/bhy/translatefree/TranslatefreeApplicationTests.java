package com.bhy.translatefree;

import com.bhy.translatefree.translate.TranslateApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class TranslatefreeApplicationTests {

	@Test
	void contextLoads() {
		String en = TranslateApi.API_INSTANCE.tryCn2en("中华文化源远流长");
		log.info("翻译结果："+en);
	}

}
