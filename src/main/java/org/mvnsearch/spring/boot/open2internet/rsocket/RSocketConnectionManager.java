package org.mvnsearch.spring.boot.open2internet.rsocket;

import java.util.List;

/**
 * RSocket connection manager
 *
 * @author linux_china
 */
public interface RSocketConnectionManager {

    void connect();

    void disConnect();

    boolean isConnected();

    List<ConnectInfo> getConnectInfo();
}
