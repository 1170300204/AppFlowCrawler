package flow;

import java.sql.Timestamp;
import java.util.Date;

public class BasicFlow {

    private long id;
    private String serverHost;
    private String srcIp;
    private int srcPort;
    private String dstIp;
    private int dstPort;
    private int protocol;
    private Timestamp timestamp;
    private boolean isInevitable;

    private FlowFeature feature;

    public BasicFlow() {
    }

    public BasicFlow(long id, String serverHost, String srcIp, int srcPort, String dstIp, int dstPort, int protocol, Timestamp timestamp, FlowFeature feature) {
        this.id = id;
        this.serverHost = serverHost;
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.timestamp = timestamp;
        this.feature = feature;
    }

    public BasicFlow(String serverHost, String dstIp, int dstPort, int protocol, Timestamp timestamp, FlowFeature feature) {
        this.serverHost = serverHost;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
        this.protocol = protocol;
        this.timestamp = timestamp;
        this.feature = feature;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public FlowFeature getFeature() {
        return feature;
    }

    public void setFeature(FlowFeature feature) {
        this.feature = feature;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsInevitable() {
        return isInevitable;
    }

    public void setInevitable(boolean inevitable) {
        isInevitable = inevitable;
    }

    @Override
    public String toString() {
        return "BasicFlow{" +
                "id=" + id +
                ", serverHost='" + serverHost + '\'' +
                ", srcIp='" + srcIp + '\'' +
                ", srcPort=" + srcPort +
                ", dstIp='" + dstIp + '\'' +
                ", dstPort=" + dstPort +
                ", protocol=" + protocol +
                ", timestamp='" + timestamp + '\'' +
                ", isInevitable=" + isInevitable +
                ", feature=" + feature +
                '}';
    }
}
