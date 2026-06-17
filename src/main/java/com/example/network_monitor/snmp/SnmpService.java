package com.example.network_monitor.snmp;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

@Service
public class SnmpService {

    private static final String COMMUNITY = "public";
    private static final int PORT = 161;

    // System uptime
    private static final String OID_UPTIME = "1.3.6.1.2.1.1.3.0";
    // Interface bytes IN (eth0)
    private static final String OID_BYTES_IN = "1.3.6.1.2.1.2.2.1.10.2";
    // Interface bytes OUT (eth0)
    private static final String OID_BYTES_OUT = "1.3.6.1.2.1.2.2.1.16.2";
    // Total RAM
    private static final String OID_TOTAL_RAM = "1.3.6.1.4.1.2021.4.5.0";
    // Available RAM
    private static final String OID_AVAIL_RAM = "1.3.6.1.4.1.2021.4.11.0";
    // CPU idle percentage
    private static final String OID_CPU_IDLE = "1.3.6.1.4.1.2021.11.11.0";

    public String snmpGet(String ipAddress, String oid) {
        try {
            DefaultUdpTransportMapping transport =
                    new DefaultUdpTransportMapping();
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY));
            target.setAddress(
                    GenericAddress.parse("udp:" + ipAddress + "/" + PORT));
            target.setRetries(2);
            target.setTimeout(3000);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            Snmp snmp = new Snmp(transport);
            ResponseEvent response = snmp.get(pdu, target);

            if (response != null &&
                    response.getResponse() != null) {
                String result = response.getResponse()
                        .get(0).getVariable().toString();
                snmp.close();
                return result;
            }
            snmp.close();
        } catch (Exception e) {
            System.err.println("SNMP error for "
                    + ipAddress + ": " + e.getMessage());
        }
        return null;
    }

    public double getCpuUsage(String ipAddress) {
        String cpuIdle = snmpGet(ipAddress, OID_CPU_IDLE);
        if (cpuIdle != null && !cpuIdle.equals("noSuchObject")
                && !cpuIdle.equals("noSuchInstance")) {
            double idle = Double.parseDouble(cpuIdle);
            return 100.0 - idle;
        }
        return 0.0;
    }

    public double getMemoryUsage(String ipAddress) {
        String total = snmpGet(ipAddress, OID_TOTAL_RAM);
        String avail = snmpGet(ipAddress, OID_AVAIL_RAM);
        if (total != null && avail != null
                && !total.equals("noSuchObject")
                && !avail.equals("noSuchObject")) {
            double totalRam = Double.parseDouble(total);
            double availRam = Double.parseDouble(avail);
            return ((totalRam - availRam) / totalRam) * 100.0;
        }
        return 0.0;
    }

    public double getUptime(String ipAddress) {
        String uptime = snmpGet(ipAddress, OID_UPTIME);
        if (uptime != null && !uptime.equals("noSuchObject")) {
            return Double.parseDouble(
                    uptime.replaceAll("[^0-9]", ""));
        }
        return 0.0;
    }
}