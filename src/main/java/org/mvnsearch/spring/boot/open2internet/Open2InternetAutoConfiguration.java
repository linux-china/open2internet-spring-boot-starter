package org.mvnsearch.spring.boot.open2internet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.uri.UriTransportRegistry;
import io.rsocket.util.DefaultPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Open2Internet auto configuration
 *
 * @author linux_china
 */
@Configuration
@EnableConfigurationProperties(Open2InternetProperties.class)
public class Open2InternetAutoConfiguration implements ApplicationListener<WebServerInitializedEvent> {
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
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Open2InternetProperties properties;
    /**
     * web client to call local http service
     */
    private WebClient webClient;
    /**
     * local base web URI
     */
    private String localBaseWebUri = "http://127.0.0.1:8080";
    /**
     * RSocket connection
     */
    private RSocket rsocket;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int localListenPort = event.getWebServer().getPort();
        this.localBaseWebUri = "http://127.0.0.1:" + localListenPort;
        this.webClient = WebClient.create(localBaseWebUri);
        //validate disabled for not
        if (properties.isDisabled()) return;
        connect();
    }

    public void connect() {
        Open2InternetAuthentication authentication = new Open2InternetAuthentication(properties.getAccessToken(), properties.getCustomDomain());
        // connect to internet exposed service gateway
        rsocket = RSocketFactory
                .connect()
                .setupPayload(DefaultPayload.create(authentication.toString()))
                .acceptor(rsocketPeer -> new AbstractRSocket() {
                    @Override
                    public Mono<Payload> requestResponse(Payload payload) {
                        try {
                            HttpRequest httpRequest = objectMapper.readValue(new ByteBufferBackedInputStream(payload.getData()), HttpRequest.class);
                            return getHttpResponse(httpRequest).map(httpResponse -> DefaultPayload.create(toJson(httpResponse)));
                        } catch (Exception e) {
                            return Mono.just(DefaultPayload.create(toJsonException(e)));
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Mono<Void> fireAndForget(Payload payload) { //todo please use metadataPush instead later
                        try {
                            Map<String, Object> info = objectMapper.readValue(new ByteBufferBackedInputStream(payload.getData()), HashMap.class);
                            if ("app.exposed".equals(info.get("eventType"))) {
                                String internetUri = (String) info.get("uri");
                                String qrCodeUri = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + URLEncoder.encode(internetUri, "utf-8");
                                outputAsGreen(String.format(hint, info.get("token"), internetUri, qrCodeUri, localBaseWebUri, internetUri, localBaseWebUri));
                            }
                        } catch (Exception ignore) {

                        }
                        return Mono.empty();
                    }
                })
                .transport(UriTransportRegistry.clientForUri(properties.getUri()))
                .start()
                .block();
    }

    public void disConnect() {
        rsocket.dispose();
        rsocket = null;
    }

    public Mono<HttpResponse> getHttpResponse(HttpRequest httpRequest) {
        WebClient.RequestBodySpec webRequest = webClient.method(HttpMethod.valueOf(httpRequest.getMethod()))
                .uri(httpRequest.getRequestUri())
                .headers(httpHeaders -> {
                    for (Map.Entry<String, String> entry : httpRequest.getHeaders().entrySet()) {
                        httpHeaders.add(entry.getKey(), entry.getValue());
                    }
                });
        if (httpRequest.getBody() != null) {
            webRequest.body(Mono.just(new ByteArrayResource(httpRequest.getBody())), ByteArrayResource.class);
        }
        Mono<ClientResponse> clientResponseMono = webRequest.exchange();
        return clientResponseMono
                .map(clientResponse -> {
                    HttpResponse httpResponse = new HttpResponse();
                    httpResponse.setStatus(clientResponse.rawStatusCode());
                    HttpHeaders httpHeaders = clientResponse.headers().asHttpHeaders();
                    for (String name : httpHeaders.keySet()) {
                        httpResponse.addHeader(name, httpHeaders.getFirst(name));
                    }
                    return httpResponse;
                })
                .zipWith(clientResponseMono.flatMap(clientResponse -> clientResponse.bodyToMono(ByteArrayResource.class)), (httpResponse, byteArrayResource) -> {
                    if (byteArrayResource != null) {
                        httpResponse.setBody(byteArrayResource.getByteArray());
                    }
                    return httpResponse;
                });
    }

    public String toJson(HttpResponse httpResponse) {
        try {
            return objectMapper.writeValueAsString(httpResponse);
        } catch (Exception e) {
            return "{}";
        }
    }

    public String toJsonException(Exception e) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus(500);
        httpResponse.addHeader("Content-Type", "text/plain;charset=UTF-8");
        httpResponse.setBody(e.getMessage().getBytes());
        return toJson(httpResponse);
    }


    public void outputAsGreen(String text) {
        System.out.println(ANSI_GREEN + text + ANSI_RESET);
    }

}
