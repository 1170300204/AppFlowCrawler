package jnet;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class PcapReader {

    public static final Logger log = LoggerFactory.getLogger(PcapReader.class);

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


    public static void main(String[] args) {

        System.out.println(splitPcapByThreshold("D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2\\input2.pcap", 4000, 20, "D:\\Workspace\\IDEA Projects\\AppFlowCrawler\\input\\vk2"));

    }
}
