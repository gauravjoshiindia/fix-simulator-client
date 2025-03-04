package com.test;

import com.test.controller.RestServicesController;
import com.test.controller.Message;
import com.test.fix.ApplicationMessageCracker;
import com.test.fix.ClientApplicationAdapter;
import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import quickfix.*;
import quickfix.fix41.MessageCracker;

import java.util.ArrayList;

@EnableScheduling
@Slf4j
@EnableQuickFixJClient
@SpringBootApplication
@ComponentScan("com.test.*")
public class Application {

    @Autowired
    RestServicesController restServicesController;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public quickfix.Application clientApplication(MessageCracker messageCracker) {
        return new ClientApplicationAdapter(messageCracker);
    }

    @Bean
    public MessageCracker messageCracker() {
        return new ApplicationMessageCracker();
    }

//    @Bean
//    public Initiator clientInitiator(
//            quickfix.Application clientApplication,
//            MessageStoreFactory clientMessageStoreFactory,
//            SessionSettings clientSessionSettings,
//            LogFactory clientLogFactory,
//            MessageFactory clientMessageFactory) throws ConfigError {
//
//        return new ThreadedSocketInitiator(clientApplication, clientMessageStoreFactory, clientSessionSettings,
//                clientLogFactory, clientMessageFactory);
//    }


    @Bean
    public LogFactory clientLogFactory(SessionSettings clientSessionSettings) {
        return new FileLogFactory(clientSessionSettings);
    }


//    @Scheduled(cron = "*/10 * * * * *") // Run every day at midnight
//    public void dailyReportGeneration() {
//        if(restServicesController.getResult() == null){
//            restServicesController.setResult(new ArrayList<>());
//        }
//        restServicesController.getResult().add(new Message("1", "4.0", "Text1"));
//    }
}
