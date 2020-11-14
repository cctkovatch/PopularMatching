package dist;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class Response implements Serializable {
    static final long serialVersionUID=2L;
    ArrayList<Pair<Integer, Integer>> matchings;
    boolean oneToOne;
    int startPost;
    int endPost;

    public Response(ArrayList<Pair<Integer, Integer>> matchings, boolean oneToOne, int startPost, int endPost){
        this.startPost = startPost;
        this.matchings = matchings;
        this.oneToOne = oneToOne;
        this.endPost = endPost;
    }
}
