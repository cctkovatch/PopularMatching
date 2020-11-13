package dist;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Matcher implements MatchingRMI{
    String[] peers; // hostname
    int[] ports; // host port
    int me = -1; // index into peers[]

    boolean master = false; // Indicates whether or not this is the master node
    Matcher[] slaves;
    int apps; // Total post count
    int posts; // Total app count
    ArrayList<ArrayList<Integer>> reducedGraph;

    Registry registry;
    MatchingRMI stub;


    /**
     * Call to create the master Matcher node.
     * The master Matcher node will generate the slave nodes when necessary.
     */
    public Matcher(ArrayList<ArrayList<Integer>> reducedGraph, int apps, int posts){
        this.apps = apps;
        this.posts = posts;
        this.reducedGraph = reducedGraph;

        String host = "127.0.0.1";
        peers = new String[posts]; // Need at most 1 slave per post to ensure work can be fully distributed
        ports = new int[posts];
        slaves = new Matcher[posts];

        for(int i = 0; i < posts; i++){
            ports[i] = 1100 + i;
            peers[i] = host;
        }

        for(int i = 0; i < posts; i++){
            slaves[i] = new Matcher(i, peers, ports, reducedGraph, apps, posts);
        }

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (MatchingRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Matching", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Called to create a Matcher slave.
     * The hostnames of all slaves (including this one)
     * are in peers[]. The ports are in ports[].
     */
    private Matcher(int me, String[] peers, int[] ports, ArrayList<ArrayList<Integer>> reducedGraph, int apps, int posts){

        this.me = me;
        this.peers = peers;
        this.ports = ports;

        this.reducedGraph = reducedGraph;
        this.apps = apps;
        this.posts = posts;


        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (MatchingRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Matching", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void StartMatching(){

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

        MatchingRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(MatchingRMI) registry.lookup("Matching");
            switch (rmi) {
                case "matchAC":
                    callReply = stub.matchAC(req);
                    break;
                case "promoteAC":
                    callReply = stub.promoteAC(req);
                    break;
                default:
                    System.out.println("Wrong parameters!");
                    break;
            }
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    @Override
    public Response matchAC(Request req) throws RemoteException {
        Response response = new Response();
        // TODO: Implement applicant-complete match generation for provided post
        return response;
    }

    @Override
    public Response promoteAC(Request req) throws RemoteException {
        Response response = new Response();
        // TODO: Implement post promotion for provided post
        return response;
    }


    /**
     *  This method is used to generate an applicant-complete matching or determine if one does not exist.
     *  This applicant-complete matching can then be manipulated into a popular matching.
     */
    private ArrayList<Pair<Integer>> generateACMatching(ArrayList<ArrayList<Integer>> reducedGraph){
        ArrayList<Pair<Integer>> matching = new ArrayList<>();

        int degreeOneCount = 0;
        int[] degrees = getPostDeg(reducedGraph, posts);

        /* Count the number of degree 1 posts. This is how many slave nodes we must invoke. */
        for(int i = 0; i < ){
            if(i == 1){
                degreeOneCount++;
            }
        }



        int appCount = 0;
        int postCount = 0;

        // TODO: Count available posts and applicants after all degree 1 pairings are made.

        if(postCount >= appCount){ // An applicant complete matching can be generated
            // TODO: Select a perfect matching of G'
            return matching;
        } else { // An applicant complete matching cannot be generated
            return null;
        }
    }

    private int[][] getAppList(ArrayList<ArrayList<Integer>> prefList, int apps, int posts){
        int[][] app_list =  new int[apps][posts];
        for (int i = 0; i < prefList.size(); i++) {
            for (int k = 0; k < prefList.get(i).size(); k++) {
                app_list[i][prefList.get(i).get(k)] = 1;
            }
        }
        return app_list;
    }

    private int[][] getPostList(ArrayList<ArrayList<Integer>> prefList, int apps, int posts){
        int[][] post_list =  new int[posts][apps];
        for (int i = 0; i < prefList.size(); i++) {
            for (int k = 0; k < prefList.get(i).size(); k++) {
                post_list[prefList.get(i).get(k)][i] = 1;
            }
        }
        return post_list;
    }

    private int[] getAppDeg(ArrayList<ArrayList<Integer>> prefList, int apps){
        int[] app_list =  new int[apps];
        for (int i = 0; i < prefList.size(); i++) {
            for (int k = 0; k < prefList.get(i).size(); k++) {
                app_list[i] ++;
            }
        }
        return app_list;
    }

    private int[] getPostDeg(ArrayList<ArrayList<Integer>> prefList, int posts){
        int[] post_list =  new int[posts];
        for (ArrayList<Integer> integers : prefList) {
            for (Integer integer : integers) {
                post_list[integer]++;
            }
        }
        return post_list;
    }
}
