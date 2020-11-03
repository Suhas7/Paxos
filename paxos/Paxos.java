package paxos;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static paxos.State.*;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{
    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]
    Registry registry;
    PaxosRMI stub;
    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing
    // Your data here
    private HashMap<Integer, Agreement> agreements;
    private List<Integer> doneStamps;
    private int proposalNum;
    private int seq;
    private Serializable val;

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){
        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);
        // Your initialization code here
        this.agreements = new HashMap<>();
        this.doneStamps = new ArrayList<>(Arrays.asList(new Integer[peers.length]));
        this.proposalNum = this.me - this.peers.length;
        Collections.fill(this.doneStamps,-1);
        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Serializable value){
        if(!this.agreements.containsKey(seq)) this.agreements.put(seq,new Agreement(0,value));
        else this.agreements.put(seq, new Agreement(this.agreements.get(seq).n_a,value));
        //temporarily store vars pass to instantiated thread
        this.seq=seq;
        this.val=value;
        new Thread(this).start();
    }

    @Override
    public void run(){
        int seq = this.seq;
        Serializable val = this.val;
        while(!this.isDead()){
            // get unique proposal id
            int n = this.proposalNum + this.peers.length;
            this.proposalNum = this.proposalNum + this.peers.length;

            final int majority = 1 + this.peers.length / 2;

            // start counter for promise messages
            int count = 1;
            for(int port = 0; port < this.ports.length; port++){
                Response x;
                if(port!=this.me) x = Call("Prepare", new Request(seq, n), port);
                else x = this.Prepare(new Request(seq, n));
                if(x!=null && x.responseType.equals("Ok")){
                    count++;
                    if(x.n > n){
                        n = x.n;
                        val = x.v_a;
                    }
                }
            }
            // no paxos iteration if promise messages is not priority
            if(count < majority) continue;

            // start counter for accept messages
            count = 1;
            for(int port = 0; port < this.ports.length; port++) {
                Response x;
                if(port != this.me) x = Call("Accept", new Request("Accept", seq, n, val), port);
                else x = this.Accept(new Request("Accept", seq, n, val));
                if (x != null && x.responseType.equals("AcceptOk")) count++;
            }


            if(count >= majority){
                Agreement ag = this.agreements.get(seq);
                ag.complete=true;
                ag.v_a = val;
                Response z = null;
                for (int i = 0; i < this.peers.length; i++) {
                    if(i != this.me) z = Call("Decide", new Request("Decide",seq, n, val), i);
                    else z = this.Decide(new Request("Decide",seq, n, val));
                }
                return;
            }
        }
    }

    // RMI handler
    public Response Prepare(Request req){
        assert req.type.equals("Prepare");
        this.mutex.lock();
        if(!this.agreements.containsKey(req.seq)){
            //no prepare with this seq has been seen
            this.agreements.put(req.seq, new Agreement(req.p_n,req.p_n,req.v_a));
            Agreement x = this.agreements.get(req.seq);
            this.mutex.unlock();
            return new Response("Ok", req.p_n,x.n_a,x.v_a);
        }
        if(this.agreements.get(req.seq).n_p < req.p_n){
            //prepare better than previous prepare todo are the args right
            this.agreements.put(req.seq,new Agreement(req.p_n,req.p_n,req.v_a));
            Agreement x = this.agreements.get(req.seq);
            this.mutex.unlock();
            return new Response("Ok",req.p_n,x.n_a,x.v_a);
        }else{
            //prepare failed, prop value too low
            this.mutex.unlock();
            return new Response("Reject");
        }
    }

    public Response Accept(Request req){
        assert req.type.equals("Accept");
        this.mutex.lock();
        if(!this.agreements.containsKey(req.seq))
            this.agreements.put(req.seq,new Agreement(req.p_n, req.p_n,req.v_a));
        Agreement x = this.agreements.get(req.seq);
        if (req.p_n >=x.n_p){
            x.n_a = req.p_n;
            x.v_a = req.v_a;
            x.complete = false;
            this.mutex.unlock();
            return new Response("AcceptOk", x.n_a, x.v_a);
        }else{
            this.mutex.unlock();
            return new Response("AcceptReject");
        }
    }

    public Response Decide(Request req){
        assert req.type.equals("Decide");
        this.mutex.lock();
        //if(!this.agreements.containsKey(req.seq)){
            //make agreement w known value and mark as final
        //    this.agreements.put(req.seq,new Agreement(true,req.v_a));
        //}else{
            //mark according entry as final and update value
            this.agreements.get(req.seq).complete=true;
            this.agreements.get(req.seq).v_a =req.v_a;
        //}
        this.mutex.unlock();
        return new Response("Done", this.agreements.get(req.seq).n_a, this.agreements.get(req.seq).v_a);
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     * see the comments for Min() for more explanation.
     *
     * "Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to."
     */
    public void Done(int seq) {
        if(seq>this.doneStamps.get(this.me)) {
            this.doneStamps.set(this.me, seq);
//            this.Min();
        }
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){ return Collections.max(this.agreements.keySet()); }

    /**
     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        this.mutex.lock();
        /**
         * Min() should return one more than the minimum among z_i,
         * where z_i is the highest number ever passed
         * to Done() on peer i. A peers z_i is -1 if it has
         * never called Done().
         */
        int min = Collections.min(this.doneStamps);

        if (min == 0) min = -1;

        /**
         * Paxos is required to have forgotten all information
         * about any instances it knows that are < Min().
         * The point is to free up memory in long-running
         * Paxos-based servers.
         */
        //todo verify
        for(Map.Entry<Integer,Agreement> entry : this.agreements.entrySet())
            if(entry.getKey()<min && entry.getValue().complete)
                this.agreements.remove(entry.getKey());

        this.mutex.unlock();
        return min+1;
    }

    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        //if(seq<this.Min()) return new retStatus(Forgotten,null);
        this.mutex.lock();
        boolean decided;
        if(!this.agreements.containsKey(seq)) decided = false;
        else decided = this.agreements.get(seq).complete;
        this.mutex.unlock();
        return decided ? new retStatus(Decided,this.agreements.get(seq).v_a) : new retStatus(Pending,null);
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;
        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }
    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }
    public boolean isunreliable(){
        return this.unreliable.get();
    }
}
