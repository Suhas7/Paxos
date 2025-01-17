package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    public int n_a;
    public Serializable v_a;
    public String responseType;
    public int latestDone;

    // Your constructor and methods here
    public Response(String type){
        this.responseType=type;
    }

    public Response(String type, int n_a, Serializable v_a, int done) {
        this.responseType = type;
        this.v_a = v_a;
        this.n_a = n_a;
        this.latestDone = done;
    }

    public boolean equals(String obj) {
        return this.responseType.equals(obj);
    }
}
