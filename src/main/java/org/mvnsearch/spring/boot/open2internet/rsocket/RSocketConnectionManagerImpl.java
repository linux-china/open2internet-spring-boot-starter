package org.mvnsearch.spring.boot.open2internet.rsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.mvnsearch.spring.boot.open2internet.JsonSupport;
import org.mvnsearch.spring.boot.open2internet.http.HttpRequest;
import org.mvnsearch.spring.boot.open2internet.http.LocalHttpServiceClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final Open2InternetAuthentication authentication;
    /**
     * upstream rsocket uri
     */
    private final String[] upstreamRsocketUris;
    /**
     * RSocket connection
     */
    private final List<RSocket> rsockets = new ArrayList<>();
    /**
     * connect info
     */
    private final List<ConnectInfo> connectInfoList = new ArrayList<>();

    private final LocalHttpServiceClient localHttpServiceClient;

    public RSocketConnectionManagerImpl(ObjectMapper objectMapper, String[] upstreamRsocketUris, Open2InternetAuthentication authentication, LocalHttpServiceClient localHttpServiceClient) {
        super(objectMapper);
        this.upstreamRsocketUris = upstreamRsocketUris;
        this.authentication = authentication;
        this.localHttpServiceClient = localHttpServiceClient;
    }

    public void connect() {
        if (!this.rsockets.isEmpty()) {
            return;
        }
        for (String upstreamRsocketUri : upstreamRsocketUris) {
            URI serverUri = URI.create(upstreamRsocketUri);
            // connect to internet exposed service gateway
            RSocket rsocket = RSocketConnector
                    .create()
                    .setupPayload(DefaultPayload.create(authentication.toString()))
                    .acceptor((connectionSetupPayload, rSocket) -> Mono.just(new RSocket() {
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
                                    ConnectInfo connectInfo = new ConnectInfo();
                                    connectInfo.setInternetUri((String) info.get("uri"));
                                    connectInfo.setLocalBaseWebUri(localHttpServiceClient.getLocalBaseWebUri());
                                    connectInfo.setAccessToken((String) info.get("token"));
                                    connectInfo.setQrCodeUri("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + URLEncoder.encode(connectInfo.getInternetUri(), "utf-8"));
                                    outputAsGreen(connectInfo.hint());
                                    connectInfoList.add(connectInfo);
                                }
                            } catch (Exception ignore) {

                            }
                        }
                    }))
                    .connect(TcpClientTransport.create(serverUri.getHost(), serverUri.getPort()))
                    .block();
            //todo deal with connection closed from upstream
            this.rsockets.add(rsocket);
        }
    }

    public void disConnect() {
        for (RSocket rsocket : rsockets) {
            try {
                rsocket.dispose();
            } catch (Exception ignore) {

            }
        }
        connectInfoList.clear();
        outputAsGreen("Disconnected from Open2Internet.");
    }

    @Override
    public boolean isConnected() {
        return !rsockets.isEmpty();
    }

    @Override
    public List<ConnectInfo> getConnectInfo() {
        return this.connectInfoList;
    }

    private void outputAsGreen(String text) {
        System.out.println(ANSI_GREEN + text + ANSI_RESET);
    }
}
