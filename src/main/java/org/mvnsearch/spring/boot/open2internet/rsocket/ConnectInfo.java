package org.mvnsearch.spring.boot.open2internet.rsocket;

/**
 * connect info
 *
 * @author linux_china
 */
public class ConnectInfo {

    private static String HINT_FORMAT = "open2internet by @linux_china\n" +
            "\n" +
            "Connected Status              online\n" +
            "Management Token              %s\n" +
            "Internet Web Interface        %s\n" +
            "Internet Web QR Code          %s\n" +
            "Local Web Interface           %s\n" +
            "Forwarding Rule               %s -> %s\n";

    private String internetUri;
    private String localBaseWebUri;
    private String qrCodeUri;
    private String accessToken;

    public String getInternetUri() {
        return internetUri;
    }

    public void setInternetUri(String internetUri) {
        this.internetUri = internetUri;
    }

    public String getLocalBaseWebUri() {
        return localBaseWebUri;
    }

    public void setLocalBaseWebUri(String localBaseWebUri) {
        this.localBaseWebUri = localBaseWebUri;
    }

    public String getQrCodeUri() {
        return qrCodeUri;
    }

    public void setQrCodeUri(String qrCodeUri) {
        this.qrCodeUri = qrCodeUri;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String hint() {
        return String.format(HINT_FORMAT, accessToken, internetUri, qrCodeUri, localBaseWebUri, internetUri, localBaseWebUri);
    }

}
