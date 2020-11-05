package kvpaxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Client {
    String[] servers;
    int[] ports;
    // Your data here
    private int seq;

    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
        // Your initialization code here
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
        KVPaxosRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(KVPaxosRMI) registry.lookup("KVPaxos");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Put")){
                callReply = stub.Put(req);}
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    // RMI handlers
    public Integer Get(String key){
        Response goodResponse = new Response();
        Request myReq = new Request(new Op("Get",this.seq++,key,null));
        for(int i = 0; i<this.ports.length; i++){
            Response currResponse = Call("Get",myReq,i);
            if(currResponse!=null && currResponse.success){
                goodResponse=currResponse;
                return (Integer) goodResponse.value;
            }
        }
        return 42;
    }

    public boolean Put(String key, Integer value){
        Request myReq = new Request(new Op("Put",this.seq++,key,value));
        for(int i = 0; i<this.ports.length; i++){
            Response currResponse = Call("Put",myReq,i);
            if(currResponse!=null && currResponse.success){
                return true;
            }
        }
        return false;
    }

}
