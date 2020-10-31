package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    // Your data here
    public String type;
    public int seq;
    public int n_a;
    public int v_a;
    // Your constructor and methods here
    public Request(int seq, int n_a, int val){
        this.type = "Accept";
        this.seq = seq;
        this.n_a = n_a;
        this.v_a = val;
    }
    public Request(int seq, int n_a){
        this.type = "Prepare";
        this.seq = seq;
        this.n_a = n_a;
    }
}
