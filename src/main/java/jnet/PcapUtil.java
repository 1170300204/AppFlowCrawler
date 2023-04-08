package jnet;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ParseUtil;

import java.io.File;
import java.util.*;


public class PcapUtil {

    public static final Logger log = LoggerFactory.getLogger(PcapUtil.class);

    //threshold ms
    public static List<String> splitPcapByThreshold(String pcapFIleName, long threshold, int minLength, String outputDirectory) {
        List<String> resFiles = new ArrayList<>();
        StringBuilder error = new StringBuilder();

        Pcap pcap = Pcap.openOffline(pcapFIleName, error);
        final PcapDumper[] dumper = {null};
        if (null == pcap) {
            log.error("Error while opening file :" + error);
            return null;
        }
        PcapPacketHandler<String> packetHandler = new PcapPacketHandler<String>() {
            private long previousTimestamp = 0;
            private int count = 0;
            @Override
            public void nextPacket(PcapPacket packet, String s) {
                if (packet.getTotalSize() < minLength)    return;
                PcapHeader header = packet.getCaptureHeader();
                long timestamp = header.timestampInMillis();
                if (previousTimestamp!=0 && timestamp - previousTimestamp >= threshold) {
                    dumper[0].flush();
                    dumper[0].close();
                    resFiles.add(outputDirectory + "/" + count + ".pcap");
                    count++;
                    dumper[0] = null;
                }
                if (dumper[0] == null) {
                    dumper[0] = pcap.dumpOpen(outputDirectory + "/" + count + ".pcap");
                }
                dumper[0].dump(packet);
                previousTimestamp = timestamp;
            }
        };
        pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "jNetPcap rocks!");
        if (dumper[0]!=null) {
            dumper[0].flush();
            dumper[0].close();
        }
        pcap.close();

        return resFiles;
    }

    public static Map<String, String> getSNIFromPcap(String pcapFIleName) {
        StringBuilder error = new StringBuilder();
        Pcap pcap = Pcap.openOffline(pcapFIleName, error);
        if (null == pcap) {
            log.error("Error while opening file :" + error);
            return null;
        }

        Map<String, String> sniMap = new HashMap<>();

        PcapPacketHandler<String> packetHandler = (packet, user) -> {
            Tcp tcp = new Tcp();
            if (packet.hasHeader(tcp) && tcp.destination() == 443 && packet.size() > tcp.getOffset()) {
                byte[] payload = tcp.getPayload();
                if (payload != null && payload.length > 0) {
                    int offset;
                    // SSL/TLS handshake record starts with 0x16
                    if (payload[0] == 0x16) {
//                            System.out.print("get SSL/TLS ");
                        // SSL/TLS handshake protocol version is at offset 1-3
                        int majorVersion = payload[1] & 0xFF;
                        int minorVersion = payload[2] & 0xFF;
//                            System.out.print(majorVersion + "." + minorVersion);
                        if (majorVersion < 3) {
                            //TLS version not support SNI
                            return;
                        }
                        // SSL/TLS handshake protocol length is at offset 3-5
                        int length = ((payload[3] & 0xFF) << 8) | (payload[4] & 0xFF);
//                            System.out.println(" length:" + length);
                        offset = 5;

                        // SSL/TLS handshake protocol message type is at offset 0
                        int type = payload[offset] & 0xFF;
                        // SSL/TLS handshake protocol message length is at offset 1-3
                        int msgLen = ((payload[offset + 1] & 0xFF) << 16)
                                | ((payload[offset + 2] & 0xFF) << 8) | (payload[offset + 3] & 0xFF);
                        // SSL/TLS Server Name Indication (SNI) extension message type is 0x00
                        if (type == 0x01) {
//                                System.out.println("get Client Hello");
                            offset += 38;
                            /* Session ID */
                            if (offset + 1 > length)    return;
                            int len = payload[offset] & 0xFF;
                            offset = offset + 1 + len;
                            /* Cipher Suites */
                            if (offset + 2 > length)    return;
                            len = ((payload[offset] & 0xFF) << 8) | (payload[offset + 1] & 0xFF);
                            offset = offset + 2 + len;
                            /* Compression Methods */
                            if (offset + 1 > length)    return;
                            len = payload[offset] & 0xFF;
                            offset = offset + 1 + len;
                            if (offset == length && majorVersion == 3 && minorVersion == 0) {
//                                    System.out.println("Received SSL 3.0 handshake without extensions");
                                return;
                            }
                            /* Extensions */
                            if (offset + 2 > length)    return;
//                            len = ((payload[offset] & 0xFF) << 8) | (payload[offset + 1] & 0xFF);
                            offset = offset + 2;
                            while(offset + 4 <= length) {
                                len = ((payload[offset + 2] & 0xFF) << 8) | (payload[offset + 3] & 0xFF);

                                if ((payload[offset] & 0xFF) == 0x00 && (payload[offset+1] & 0xFF) == 0x00) {
//                                        System.out.println("get SNI");
                                    offset = offset + 2;
                                    int sniExtensionLen = ((payload[offset] & 0xFF) << 8) | (payload[offset + 1] & 0xFF);
//                                        System.out.println(sniExtensionLen);
                                    offset = offset + 2;
                                    if ((payload[offset + 2] & 0xFF) == 0x00) {
//                                            System.out.println("hostname");
                                        int hostnameLength = ((payload[offset + 3] & 0xFF) << 8) | (payload[offset + 4] & 0xFF);
//                                            System.out.println(hostnameLength);
                                        String sni = new String(Arrays.copyOfRange(payload, offset + 5, offset + 5 + hostnameLength));
                                        Ip4 ip4 = new Ip4();
                                        if (packet.hasHeader(ip4)) {
                                            String dstIp = FormatUtils.ip(ip4.destination());
                                            sniMap.put(dstIp,sni);
                                        }
                                    }

                                }
                                offset = offset + 4 + len;
                            }
                        }
                    }
                }
            }
        };
        pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "jNetPcap rocks!");
        pcap.close();
        log.info("From pcap get sni : " + sniMap);
        return sniMap;
    }

    public static String filterPcapBySni(String pcapFIleName, int appId, String outputDirectory) {
        Set<String> sniFromDB  = ParseUtil.getSNIFromDB(appId);
        if (sniFromDB==null)    return null;
        Map<String, String> sniFromPcap = getSNIFromPcap(pcapFIleName);
        if (null == sniFromPcap)    return null;
        Set<String> ips = new HashSet<>();
//        for (String sni : sniFromDB) {
//            if (sniFromPcap.containsValue(sni)) {
//                ips.add(sniFromPcap.get(sni));
//            }
//        }
        for (String ip : sniFromPcap.keySet()) {
            if (sniFromDB.contains(sniFromPcap.get(ip))) {
                ips.add(ip);
            }
        }
        log.info("Ip in pcap match SNI in DB : " + ips);

        StringBuilder error = new StringBuilder();
        Pcap pcap = Pcap.openOffline(pcapFIleName, error);
        if (null == pcap) {
            log.error("Error while opening file :" + error);
            return null;
        }

        String resPcap = outputDirectory + File.separator + "filtered.pcap";
        PcapDumper dumper = pcap.dumpOpen(resPcap);
        Ip4 ip4 = new Ip4();
        PcapPacketHandler<String> packetHandler = (packet, user) -> {
            if (packet.hasHeader(ip4)) {
                String dstIp = FormatUtils.ip(ip4.destination());
                String srcIp = FormatUtils.ip(ip4.source());
                if (ips.contains(dstIp) || ips.contains(srcIp)) {
                    dumper.dump(packet);
                }
            }
        };
        pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "jNetPcap rocks!");
        dumper.flush();
        dumper.close();
        pcap.close();
        return resPcap;
    }


    public static void main(String[] args) {

//        System.out.println(splitPcapByThreshold("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2\\input2.pcap", 4000, 20, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2"));
//        getSNIFromPcap("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2\\input2.pcap");
        System.out.println(filterPcapBySni("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2\\input2.pcap", 1, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2"));
    }
}
