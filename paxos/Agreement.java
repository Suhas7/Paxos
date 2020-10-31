package paxos;

public class Agreement {
    int proposal;
    int seen;
    int accepted;
    Object value;
    boolean complete;
    public Agreement(int me, Object value){
        this.value=value;
        accepted=seen=0;
        this.proposal=me;
        complete=false;
    }
    public Agreement(int me, int prop, int val) {
        this.value=(Integer) value;
        accepted=seen=0;
        this.proposal=me;
        complete=false;
    }
    public Agreement(boolean complete, int val){
        this.complete=complete;
        this.value=(Integer) val;
    }
}
