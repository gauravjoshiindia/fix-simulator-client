/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.fix;

import com.test.service.MessageStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix41.MessageCracker;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientApplicationAdapter implements Application {

    private static final Logger log = LoggerFactory.getLogger(ClientApplicationAdapter.class);

    private final MessageCracker messageCracker;

    //SimpleDateFormat sdf = new SimpleDateFormat("")

    @Autowired
    private MessageStoreService messageStoreService;

    public ClientApplicationAdapter(MessageCracker messageCracker) {
        this.messageCracker = messageCracker;
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound {
        log.info("fromAdmin: Message={}, SessionId={}", message, sessionId);
        addMessage(message, sessionId);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        log.info("fromApp: Message={}, SessionId={}", message, sessionId);

        try {
            messageCracker.crack(message, sessionId);
            addMessage(message, sessionId);

        } catch (UnsupportedMessageType | FieldNotFound | IncorrectTagValue e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addMessage(Message message, SessionID sessionId) throws FieldNotFound {
        List<com.test.controller.Message> messagesBySession = messageStoreService.getMessageMap().get(sessionId);

        if(messagesBySession==null){
            messagesBySession = new ArrayList<com.test.controller.Message>();
            messageStoreService.getMessageMap().put(sessionId, messagesBySession);
        }

        messagesBySession.add(new com.test.controller.Message(messagesBySession.size() + 1, message.getHeader().getString(BeginString.FIELD),
                message.getHeader().getString(MsgType.FIELD),
                message.toString(), message.getHeader().getString(SenderCompID.FIELD),
                message.getHeader().getString(TargetCompID.FIELD), message.getHeader().getUtcTimeStamp(SendingTime.FIELD).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("onCreate: SessionId={}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("onLogon: SessionId={}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("onLogout: SessionId={}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        log.info("toAdmin: Message={}, SessionId={}", message, sessionId);
        try {
            addMessage(message, sessionId);
        } catch (FieldNotFound exception) {
            log.error(exception.getMessage(), exception);
        }


    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        log.info("toApp: Message={}, SessionId={}", message, sessionId);
        try {
            addMessage(message, sessionId);
        } catch (FieldNotFound exception) {
            log.error(exception.getMessage(), exception);
        }

    }
}
