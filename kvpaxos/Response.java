package kvpaxos;

import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=22L;
    // your data here
    boolean success;
    Serializable value;
    // Your constructor and methods here
    public Response(){this.success=false;}
    public Response(Serializable val){this.value=val;this.success=true;}
}
