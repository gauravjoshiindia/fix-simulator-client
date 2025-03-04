package com.test.fix;

import lombok.Data;

@Data
public class SessionProperties {

    private String host;
    private int port;
    private String senderCompId;
    private String targetCompId;
    private String fixVersion;

}
