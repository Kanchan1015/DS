package com.logsystem.service;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;

@Service
public class TimeSyncService {

    private static final String NTP_SERVER = "pool.ntp.org";

    public Instant getSynchronizedTimestamp() {
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(3000);
            InetAddress hostAddr = InetAddress.getByName(NTP_SERVER);
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails();
            Date serverTime = info.getMessage().getTransmitTimeStamp().getDate();
            return serverTime.toInstant();
        } catch (Exception e) {
            // Fallback to system time if NTP fails
            return Instant.now();
        }
    }

    public Instant normalizeTimestamp(Instant incomingTimestamp) {
        return incomingTimestamp != null ? incomingTimestamp : getSynchronizedTimestamp();
    }
}