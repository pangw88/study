package com.wp.study.mq.amqp.rabbitmq.spring.listener;

import org.springframework.stereotype.Service;

@Service("foo")
public class Foo {

    public void listen(String foo) {
        System.out.println(foo);
    }
}
