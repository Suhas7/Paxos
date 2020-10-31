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
    int n, n_a;
    Object v_a;
    String responseType;
    // Your constructor and methods here
    public Response(String type){
        this.responseType=type;
    }

    public Response(String type, int n_a, Object v_a) {
        this.responseType=type;
        this.v_a=v_a;
        this.n_a=n_a;
    }

    public Response(String type, int p_n, int n_a, Object v_a) {
        this.responseType=type;
        this.v_a = v_a;
        this.n_a =n_a;
        this.n = p_n;
    }

    public boolean equals(String obj) {
        return this.responseType.equals(obj);
    }
}
