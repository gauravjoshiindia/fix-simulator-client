package com.test.service;

import com.test.controller.Message;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import quickfix.SessionID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@Service
public class MessageStoreService {

    private Map<SessionID, List<Message>> messageMap;

    public MessageStoreService(){
        if(messageMap == null)
            messageMap = new HashMap<SessionID, List<Message>>();
    }
}
