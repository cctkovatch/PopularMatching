package dist;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.util.Pair;

public class Matcher implements MatchingRMI{
    String[] peers; // hostname
    int[] ports; // host port
    int me = -1; // index into peers[]

    boolean master = false; // Indicates whether or not this is the master node
    Matcher[] slaves;
    int apps; // Total post count
    int posts; // Total app count
    ArrayList<ArrayList<Integer>> reducedGraph;
    ArrayList<Response> threadResponses;
    int responseCount = 0;

    Registry registry;
    MatchingRMI stub;


    /**
     * Call to create the master Matcher node.
     * The master Matcher node will generate the slave nodes when necessary.
     */
    public Matcher(ArrayList<ArrayList<Integer>> prefList, int apps, int posts){
        this.apps = apps;
        this.posts = posts;

        reducedGraph = getReducedGraph(prefList);
        threadResponses = new ArrayList<>();

        for(int i = 0; i < posts; i++){
            threadResponses.add(null);
        }

        String host = "127.0.0.1";
        peers = new String[posts]; // Need at most 1 slave per post to ensure work can be fully distributed
        ports = new int[posts];
        slaves = new Matcher[posts];

        for(int i = 0; i < posts; i++){ // TODO: Rework this so we don't have a slave for every post, doesn't scale well at all.
            ports[i] = 1100 + i;
            peers[i] = host;
        }

        for(int i = 0; i < posts; i++){
            slaves[i] = new Matcher(i, peers, ports, reducedGraph, apps, posts);
        }

        // register peers, do not modify this part
//        try{
//            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
//            registry = LocateRegistry.createRegistry(this.ports[this.me]);
//            stub = (MatchingRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
//            registry.rebind("Matching", stub);
//        } catch(Exception e){
//            e.printStackTrace();
//        }
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

    public int[] startMatching(){
        ArrayList<Pair<Integer, Integer>> matchingAC = generateACMatching();
        ArrayList<Pair<Integer, Integer>> popularMatching = generatePopularMatching(matchingAC);

        if(popularMatching == null){
            System.out.println("No popular matching exists");

            /* Master node and slave nodes cleanup */
            // TODO: Cleanup nodes

            return null;
        } else {
            System.out.println("A popular matching exists");
            for(Pair<Integer, Integer> match : popularMatching){
                System.out.println("Applicant " + match.getKey() + " pairs with post " + match.getValue());
            }

            int[] compressedMatching = new int[posts];

            for(int i = 0; i < posts; i++){
                compressedMatching[i] = -1;
            }

            for(Pair<Integer, Integer> match : popularMatching){
                compressedMatching[match.getValue()] = match.getKey();
            }

            /* Master node and slave nodes cleanup */
            // TODO: Cleanup nodes

            return compressedMatching;
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
            e.printStackTrace();
            return null;
        }
        return callReply;
    }

    @Override
    public Response matchAC(Request req) throws RemoteException {
        Response response;
        ArrayList<Pair<Integer, Integer>> matchings = new ArrayList<>();

        int[][] postList = req.postList;
        int currPost = req.post;
        boolean oneToOne = false;

        /* Keep going until we are no longer creating degree 1 posts by matching degree 1 posts */
        while(req.postDegrees[currPost] == 1){
            for(int i = 0; i < apps; i++){
                if(postList[currPost][i] == 1){ // We found the app it needs to be paired with
                    matchings.add(new Pair<>(i, currPost));
                    req.postDegrees[currPost] = 0;
                    postList[currPost][i] = 0; // Remove the edge

                    /* Navigate to the next post, update its degree */
                    for(int j = 0; j < posts; j++){
                        if(postList[j][i] == 1){ // We found the other post our matched app is connected to
                            postList[j][i] = 0; // Remove the edge
                            currPost = j;
                            req.postDegrees[currPost]--; // Decrement degree
                            i = apps; // Need to break out of the upper for loop
                            break;
                        }
                    }
                }
            }
        }

        /* If there is a path between two degree one nodes through degree two nodes,
         we can only accept one of their matching sets. This allows us to discard the
         second slave node's matching. */
        if(req.postDegrees[currPost] == 0) oneToOne = true;

        response = new Response(matchings, oneToOne, req.post, currPost);

        return response;
    }

    @Override
    public Response promoteAC(Request req) throws RemoteException {
        Response response;

        int fPost = req.post;

        /* Find the first applicant we can promote, and promote them */

        for(int i = 0; i < reducedGraph.size(); i++){
            if(reducedGraph.get(i).get(0).equals(fPost)){
                Pair<Integer, Integer> promotion = new Pair<>(i, fPost);
                ArrayList<Pair<Integer, Integer>> matching = new ArrayList<>();
                matching.add(promotion);
                response = new Response(matching, false, req.post, -1);
                return response;
            }
        }

        ArrayList<Pair<Integer, Integer>> matching = new ArrayList<>();

        response = new Response(matching, false, req.post, -1);

        return response;
    }


    /**
     *  This method is used to generate an applicant-complete matching or determine if one does not exist.
     *  This applicant-complete matching can then be manipulated into a popular matching.
     */
    private ArrayList<Pair<Integer, Integer>> generateACMatching(){
        ArrayList<Pair<Integer, Integer>> matching = new ArrayList<>();

        int degreeOneCount = 0;
        int[] degrees = getPostDeg(reducedGraph, posts);
        int[][] postList = getPostList(reducedGraph, apps, posts);

        for(int i = 0; i < posts; i++){
            for(int j = 0; j < apps; j++){
                System.out.print(postList[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        /* Count the number of degree 1 posts. This is how many slave nodes we must invoke. */
        for(int i = 0; i < posts; i++){
            if(degrees[i] == 1){
                degreeOneCount++;
            }
        }

        /* Request a slave to process matching for a given degree 1 post */
        // TODO: Rework this to generate a new thread for each degree 1 post so messaging can happen in parallel.
        int orphanedPostCount = 0;
        HashSet<Integer> oneToOneEndPosts = new HashSet<>();
        while(degreeOneCount > 0){
            for(int i = 0; i < posts; i++){
                if(degrees[i] == 1){
                    Request request = new Request(i, degrees, postList);
                    Response response = Call("matchAC", request, i);
                    System.out.println(response);
                    threadResponses.set(i, response);
                    responseCount++;
                }
            }

            //while(responseCount < degreeOneCount); // Necessary when using threads for parallelized messaging.

            for(Response r : threadResponses){
                if(r != null){
                    if(r.oneToOne && oneToOneEndPosts.contains(r.startPost)){ // We have a conflicting matching set, so only keep the first one we saw.
                        System.out.println("Start: " + r.startPost + " End: " + r.endPost);
                        degrees[r.startPost] = 0;
                        continue;
                    } else if(r.oneToOne){
                        oneToOneEndPosts.add(r.endPost);
                    }
                    for(Pair<Integer, Integer> match : r.matchings){
                        if(postList[match.getValue()][match.getKey()] == 1) { // Check if matching hasn't been made yet
                            matching.add(match);
                            for(int i = 0; i < posts; i++){
                                if(postList[i][match.getKey()] == 1){
                                    postList[i][match.getKey()] = 0;
                                    degrees[i] -= 1;

                                    /* If a matching decreases the degree of a different post to 0, that post is orphaned */
                                    if(degrees[i] == 0 && i != match.getValue()){
                                        orphanedPostCount++;
                                    }
                                }
                            }
                            for(int i = 0; i < apps; i++){
                                postList[match.getValue()][i] = 0;
                            }
                        }
                    }
                } else {
                    System.out.println("Null response!");
                }
            }
            degreeOneCount = 0;
            for(int i : degrees){
                if(i == 1){
                    degreeOneCount++;
                }
            }
        }



        for(int i = 0; i < posts; i++){
            for(int j = 0; j < apps; j++){
                System.out.print(postList[i][j] + " ");
            }
            System.out.println();
        }

        int appCount = apps - matching.size();
        int degreeZeroCount = 0;
        for(int i : degrees){
            if(i == 0){
                degreeZeroCount++;
            }
        }

        int postCount = posts - degreeZeroCount; //matching.size() - orphanedPostCount;

        if(postCount >= appCount){ // An applicant complete matching can be generated
            int app = 0, post = 0;
            System.out.println("Generating applicant complete matching between " + postCount + " posts and " + appCount + " apps");
            while(appCount > 0){
                if(postList[post][app] == 1){
                    matching.add(new Pair<>(app, post));
                    degrees[post] = 0;
                    appCount--;
                    postCount--;
                    for(int i = 0; i < apps; i++){ // This post is matched, remove all other edges
                        postList[post][i] = 0;
                    }
                    for(int i = 0; i < posts; i++){
                        if(postList[i][app] == 1){ // Follow the cycle path
                            postList[i][app] = 0;
                            post = i;
                            app = 0; // Need to go back to zero because it is possible for a cycle to go "backwards"
                            break;
                        }
                    }
                } else {
                    app++;
                    if(app == apps){
                        app = 0;
                        post++;
                        if(post == posts) post = 0;
                    }
                }
            }

            return matching;
        } else { // An applicant complete matching cannot be generated
            return null;
        }
    }

    private ArrayList<Pair<Integer, Integer>> generatePopularMatching(ArrayList<Pair<Integer, Integer>> matchingAC){
        /* In the case that an applicant-complete matching could not be made */
        if(matchingAC == null){
            return null;
        }

        HashSet<Integer> fPosts = new HashSet<>();

        /* Need to see which f-posts are unmatched */
        for(ArrayList<Integer> posts : reducedGraph){
            fPosts.add(posts.get(0)); // Collect all f-posts
        }

        for(Pair<Integer, Integer> match : matchingAC){
            fPosts.remove(match.getValue()); // Removes all matched f-posts
        }

        ArrayList<Response> responses = new ArrayList<>();

        /* Need to promote these unmatched fPosts */
        for(Integer fPost : fPosts){
            Request request = new Request(fPost, null, null);
            responses.add(Call("promoteAC", request, fPost));
        }

        int nullCount = 0;
        for(Response response : responses){
            if(response == null) nullCount++;
        }

        System.out.println("Null Count: " + nullCount);

        HashSet<Integer> promotedApps = new HashSet<>();
        for(Response response : responses){
            if(response == null){
                System.out.println("Null response????");
            } else if(response.matchings == null){
                System.out.println("Null matchings array");
            } else if(response.matchings.size() == 0){
                System.out.println("Empty matchings array");
            }
            promotedApps.add(response.matchings.get(0).getKey());
        }

        ArrayList<Pair<Integer, Integer>> toRemove = new ArrayList<>();
        for(Pair<Integer, Integer> match : matchingAC){
            if(promotedApps.contains(match.getKey())){ // Need to promote in this case
                toRemove.add(match);
            }
        }

        matchingAC.removeAll(toRemove); // Remove all matches that will be promoted.

        for(Response response : responses){
            matchingAC.add(response.matchings.get(0)); // Adds the promotions to the matching
        }

        /* Matching is now popular */
        return matchingAC;
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
            for (Integer post : integers) {
                post_list[post]++;
            }
        }
        return post_list;
    }

    private static ArrayList<ArrayList<Integer>> getReducedGraph(ArrayList<ArrayList<Integer>> prefList) {
        ArrayList<ArrayList<Integer>> reducedG = new ArrayList<>();
        ArrayList<Integer> fPosts = new ArrayList<>();

        for (ArrayList<Integer> integers : prefList) {
            fPosts.add(integers.get(0));
        }
        for (int p = 0; p < prefList.size(); p++) {
            reducedG.add(new ArrayList<>());
            ArrayList<Integer> lst = prefList.get(p);
            reducedG.get(p).add(lst.get(0));

            for (Integer i:lst) {
                if (!fPosts.contains(i)) {
                    reducedG.get(p).add(i);
                    break;
                }
            }
        }
        return reducedG;
    }
}
