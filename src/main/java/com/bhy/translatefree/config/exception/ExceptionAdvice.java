package com.bhy.translatefree.config.exception;

import com.bhy.translatefree.config.Rsp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局Exception handler
 *
 * @author oceanBin on 2020/3/14
 */
@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

  /**
   * 将异常在Controller捕获转换成Result对象返回
   * @param ex
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity handleAllException(Exception ex){
    log.error("ExceptionAdvice.handleAllException",ex);
    // 返回统一的error Result
    return ResponseEntity.ok(Rsp.builder().code(Rsp.FAIL).result("系统异常").build());
  }
}
