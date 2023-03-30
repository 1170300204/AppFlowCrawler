package flow;

public class FLowRelation {
    public int relId;
    public int multiflowId;
    public int flowId1;
    public int flowId2;
    public boolean isPO;
    public boolean POtype;
    public int flowcount1;
    public int flowcount2;

    public FLowRelation() {
    }

    public FLowRelation(int relId, int multiflowId, int flowId1, int flowId2, boolean isPO, boolean POtype, int flowcount1, int flowcount2) {
        this.relId = relId;
        this.multiflowId = multiflowId;
        this.flowId1 = flowId1;
        this.flowId2 = flowId2;
        this.isPO = isPO;
        this.POtype = POtype;
        this.flowcount1 = flowcount1;
        this.flowcount2 = flowcount2;
    }

    public int getRelId() {
        return relId;
    }

    public void setRelId(int relId) {
        this.relId = relId;
    }

    public int getMultiflowId() {
        return multiflowId;
    }

    public void setMultiflowId(int multiflowId) {
        this.multiflowId = multiflowId;
    }

    public int getFlowId1() {
        return flowId1;
    }

    public void setFlowId1(int flowId1) {
        this.flowId1 = flowId1;
    }

    public int getFlowId2() {
        return flowId2;
    }

    public void setFlowId2(int flowId2) {
        this.flowId2 = flowId2;
    }

    public boolean isPO() {
        return isPO;
    }

    public void setPO(boolean PO) {
        isPO = PO;
    }

    public boolean isPOtype() {
        return POtype;
    }

    public void setPOtype(boolean POtype) {
        this.POtype = POtype;
    }

    public int getFlowcount1() {
        return flowcount1;
    }

    public void setFlowcount1(int flowcount1) {
        this.flowcount1 = flowcount1;
    }

    public int getFlowcount2() {
        return flowcount2;
    }

    public void setFlowcount2(int flowcount2) {
        this.flowcount2 = flowcount2;
    }
}
