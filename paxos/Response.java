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
    Object v_a;
    int n_p, n;
    Object data;
    String responseType;
    // Your constructor and methods here
    public Response(String type, Object data){
        this.responseType=type;
        this.data=data;
    }

    public Response(String ok, int val, int accepted, Object value) {
        this.responseType=ok;
        this.n=val;
        this.n_p=accepted;
        this.v_a=value;
    }

    public Response(String type, int n_a, Object v_a) {
        this.responseType=type;
    }

    public boolean equals(String obj) {
        return this.responseType.equals(obj);
    }
}
