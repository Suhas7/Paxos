package paxos;

import java.io.Serializable;

public class Agreement {
    int n_p;
    int n_a;
    Serializable v_a;
    boolean complete;
    public Agreement(int n, Serializable v_a){
        this.v_a = v_a;
        n_a = n_p  = n;
        complete = false;
    }
    public Agreement(int n_p, int n_a, Serializable v_a){
        this.n_p = n_p;
        this.n_a = n_a;
        this.v_a = v_a;
        this.complete = false;
    }

    public Agreement(boolean complete, Serializable val){
        this.complete=complete;
        this.v_a = val;
    }
}
