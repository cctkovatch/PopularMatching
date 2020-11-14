package dist;
import java.io.Serializable;
import java.util.ArrayList;

public class Response implements Serializable {
    static final long serialVersionUID=2L;
    ArrayList<Pair<Integer>> matchings;
    boolean oneToOne;
    int endPost;

    public Response(ArrayList<Pair<Integer>> matchings, boolean oneToOne, int endPost){
        this.matchings = matchings;
        this.oneToOne = oneToOne;
        this.endPost = endPost;
    }
}
