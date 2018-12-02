package org.mvnsearch.spring.boot.open2internet.rsocket;

/**
 * RSocket connection manager
 *
 * @author linux_china
 */
public interface RSocketConnectionManager {

    void connect();

    void disConnect();

    boolean isConnected();

    ConnectInfo getConnectInfo();
}
