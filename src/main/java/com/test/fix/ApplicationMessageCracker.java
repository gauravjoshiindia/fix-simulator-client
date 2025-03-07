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

import lombok.extern.slf4j.Slf4j;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.fix41.MessageCracker;

@Slf4j
public class ApplicationMessageCracker extends MessageCracker {

	@Override
	public void onMessage(quickfix.fix41.OrderCancelRequest orderCancelRequest, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("Message received for sessionID={}: {}", sessionID, orderCancelRequest);
		log.info("*****************");
	}
}