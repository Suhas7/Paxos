package paxos;

public class Agreement {
    int proposal;
    int n_p;
    int n_a;
    Object v_a;
    boolean complete;
    public Agreement(int me, Object v_a){
        this.v_a = v_a;
        n_a = n_p  = 0;
        this.proposal = me;
        complete = false;
    }
    public Agreement(int n_p, int n_a, Object v_a, int prop){
        this.n_p = n_p;
        this.n_a = n_a;
        this.v_a = v_a;
        this.proposal = prop;
        this.complete = false;
    }

    public Agreement(boolean complete, int val){
        this.complete=complete;
        this.v_a =(Integer) val;
    }
}
