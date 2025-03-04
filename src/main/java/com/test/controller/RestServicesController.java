package com.test.controller;

import com.test.domain.LoginResponse;
import com.test.fix.SessionProperties;
import com.test.service.FixConnectionService;
import com.test.service.MessageStoreService;
import io.allune.quickfixj.spring.boot.starter.connection.ConnectorManager;
import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import quickfix.*;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Getter
@Setter
public class RestServicesController {

    @Autowired
    QuickFixJTemplate quickfixJTemplate;

    @Autowired
    private MessageStoreFactory messageStoreFactory;

    @Autowired
    private MessageStoreService messageStoreService;

    @Autowired
    private FixConnectionService fixConnectionService;

    public List<Message> getResult() {
        return result;
    }

    public void setResult(List<Message> result) {
        this.result = result;
    }

    private List<Message> result = new ArrayList<>();

    @RequestMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping(value = "/messages")
    public List<Message> messages(@RequestParam String fixVersion, @RequestParam String senderCompId, @RequestParam String targetCompId) {

        SessionID sessionID = new SessionID(fixVersion, senderCompId, targetCompId);

        return messageStoreService.getMessageMap().get(sessionID);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping(value = "/availableSessions")
    public List<SessionProperties> availableSessions() {

        return fixConnectionService.getConnections().keySet().stream().map((sessionId)->{return getSessionPropertiesFromSessionId(sessionId);}).collect(Collectors.toList());
    }

    private SessionProperties getSessionPropertiesFromSessionId(SessionID sessionId){
        SessionProperties props = new SessionProperties();
        props.setFixVersion(sessionId.getBeginString());
        props.setSenderCompId(sessionId.getSenderCompID());
        props.setTargetCompId(sessionId.getTargetCompID());
        return props;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping(value = "/startSession", consumes = "application/json")
    public ResponseEntity<LoginResponse> startSession(@RequestBody SessionProperties sessionProperties) throws IOException {

        try {
            SessionID sessionID = new SessionID(new BeginString(sessionProperties.getFixVersion()),
                    new SenderCompID(sessionProperties.getSenderCompId()),
                    new TargetCompID(sessionProperties.getTargetCompId()));

            if (!connectionService.getConnections().containsKey(sessionID)) {


                Dictionary dictionary = getDictionary(sessionProperties);
                SessionSettings sessionSettings = getSessionSettings(sessionProperties, sessionID, dictionary);

                MessageStoreFactory clientMessageStoreFactory = new FileStoreFactory(sessionSettings);
                LogFactory clientLogFactory = new FileLogFactory(sessionSettings);

                SocketInitiator initiator = new SocketInitiator(clientApplication, clientMessageStoreFactory,
                        sessionSettings, clientLogFactory, clientMessageFactory);

                initiator.getManagedSessions();


                //initiator.start();

                ConnectorManager connectorManager = new ConnectorManager(initiator);
                connectionService.getConnections().putIfAbsent(sessionID, connectorManager);

                connectorManager.start();

                return new ResponseEntity<LoginResponse>(new LoginResponse(true, false), HttpStatus.OK);
            } else if (connectionService.getConnections().get(sessionID).isRunning()) {
                return new ResponseEntity<LoginResponse>(new LoginResponse(true, true), HttpStatus.OK);
            } else {
                connectionService.getConnections().get(sessionID).start();
                return new ResponseEntity<LoginResponse>(new LoginResponse(true, false), HttpStatus.OK);
            }
        } catch (Exception exception) {

            return new ResponseEntity<LoginResponse>(new LoginResponse(false, false), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private SessionSettings getSessionSettings(@RequestBody SessionProperties sessionProperties, SessionID sessionID, Dictionary dictionary) throws ConfigError {
        SessionSettings sessionSettings = new SessionSettings();
        sessionSettings.set(sessionID, dictionary);
        sessionSettings.setString("ConnectionType", "initiator");
        sessionSettings.setString("BeginString", sessionProperties.getFixVersion());
        sessionSettings.setString("SenderCompID", sessionProperties.getSenderCompId());
        sessionSettings.setString("TargetCompID", sessionProperties.getTargetCompId());
        sessionSettings.setString("SocketConnectHost", sessionProperties.getHost());
        sessionSettings.setString("SocketConnectPort", String.valueOf(sessionProperties.getPort()));
        sessionSettings.setLong("HeartBtInt", 30);
        sessionSettings.setString("FileLogPath", "logs-client");
        sessionSettings.setString("StartTime", "00:00:00");
        sessionSettings.setString("EndTime", "00:00:00");
        sessionSettings.setString("FileStorePath", "data-client");
        sessionSettings.setBool("UseDataDictionary", false);
        return sessionSettings;
    }

    private Dictionary getDictionary(@RequestBody SessionProperties sessionProperties) {
        Dictionary dictionary = new Dictionary();
        dictionary.setString("ConnectionType", "initiator");
        dictionary.setString("BeginString", sessionProperties.getFixVersion());
        dictionary.setString("SenderCompID", sessionProperties.getSenderCompId());
        dictionary.setString("TargetCompID", sessionProperties.getTargetCompId());
        dictionary.setString("SocketConnectHost", sessionProperties.getHost());
        dictionary.setString("SocketConnectPort", String.valueOf(sessionProperties.getPort()));
        dictionary.setLong("HeartBtInt", 30);
        dictionary.setString("FileLogPath", "logs-client");
        dictionary.setString("StartTime", "00:00:00");
        dictionary.setString("EndTime", "00:00:00");
        dictionary.setString("FileStorePath", "data-client");
        dictionary.setBool("UseDataDictionary", false);
        return dictionary;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping(value = "/stopSession")
    public ResponseEntity<String> stopSession(@RequestBody SessionProperties sessionProperties) throws ConfigError {

        SessionID sessionID = new SessionID(new BeginString(sessionProperties.getFixVersion()),
                new SenderCompID(sessionProperties.getSenderCompId()),
                new TargetCompID(sessionProperties.getTargetCompId()));

        if (!connectionService.getConnections().containsKey(sessionID)) {
            return new ResponseEntity<String>("No Such Session Exists", HttpStatus.OK);
        }else if(connectionService.getConnections().get(sessionID).isRunning()){
            connectionService.getConnections().get(sessionID).stop();
            return new ResponseEntity<String>("Session Logged Off", HttpStatus.OK);
        }else{
            return new ResponseEntity<String>("Session Exists But Not Running", HttpStatus.OK);
        }

    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping(value = "/stopAndClearSession")
    public ResponseEntity<String> stopAndClearSession(@RequestBody SessionProperties sessionProperties) throws ConfigError {

        stopSession(sessionProperties);
        fixConnectionService.getConnections().remove(new SessionID(sessionProperties.getFixVersion(), sessionProperties.getSenderCompId(), sessionProperties.getTargetCompId()));
        return new ResponseEntity<String>("OK", HttpStatus.OK);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping(value = "/uploadMessagesForSession", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadMessagesForSession(@RequestParam String senderCompId, @RequestParam String targetCompId, @RequestParam String fixVersion, @RequestPart("file")MultipartFile file) throws IOException {

        System.out.println("File Name of Uploaded File ::::;;: " + file.getName());
        System.out.println("FIX Session " + fixVersion +":"+senderCompId+"->"+targetCompId);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            while((line = reader.readLine())!=null){
                quickfix.Message parsedMessage = validateFixMessage(line, fixVersion);
                SessionID sessionId = new SessionID(fixVersion, senderCompId, targetCompId);

                ConnectorManager connectorManager = fixConnectionService.getConnections().get(sessionId);
                if(connectorManager.isRunning()){
                    Session.sendToTarget(parsedMessage, sessionId);
                }
            }
            return new ResponseEntity<String>("File Uploaded ", HttpStatus.OK);
        }catch(IllegalArgumentException illegalArg){
            return new ResponseEntity<String>(illegalArg.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(SessionNotFound sessionNotFound){
            return new ResponseEntity<String>(sessionNotFound.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }



    }

    private quickfix.Message validateFixMessage(String line, String fixVersion) throws IllegalArgumentException {
        String dictionaryPath = fixVersion.replaceAll("\\.", "") + ".xml";
        System.out.println(dictionaryPath);
        try {
            DataDictionary dictionary = new DataDictionary(dictionaryPath);
            quickfix.Message quickfixJMessage = new quickfix.Message(line, dictionary);
            dictionary.validate(quickfixJMessage);

            return quickfixJMessage;

        }catch(ConfigError configError){
            throw new IllegalArgumentException("Invalid Fix Version provided : " + fixVersion);
        }catch(InvalidMessage invalidMessage){
            throw new IllegalArgumentException("Invalid Message in File : " + fixVersion + "  " + invalidMessage.getMessage());
        }catch(IncorrectTagValue incorrectTagValue){
            throw new IllegalArgumentException("Invalid Tag Value in Message : " + line + " Field: " +incorrectTagValue.getField());
        }catch(FieldNotFound fieldNotFound){
            throw new IllegalArgumentException("Field Not Found in Message : " + line + " Field: " + fieldNotFound.field);
        }catch(IncorrectDataFormat incorrectDataFormat){
            throw new IllegalArgumentException("Incorrect Data Format in Message : " + line + " Incorrect Format : " + incorrectDataFormat.getField()
            );
        }
    }
    @Autowired
    quickfix.Application clientApplication;


    @Autowired
    MessageFactory clientMessageFactory;

    @Autowired
    FixConnectionService connectionService;


}
