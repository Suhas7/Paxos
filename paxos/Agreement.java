package paxos;

import java.io.Serializable;

public class Agreement {
    int n_p;
    int n_a;
    Serializable v_a;
    State state;

    public Agreement(){
        this.n_p = -1;
        this.n_a = -1;
        this.v_a = null;
        this.state = State.Pending;
    }

}
