package flow;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FlowFeature {

    private double totalPktMaxLength;
    private double totalPktMinLength;
    private double totalPktMeanLength;
    private double totalPktStdLength;

    private long fwdPktCount;
    private long bwdPktCount;
    private double fwdPktTotalLength;
    private double bwdPktTotalLength;
    private double fwdPktMaxLength;
    private double bwdPktMaxLength;
    private double fwdPktMinLength;
    private double bwdPktMinLength;
    private double fwdPktMeanLength;
    private double bwdPktMeanLength;
    private double fwdPktStdLength;
    private double bwdPktStdLength;

    public FlowFeature() {
    }

    public FlowFeature(double totalPktMaxLength, double totalPktMinLength, double totalPktMeanLength, double totalPktStdLength,
                       long fwdPktCount, long bwdPktCount, double fwdPktTotalLength, double bwdPktTotalLength,
                       double fwdPktMaxLength, double bwdPktMaxLength, double fwdPktMinLength, double bwdPktMinLength,
                       double fwdPktMeanLength, double bwdPktMeanLength, double fwdPktStdLength, double bwdPktStdLength) {
        this.totalPktMaxLength = totalPktMaxLength;
        this.totalPktMinLength = totalPktMinLength;
        this.totalPktMeanLength = totalPktMeanLength;
        this.totalPktStdLength = totalPktStdLength;
        this.fwdPktCount = fwdPktCount;
        this.bwdPktCount = bwdPktCount;
        this.fwdPktTotalLength = fwdPktTotalLength;
        this.bwdPktTotalLength = bwdPktTotalLength;
        this.fwdPktMaxLength = fwdPktMaxLength;
        this.bwdPktMaxLength = bwdPktMaxLength;
        this.fwdPktMinLength = fwdPktMinLength;
        this.bwdPktMinLength = bwdPktMinLength;
        this.fwdPktMeanLength = fwdPktMeanLength;
        this.bwdPktMeanLength = bwdPktMeanLength;
        this.fwdPktStdLength = fwdPktStdLength;
        this.bwdPktStdLength = bwdPktStdLength;
    }

    public double getTotalPktMaxLength() {
        return totalPktMaxLength;
    }

    public void setTotalPktMaxLength(double totalPktMaxLength) {
        this.totalPktMaxLength = totalPktMaxLength;
    }

    public double getTotalPktMinLength() {
        return totalPktMinLength;
    }

    public void setTotalPktMinLength(double totalPktMinLength) {
        this.totalPktMinLength = totalPktMinLength;
    }

    public double getTotalPktMeanLength() {
        return totalPktMeanLength;
    }

    public void setTotalPktMeanLength(double totalPktMeanLength) {
        this.totalPktMeanLength = totalPktMeanLength;
    }

    public double getTotalPktStdLength() {
        return totalPktStdLength;
    }

    public void setTotalPktStdLength(double totalPktStdLength) {
        this.totalPktStdLength = totalPktStdLength;
    }

    public long getFwdPktCount() {
        return fwdPktCount;
    }

    public void setFwdPktCount(long fwdPktCount) {
        this.fwdPktCount = fwdPktCount;
    }

    public long getBwdPktCount() {
        return bwdPktCount;
    }

    public void setBwdPktCount(long bwdPktCount) {
        this.bwdPktCount = bwdPktCount;
    }

    public double getFwdPktTotalLength() {
        return fwdPktTotalLength;
    }

    public void setFwdPktTotalLength(double fwdPktTotalLength) {
        this.fwdPktTotalLength = fwdPktTotalLength;
    }

    public double getBwdPktTotalLength() {
        return bwdPktTotalLength;
    }

    public void setBwdPktTotalLength(double bwdPktTotalLength) {
        this.bwdPktTotalLength = bwdPktTotalLength;
    }

    public double getFwdPktMaxLength() {
        return fwdPktMaxLength;
    }

    public void setFwdPktMaxLength(double fwdPktMaxLength) {
        this.fwdPktMaxLength = fwdPktMaxLength;
    }

    public double getBwdPktMaxLength() {
        return bwdPktMaxLength;
    }

    public void setBwdPktMaxLength(double bwdPktMaxLength) {
        this.bwdPktMaxLength = bwdPktMaxLength;
    }

    public double getFwdPktMinLength() {
        return fwdPktMinLength;
    }

    public void setFwdPktMinLength(double fwdPktMinLength) {
        this.fwdPktMinLength = fwdPktMinLength;
    }

    public double getBwdPktMinLength() {
        return bwdPktMinLength;
    }

    public void setBwdPktMinLength(double bwdPktMinLength) {
        this.bwdPktMinLength = bwdPktMinLength;
    }

    public double getFwdPktMeanLength() {
        return fwdPktMeanLength;
    }

    public void setFwdPktMeanLength(double fwdPktMeanLength) {
        this.fwdPktMeanLength = fwdPktMeanLength;
    }

    public double getBwdPktMeanLength() {
        return bwdPktMeanLength;
    }

    public void setBwdPktMeanLength(double bwdPktMeanLength) {
        this.bwdPktMeanLength = bwdPktMeanLength;
    }

    public double getFwdPktStdLength() {
        return fwdPktStdLength;
    }

    public void setFwdPktStdLength(double fwdPktStdLength) {
        this.fwdPktStdLength = fwdPktStdLength;
    }

    public double getBwdPktStdLength() {
        return bwdPktStdLength;
    }

    public void setBwdPktStdLength(double bwdPktStdLength) {
        this.bwdPktStdLength = bwdPktStdLength;
    }

    public List<Double> getFeatureList() {
        List<Double> ret = new LinkedList<>();
        ret.add(totalPktMaxLength);
        ret.add(totalPktMinLength);
        ret.add(totalPktMeanLength);
        ret.add(totalPktStdLength);
        ret.add((double)fwdPktCount);
        ret.add((double)bwdPktCount);
        ret.add(fwdPktTotalLength);
        ret.add(bwdPktTotalLength);
        ret.add(fwdPktMaxLength);
        ret.add(bwdPktMaxLength);
        ret.add(fwdPktMinLength);
        ret.add(bwdPktMinLength);
        ret.add(fwdPktMeanLength);
        ret.add(bwdPktMeanLength);
        ret.add(fwdPktStdLength);
        ret.add(bwdPktStdLength);
        return ret;
    }

    @Override
    public String toString() {
        return "FlowFeature{" +
                "totalPktMaxLength=" + totalPktMaxLength +
                ", totalPktMinLength=" + totalPktMinLength +
                ", totalPktMeanLength=" + totalPktMeanLength +
                ", totalPktStdLength=" + totalPktStdLength +
                ", fwdPktCount=" + fwdPktCount +
                ", bwdPktCount=" + bwdPktCount +
                ", fwdPktTotalLength=" + fwdPktTotalLength +
                ", bwdPktTotalLength=" + bwdPktTotalLength +
                ", fwdPktMaxLength=" + fwdPktMaxLength +
                ", bwdPktMaxLength=" + bwdPktMaxLength +
                ", fwdPktMinLength=" + fwdPktMinLength +
                ", bwdPktMinLength=" + bwdPktMinLength +
                ", fwdPktMeanLength=" + fwdPktMeanLength +
                ", bwdPktMeanLength=" + bwdPktMeanLength +
                ", fwdPktStdLength=" + fwdPktStdLength +
                ", bwdPktStdLength=" + bwdPktStdLength +
                '}';
    }
}
