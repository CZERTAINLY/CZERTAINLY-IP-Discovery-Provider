package com.czertainly.discovery.ip.service.impl;

import com.czertainly.discovery.ip.dto.ConnectionResponse;
import com.czertainly.discovery.ip.service.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.X509Certificate;

@Service
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionServiceImpl.class);

    @Override
    public ConnectionResponse getCertificates(String url) throws IOException, SocketTimeoutException {

        logger.info("Requesting the certificate from URL {}", url);
        URL destination = new URL(url);
        try {
            HttpsURLConnection conn = (HttpsURLConnection) destination.openConnection();
            logger.debug("Connection object framed for the URL {}", url);
            conn.setConnectTimeout(300);
            conn.connect();
            logger.debug("Connected to {}", url);
            X509Certificate[] certs = (X509Certificate[]) conn.getServerCertificates();
            String cipher = conn.getCipherSuite().toString();
            conn.disconnect();
            logger.debug("Connection to {} terminated", url);
            return new ConnectionResponse(cipher, certs);
        } catch (ConnectException e) {
            throw new SocketTimeoutException("Unable to connect to URL");
        }

    }


}
