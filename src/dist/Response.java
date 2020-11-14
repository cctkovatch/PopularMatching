package dist;
import java.io.Serializable;
import java.util.ArrayList;

public class Response implements Serializable {
    static final long serialVersionUID=2L;
    ArrayList<Pair<Integer>> matchings;

    public Response(ArrayList<Pair<Integer>> matchings){
        this.matchings = matchings;
    }
}
