import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import generator.PreferenceListGenerator;
import para.ParallelMatching;
import sequential.SequentialMatching;
public class ParallelTests {

	@Test
	public void test() {
		
		//must have AT LEAST 2 POSTS and therefore at least 2 applicants
		int app_range = 100;
		int jump = 5;
		int post_diff = 10;
		int post_jump = 5;
		
    	PreferenceListGenerator x = new PreferenceListGenerator();
//    	x.writeToFile(x.getPrefList());
    	int a = 2;

    	
    	while (a < app_range) {
    		int p = a; 
    		while (p < a+post_diff) {
    			x.setSize(a, p);
    			ArrayList<ArrayList<Integer>> pref_list = x.getPrefList();
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
    	    		System.out.println("no applicant complete matching");
    	    		// keep trying until there is an app complete matching
    	    		
    	    		
    	    	}
    	    	else {
	    	    	assertTrue(matchingCheck(apps, posts, pref_list, matching));
	    	    	writeTiming(apps, posts, "Sequential", seq_duration);
	    	    	writeTiming(apps, posts, "Parallel", par_duration);
	    	    	p += post_jump;
    	    	}
    			
    		}
    		a += jump;
    	}
    	
    	

	}
	
	public boolean matchingCheck(int apps, int posts, ArrayList<ArrayList<Integer>> pref_list, int[] matching) {
		
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
    	
    	int num_posts_matched = 0;
    	for (int curr_post = 0; curr_post < matching.length; curr_post++) {
    		if (matching[curr_post] >= 0)
    			num_posts_matched++;
    	}
    	if (num_posts_matched != apps)
    		return false;
    	for (int curr_post = 0; curr_post < matching.length; curr_post++) {
    		int curr_app = matching[curr_post];
    		if (curr_app >= 0) {
    		String msg = curr_app + " matched to post: " +curr_post+" f:"+fposts[curr_app]+" s:"+sposts[curr_app];
    		if (!(fposts_set.contains(curr_post) || sposts[curr_app] == curr_post))
    			return false;
//    		assertTrue(msg, fposts_set.contains(curr_post) || sposts[curr_app] == curr_post);
    		}
    	}
		
		
		return true;
	}
	public void writeTiming(int apps, int posts, String type, long duration) {
		
		String fileName = type;
		String line = apps + " " + posts + " " + duration +"\n";

	    PrintWriter printWriter = null;
	    File file = new File(fileName);
	    try {
	        if (!file.exists()) file.createNewFile();
	        printWriter = new PrintWriter(new FileOutputStream(fileName, true));
	        printWriter.write(line);
	    } catch (IOException ioex) {
	        ioex.printStackTrace();
	    } finally {
	        if (printWriter != null) {
	            printWriter.flush();
	            printWriter.close();
	        }
	    }
		
	}


}
