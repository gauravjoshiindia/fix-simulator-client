package com.test.service;

import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import quickfix.SessionID;

import java.util.HashMap;
import java.util.Map;

@Service
@Data
@Getter
@Setter
public class FixConnectionService {

    Map<SessionID, ConnectorManager> connections;

    public FixConnectionService(){
        if(connections==null){
            connections = new HashMap<SessionID, ConnectorManager>();
        }
    }
}
