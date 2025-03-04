package com.test.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class Message {

    private long id;
    private String fixVersion;
    private String messageType;
    private String messageText;
    private String senderCompId;
    private String targetCompId;
    private String sendingTime;

}
