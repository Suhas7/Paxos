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
}
