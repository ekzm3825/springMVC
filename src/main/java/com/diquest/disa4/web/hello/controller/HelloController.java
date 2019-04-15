package com.diquest.disa4.web.hello.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/hello")
public class HelloController {

    @RequestMapping(value = "/main.do", produces = MediaType.TEXT_HTML_VALUE)
    public void main() {
        log.info("Hello Main");
    }

}
