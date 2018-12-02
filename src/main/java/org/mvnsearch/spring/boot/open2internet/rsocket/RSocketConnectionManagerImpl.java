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

    private static String hint = "open2internet by @linux_china\n" +
            "\n" +
            "Connected Status              online\n" +
            "Management Token              %s\n" +
            "Internet Web Interface        %s\n" +
            "Internet Web QR Code          %s\n" +
            "Local Web Interface           %s\n" +
            "Forwarding Rule               %s -> %s\n";
    /**
     * RSocket connection
     */
    private RSocket rsocket;

    private LocalHttpServiceClient localHttpServiceClient;

    private Open2InternetAuthentication authentication;
    private String upstreamRsocketUri;

    public RSocketConnectionManagerImpl(ObjectMapper objectMapper, String upstreamRsocketUri, Open2InternetAuthentication authentication, LocalHttpServiceClient localHttpServiceClient) {
        super(objectMapper);
        this.upstreamRsocketUri = upstreamRsocketUri;
        this.authentication = authentication;
        this.localHttpServiceClient = localHttpServiceClient;
    }

    public void connect() {
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
                    public Mono<Void> fireAndForget(Payload payload) { //todo please use metadataPush instead later
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> info = readValue(payload.getData(), HashMap.class);
                            if ("app.exposed".equals(info.get("eventType"))) {
                                String internetUri = (String) info.get("uri");
                                String localBaseWebUri = localHttpServiceClient.getLocalBaseWebUri();
                                String qrCodeUri = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + URLEncoder.encode(internetUri, "utf-8");
                                outputAsGreen(String.format(hint, info.get("token"), internetUri, qrCodeUri, localBaseWebUri, internetUri, localBaseWebUri));
                            }
                        } catch (Exception ignore) {

                        }
                        return Mono.empty();
                    }
                })
                .transport(UriTransportRegistry.clientForUri(upstreamRsocketUri))
                .start()
                .block();
    }

    public void disConnect() {
        rsocket.dispose();
        rsocket = null;
    }

    @Override
    public boolean isConnected() {
        return rsocket != null && rsocket.availability() > 0;
    }

    private void outputAsGreen(String text) {
        System.out.println(ANSI_GREEN + text + ANSI_RESET);
    }
}
