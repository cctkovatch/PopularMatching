package dist;
import java.io.Serializable;

public class Request implements Serializable {
    static final long serialVersionUID=1L;
    int post;
    int[] postDegrees;
    int[][] postList;

    public Request(int post, int[] postDegrees, int[][] postList) {
        this.post = post;
        this.postDegrees = postDegrees;
        this.postList = postList;
    }
}
