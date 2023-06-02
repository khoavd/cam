package com.cam;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class MultiThread {

    static Logger logger = Logger.getLogger(MultiThread.class.getName());

    private int NUMBER_OF_THREADS = 10;

    public static void main(String[] args) {

        MultiThread mt = new MultiThread();

        List<String> notifications = mockNotiList();

        logger.info("Total notification [" + mockNotiList().size() + "]");

        if (notifications.size() <= mt.NUMBER_OF_THREADS) {
            mt.NUMBER_OF_THREADS = notifications.size();
        }

        Thread [] threads = new Thread[mt.NUMBER_OF_THREADS];

        final int notiPerThread = notifications.size()/ mt.NUMBER_OF_THREADS;
        final int remainingNotis = notifications.size() % mt.NUMBER_OF_THREADS;


        long start = ZonedDateTime.now().toInstant().toEpochMilli();

        for (int t = 0; t <  mt.NUMBER_OF_THREADS; t++) {
            final int thread = t;

            threads[t] = new Thread() {
                @Override
                public void run() {
                    mt.processNotify(notifications,  mt.NUMBER_OF_THREADS, thread, notiPerThread, remainingNotis);
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

        logger.info("TOTAL 1 Time =" + (end-start) + "ms");

        long start2 = ZonedDateTime.now().toInstant().toEpochMilli();

        for (String s : mockNotiList()) {
            logger.info("processing " + s + " in thread " + Thread.currentThread().getName());
        }
/*        mockNotiList().stream().forEach(e -> {
            logger.info("processing " + e + " in thread " + Thread.currentThread().getName());
        });*/

        long end2 = ZonedDateTime.now().toInstant().toEpochMilli();

        logger.info("TOTAL 2 Time =" + (end2-start2) + "ms");
    }

    private void processNotify(List<String> notifications,
                               int numberOfThreads,
                               int thread,
                               int notisPerThread,
                               int remainingNotis) {

        List<String> inNotis = new ArrayList<>();

        for (int i = thread * notisPerThread; i < (thread + 1) * notisPerThread; i++) {
            inNotis.add(notifications.get(i));
        }

        if (thread == numberOfThreads - 1 && remainingNotis > 0) {
            for (int j = notifications.size() - remainingNotis; j < notifications.size(); j ++) {
                inNotis.add(notifications.get(j));
            }
        }


        for (String noti : inNotis) {
            try {
                logger.info("processing " + noti + " in thread " + Thread.currentThread().getName());

            } catch (Exception e) {

            }
        }
    }

    public static List<String> mockNotiList() {
        return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10","1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    }
}
