package org.mvnsearch.spring.boot.open2internet.rsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.uri.UriTransportRegistry;
import io.rsocket.util.DefaultPayload;
import org.mvnsearch.spring.boot.open2internet.JsonSupport;
import org.mvnsearch.spring.boot.open2internet.http.HttpRequest;
import org.mvnsearch.spring.boot.open2internet.http.LocalHttpServiceClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Rsocket connection manager implementation
 *
 * @author linux_china
 */
public class RSocketConnectionManagerImpl extends JsonSupport implements RSocketConnectionManager {
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    /**
     * authentication to upstream rsocket
     */
    private Open2InternetAuthentication authentication;
    /**
     * upstream rsocket uri
     */
    private String upstreamRsocketUri;
    /**
     * RSocket connection
     */
    private RSocket rsocket;
    /**
     * connect info
     */
    private ConnectInfo connectInfo;

    private LocalHttpServiceClient localHttpServiceClient;

    public RSocketConnectionManagerImpl(ObjectMapper objectMapper, String upstreamRsocketUri, Open2InternetAuthentication authentication, LocalHttpServiceClient localHttpServiceClient) {
        super(objectMapper);
        this.upstreamRsocketUri = upstreamRsocketUri;
        this.authentication = authentication;
        this.localHttpServiceClient = localHttpServiceClient;
    }

    public void connect() {
        if (this.rsocket != null && rsocket.availability() != 0.0) {
            return;
        }
        // connect to internet exposed service gateway
        rsocket = RSocketFactory
                .connect()
                .setupPayload(DefaultPayload.create(authentication.toString()))
                .acceptor(rsocketPeer -> new AbstractRSocket() {
                    @Override
                    public Mono<Payload> requestResponse(Payload payload) {
                        try {
                            HttpRequest httpRequest = readValue(payload.getData(), HttpRequest.class);
                            return localHttpServiceClient.getHttpResponse(httpRequest).map(httpResponse -> DefaultPayload.create(toJson(httpResponse)));
                        } catch (Exception e) {
                            return Mono.just(DefaultPayload.create(toJson(e)));
                        }
                    }

                    @Override
                    public Mono<Void> fireAndForget(Payload payload) { 
                        displayInfo(payload.getData());
                        return Mono.empty();
                    }

                    @Override
                    public Mono<Void> metadataPush(Payload payload) {
                        displayInfo(payload.getMetadata());
                        return Mono.empty();
                    }

                    private void displayInfo(ByteBuffer jsonData) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> info = readValue(jsonData, HashMap.class);
                            if ("app.exposed".equals(info.get("eventType"))) {
                                connectInfo = new ConnectInfo();
                                connectInfo.setInternetUri((String) info.get("uri"));
                                connectInfo.setLocalBaseWebUri(localHttpServiceClient.getLocalBaseWebUri());
                                connectInfo.setAccessToken((String) info.get("token"));
                                connectInfo.setQrCodeUri("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + URLEncoder.encode(connectInfo.getInternetUri(), "utf-8"));
                                outputAsGreen(connectInfo.hint());
                            }
                        } catch (Exception ignore) {

                        }
                    }
                })
                .transport(UriTransportRegistry.clientForUri(upstreamRsocketUri))
                .start()
                .block();
    }

    public void disConnect() {
        if (rsocket != null) {
            rsocket.dispose();
        }
        rsocket = null;
        connectInfo = null;
        outputAsGreen("Disconnected from Open2Internet.");
    }

    @Override
    public boolean isConnected() {
        return rsocket != null && rsocket.availability() > 0;
    }

    @Override
    public ConnectInfo getConnectInfo() {
        return this.connectInfo;
    }

    private void outputAsGreen(String text) {
        System.out.println(ANSI_GREEN + text + ANSI_RESET);
    }
}
