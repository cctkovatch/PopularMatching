import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import dist.Matcher;
import org.junit.Test;

import generator.PreferenceListGenerator;

public class DistributedTests {

    @Test
    public void test() {
        PreferenceListGenerator x = new PreferenceListGenerator(50,60);
//    	x.writeToFile(x.getPrefList());
        ArrayList<ArrayList<Integer>> pref_list = x.getPrefList();
        x.writeToFile(pref_list);
        int apps = x.getAppCount();
        int posts = x.getPostCount();
        Matcher master = new Matcher(pref_list, apps, posts);
        int[] matching = master.startMatching();
        if (matching == null) {
            System.out.println("No applicant complete matching");
            return;
        }
        ArrayList<Integer> fposts_set = new ArrayList<Integer>();
        int[] fposts = new int[apps];
        int[] sposts = new int[apps];

        for (int i = 0; i <pref_list.size(); i++) {
            ArrayList<Integer> p = pref_list.get(i);
            System.out.println(i+": "+p.toString());
            fposts[i] = p.get(0);
            if (!fposts_set.contains(fposts[i]))
                fposts_set.add(fposts[i]);
        }

        System.out.println(fposts_set.toString());

        for (int i = 0; i <pref_list.size(); i++) {
            ArrayList<Integer> p = pref_list.get(i);
            sposts[i] = -1;
            set_spost:
            for (int k = 1; k < p.size(); k++) {
                Integer sec = p.get(k);
//				System.out.print("checking " + sec + ": ");
                if (!fposts_set.contains(sec)) {
//	    			System.out.println("adding second as "+ sec);
                    sposts[i] = sec;
                    break set_spost;
                }
            }
        }

        System.out.println(Arrays.toString(sposts));
        int num_posts_matched = 0;
        for (int curr_post = 0; curr_post < matching.length; curr_post++) {
            if (matching[curr_post] >= 0)
                num_posts_matched++;
        }
        assertTrue(num_posts_matched == apps);
        for (int curr_post = 0; curr_post < matching.length; curr_post++) {
            int curr_app = matching[curr_post];
            if (curr_app >= 0) {
                String msg = curr_app + " matched to post: " +curr_post+" f:"+fposts[curr_app]+" s:"+sposts[curr_app];
                assertTrue(msg, fposts_set.contains(curr_post) || sposts[curr_app] == curr_post);
            }
        }
    }

}
