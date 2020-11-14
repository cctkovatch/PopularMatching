package generator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Generates a random set of preference lists for a apps and p posts. 
 * Each applicant has at most k posts in its preference list, with k <= p and chosen randomly.
 * Write to file will write the preference list to file in the format of original test preference list.
 * Example usage in par.Main
 */

public class PreferenceListGenerator {
	private int posts;
	private int apps;
	ArrayList<ArrayList<Integer>> pref_list;
	
	public PreferenceListGenerator(int apps, int posts) {
		this.posts = posts;
		this.apps = apps;
		pref_list = new ArrayList<ArrayList<Integer>>(apps);
	}
	public void setSize(int posts, int apps) {
		this.posts = posts;
		this.apps = apps;
	
	}
	
	public ArrayList<ArrayList<Integer>> getPrefList() {
		pref_list.clear();
		for (int i = 0; i < apps; i++) {
			int num_acceptable = ThreadLocalRandom.current().nextInt(2, posts + 1);
			// each applicant has random number of acceptable edges
			ArrayList<Integer> ranking = new ArrayList<Integer>();
			for (int k = 0; k < posts; k++) {
				ranking.add(k);
			}
			shuffleArray(ranking);
			while(ranking.size() > num_acceptable) {
				ranking.remove(ranking.size()-1);
			}
			pref_list.add(ranking);
		}
		return pref_list;
	}
	public int getAppCount() {
		return apps;
	}
	public int getPostCount() {
		return posts;
	}
	public void writeToFile(ArrayList<ArrayList<Integer>> p_list) {
		FileWriter myWriter;
		try {
			String title = "pref_list_"+posts+"_posts_"+apps+"_apps";
			myWriter = new FileWriter(title);
			String line = "";
			line += apps + " " + posts + "\n";
			for (int i = 0; i < p_list.size(); i++) {
				for(int k = 0; k < p_list.get(i).size()-1; k++) {
					line += p_list.get(i).get(k) + ",";
				}
				line += p_list.get(i).get(p_list.get(i).size()-1) + "\n";
			}
			
		    myWriter.write(line);
		    myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void shuffleArray(ArrayList<Integer> ar){
	    Random rnd = ThreadLocalRandom.current();
	    for (int i = ar.size() - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar.get(index);
	      ar.set(index, ar.get(i));
	      ar.set(i, a);
	    }
	}

}
