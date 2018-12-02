package org.mvnsearch.spring.boot.open2internet;

import org.mvnsearch.spring.boot.open2internet.http.LocalHttpServiceClient;
import org.mvnsearch.spring.boot.open2internet.rsocket.RSocketConnectionManager;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.HashMap;

/**
 * open2internet endpoint: get info or connect/disconnect dynamically
 *
 * @author linux_china
 */
@Endpoint(id = "open2internet")
public class Open2InternetEndpoint {
    Open2InternetProperties properties;
    LocalHttpServiceClient localHttpServiceClient;
    RSocketConnectionManager rsocketConnectionManager;

    public Open2InternetEndpoint() {

    }

    public void setProperties(Open2InternetProperties properties) {
        this.properties = properties;
    }

    public void setLocalHttpServiceClient(LocalHttpServiceClient localHttpServiceClient) {
        this.localHttpServiceClient = localHttpServiceClient;
    }

    public void setRsocketConnectionManager(RSocketConnectionManager rsocketConnectionManager) {
        this.rsocketConnectionManager = rsocketConnectionManager;
    }

    @ReadOperation
    public Object info() {
        if (rsocketConnectionManager.isConnected()) {
            return rsocketConnectionManager.getConnectInfo();
        } else {
            HashMap<String, Object> info = new HashMap<>();
            info.put("connected", false);
            return info;
        }
    }

    @WriteOperation
    public void update(@Selector String ops) {
        if ("connect".equals(ops)) {
            rsocketConnectionManager.connect();
        } else if ("disconnect".equals(ops)) {
            rsocketConnectionManager.disConnect();
        }
    }
}
