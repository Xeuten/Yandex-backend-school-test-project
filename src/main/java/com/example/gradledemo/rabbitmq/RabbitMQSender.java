package com.example.gradledemo.rabbitmq;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class RabbitMQSender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private Queue queue;

    // This method sends data to RabbitMQ queue only if the request was sent by telegram bot. In the
    // metainfoParts array the id of telegram chat and the type of request are stored
    public void sendMessage(ResponseEntity<HashMap<String, Object>> response, String metainfo) {
        String[] metainfoParts = metainfo.split("/", 3);
        if(metainfoParts.length == 3 && metainfoParts[0].equals("tgBotBackend")) {
            rabbitTemplate.convertAndSend(queue.getName(), metainfoParts[1] + "\n" + metainfoParts[2]
                    + "\n" + response.toString());
        }
    }

}