package org.mvnsearch.spring.boot.open2internet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvnsearch.spring.boot.open2internet.http.LocalHttpServiceClient;
import org.mvnsearch.spring.boot.open2internet.http.LocalHttpServiceClientImpl;
import org.mvnsearch.spring.boot.open2internet.rsocket.Open2InternetAuthentication;
import org.mvnsearch.spring.boot.open2internet.rsocket.RSocketConnectionManager;
import org.mvnsearch.spring.boot.open2internet.rsocket.RSocketConnectionManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * Open2Internet auto configuration
 *
 * @author linux_china
 */
@Configuration
@EnableConfigurationProperties(Open2InternetProperties.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class Open2InternetAutoConfiguration implements ApplicationListener<WebServerInitializedEvent> {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Open2InternetProperties properties;
    /**
     * local http service client
     */
    private LocalHttpServiceClient localHttpServiceClient;
    /**
     * rsocket connect manager
     */
    private RSocketConnectionManager rSocketConnectionManager;

    private Open2InternetEndpoint endpoint = new Open2InternetEndpoint();

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int localListenPort = event.getWebServer().getPort();
        String localBaseWebUri = "http://127.0.0.1:" + localListenPort;
        this.localHttpServiceClient = new LocalHttpServiceClientImpl(objectMapper, localBaseWebUri);
        Open2InternetAuthentication authentication = new Open2InternetAuthentication(properties.getAccessToken(), properties.getCustomDomain());
        this.rSocketConnectionManager = new RSocketConnectionManagerImpl(objectMapper, properties.getUri(), authentication, localHttpServiceClient);
        //reset endpoint
        this.endpoint.setProperties(properties);
        this.endpoint.setLocalHttpServiceClient(localHttpServiceClient);
        this.endpoint.setRsocketConnectionManager(rSocketConnectionManager);
        //validate enable for not
        if (properties.isEnable()) {
            rSocketConnectionManager.connect();
        }
    }

    @Bean
    public Open2InternetEndpoint open2InternetEndpoint() {
        return this.endpoint;
    }

    @PreDestroy
    public void destroy() {
        rSocketConnectionManager.disConnect();
    }

}
