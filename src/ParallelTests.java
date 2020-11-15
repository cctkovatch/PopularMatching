import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import generator.PreferenceListGenerator;
import para.ParallelMatching;
import sequential.SequentialMatching;
public class ParallelTests {

	@Test
	public void test() {
    	PreferenceListGenerator x = new PreferenceListGenerator(12,14);
//    	x.writeToFile(x.getPrefList());
    	ArrayList<ArrayList<Integer>> pref_list = x.getPrefList();
    	x.writeToFile(pref_list);
    	int apps = x.getAppCount();
    	int posts = x.getPostCount();
    	long startTime = System.nanoTime();
    	int[] seq_match = SequentialMatching.promotedMatch(pref_list, apps, posts);
    	long endTime = System.nanoTime();

    	long seq_duration = (endTime - startTime);
    	
    	startTime = System.nanoTime();
    	int[] matching = ParallelMatching.promotedMatch(pref_list, apps, posts);
    	endTime = System.nanoTime();

    	long par_duration = (endTime - startTime);
    	System.out.println("seq match: " + Arrays.toString(seq_match) + " " + seq_duration);
    	System.out.println("par match: " + Arrays.toString(matching) + " " + par_duration);

    	if (matching == null) {
    		assertTrue("no applicant complete matching", false);
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
