package sequential;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import generator.PreferenceListGenerator;

public class SequentialMatching {
	
    public static void main(String[] args) throws Exception {
    	
    	int apps = 0;
    	int posts = 0;
    	String inputFile = args[0];
    	Scanner sc = new Scanner(new File(inputFile));
        String[] inputSizes = sc.nextLine().split(" ");
    	apps = Integer.parseInt(inputSizes[0]);
    	posts = Integer.parseInt(inputSizes[1]);
    	
    	ArrayList<ArrayList<Integer>> pref_list = parsePrefList(args, sc, apps, posts);
    	promotedMatch(pref_list, apps, posts);

    }
    
    public static int[] promotedMatch(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts) {

    	ArrayList<ArrayList<Integer>> red_G = getReducedGraph(pref_list);

    	int[] app1_fpost = new int[posts];		
    	int[] is_fpost = new int[posts];
    	getFPosts(is_fpost, app1_fpost, red_G, posts); 
    	int[] matching_apps = new int[apps];
    	int[] matching = new int[posts];
    	if (!match(matching, matching_apps, red_G, apps, posts)) {
    		System.out.println("null list");
    		return null;
    	}
    	System.out.println("matched" + Arrays.toString(matching));
    	System.out.println("matched" + Arrays.toString(matching_apps));

    	for(int i = 0; i < matching.length; i++) {

	    	  if (matching[i] == -1 && is_fpost[i] == 1) {
	    		  int promote_app = app1_fpost[i];
		    	  int curr_matched_post = matching_apps[promote_app];
		    	  matching[curr_matched_post] = -1; 
		    	  matching[i] = promote_app;
		    	  matching_apps[promote_app] = i;
	    	  }
    		
    	}
    	return matching;
    
    }
    
    public static boolean match(int[] matching, int[] matching_apps, ArrayList<ArrayList<Integer>> red_G, int apps, int posts) {
    	
    	int[][] post_list = getPostList(red_G, apps, posts);
    	int[][] app_list = getAppList(red_G, apps, posts);


    	int[] deg_apps = getAppDeg(red_G, apps, posts);
    	int[] deg_posts = getPostDeg(red_G, apps, posts);
		Arrays.setAll(matching_apps,  e-> {return -1;});
		Arrays.setAll(matching,  e-> {return -1;});

    	int deg1_posts_count = 0;
    	for (int i = 0; i < deg_posts.length; i++) {
    		if (deg_posts[i] == 1) {
    			deg1_posts_count++;
    		}
    	}    	

    	
    	int deg1_indexer = 0;
		int num_match = 0;		


    	while (deg1_posts_count > 0) {
    		int p = -1;
    		int[] even_posts = new int[posts]; 
    		int[] even_apps = new int[apps]; 
    		Arrays.setAll(even_apps,  e-> {return -1;});
    		Arrays.setAll(even_posts,  e-> {return -1;});
			//these are the posts-app edges that are even dist away from p (including p), to be added in parallel
    		
    		while(p == -1) {
    			if (deg_posts[deg1_indexer] == 1) {
    				p = deg1_indexer;
//    				System.out.println("next deg 1 post is" + p);
    			}
    			deg1_indexer++;
    			if (deg1_indexer >= posts)
    				deg1_indexer %= posts;
    		
    		}

			int[] list = post_list[p];
			boolean has_next_path = true;
			int a = 0;
	
//			System.out.println(Arrays.toString(deg_posts) + " degrees" + num_match);
			//finding all edges an even distance away from p
			while (has_next_path) {
				if(matching_apps[a] == -1 && even_apps[a] == -1 && list[a] == 1) {
					even_posts[p] = a;
					even_apps[a] = p;
					num_match++;
					has_next_path = false;
//					System.out.println("adding post " + p + " with app " + a);
					find_next_post:
					for (int i = 0; i < app_list[a].length; i++) {
						if (i != p && app_list[a][i] == 1) { 	//find the p's attached to app that have degree 2
							if (deg_posts[i] == 2 && even_posts[i] == -1) {
//								System.out.println("setting next post for " + a + " as post: " + i);
								list = post_list[i];
								has_next_path = true;
								a = -1;
								p = i;
								break find_next_post;
							}
						}
						
					}
				}
				a++;
			
			}
//			System.out.println(Arrays.toString(even_posts) + " posts found" + num_match);
//			System.out.println(Arrays.toString(even_apps) + " apps found" + num_match);
			//matching and degree posts is parallel set
			for(int e = 0; e < matching.length; e++) {
				if (even_posts[e] != -1) 
					matching[e] = even_posts[e];
			}
			for(int e = 0; e < matching_apps.length; e++) {
				if (even_apps[e] != -1) 
					matching_apps[e] = even_apps[e];
			}
			deg1_posts_count = 0;
			for(int e = 0; e < deg_posts.length; e++) {
				if (matching[e] != -1) {
    				deg_posts[e] = 0;
    			}
    			else{
    				int k = deg_posts[e];
    				for(int i = 0; i < post_list[e].length; i++){
    					if (post_list[e][i] == 1 && even_apps[i] != -1)
    						k--;
    				}
    				deg_posts[e] = k;
    				if (k == 1) {
    					deg1_posts_count++;
    				}
    			}
			}
    		
			
//			System.out.println(Arrays.toString(matching) + " matched" + num_match);
//			System.out.println(Arrays.toString(matching_apps) + " matched apps" + num_match);
//
//			System.out.println(Arrays.toString(deg_posts) + " degrees" + num_match);
//			System.out.println(deg1_posts_count);

    	}
    	
		int num_deg0_posts = 0;
		for(int e = 0; e < deg_posts.length; e++) {
			if(deg_posts[e] == 0)
				num_deg0_posts++;
		}
		int not_match = apps-num_match;
		
		
		if (posts-num_deg0_posts < apps-num_match) {
			System.out.println(num_deg0_posts + "no app complete matching");
			matching = null;
			return false;
		}
		else {
//			System.out.println("break matching");

			//break remainder of G into perfect matching
			int next_app = -1;
			for(int i = 0; i < apps; i++) {
				if (matching_apps[i] == -1)
					next_app = i;
			}
			//while there is still an unmatched applicant
			while(next_app != -1) {
				int curr_app = next_app;
				find_app_edge:
				for (int i = 0; i < posts; i++) {
					if (app_list[curr_app][i] == 1 && matching[i] == -1) { 
						//if there exists an edge (app, i) and i is unmatched, match them
						matching_apps[curr_app] = i;
						matching[i] = curr_app;
						next_app = -1;
						for(int k = 0; k < apps; k++) {
							if (matching_apps[k] == -1 && post_list[i][k] == 1) {
								next_app = k;
								break find_app_edge;
							}
						}
						
						
					}
				}
				
			}
		}
    	

//		System.out.println(Arrays.toString(matching) + " matched" + num_match);
//		System.out.println(Arrays.toString(matching_apps) + " matched apps" + num_match);
//    	int matching[] = {-1,-1,-1,-1};
//    	int test[]  = {-1, 3, 0, -1};
//    	int is[] = {0, 1, 2, 3};
//    	Arrays.stream(is).filter(i -> test[i] != -1)
//    		      .forEach(i -> {
//    		    	  if (i != -1)
//    		    		  matching[test[i]] = i;
//    		      });
//

		return true;
    }
    
    private static void getFPosts(int[] is_fpost, int[] app1_fpost, ArrayList<ArrayList<Integer>> reduced_G, int posts) {
    	
    	for (int i = 0; i < reduced_G.size(); i++) {
    		ArrayList<Integer> k = reduced_G.get(i);
    		is_fpost[k.get(0)] = 1;
    		app1_fpost[k.get(0)] = i;
    	}

    }
    private static ArrayList<ArrayList<Integer>> getReducedGraph(ArrayList<ArrayList<Integer>> pref_list) {
    	ArrayList<ArrayList<Integer>> reduced_G = new ArrayList<ArrayList<Integer>>();
    	ArrayList<Integer> fposts = new ArrayList<Integer>(); 

    	for (int p = 0; p < pref_list.size(); p++) {
    		fposts.add(pref_list.get(p).get(0));
    	}
    	for (int p = 0; p < pref_list.size(); p++) {
    		reduced_G.add(new ArrayList<Integer>());
    		ArrayList<Integer> lst = pref_list.get(p);
    		reduced_G.get(p).add(lst.get(0));
    		addtwo:
    		for (Integer i:lst) {
    			if (!fposts.contains(i)) {
    				reduced_G.get(p).add(i);
    				break addtwo;
    			}
    		}
    	}
    	return reduced_G;
    }
    private static int[][] getAppList(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts){
    	int[][] app_list =  new int[apps][posts];
    	for (int i = 0; i < pref_list.size(); i++) {
    		for (int k = 0; k < pref_list.get(i).size(); k++) {
    			app_list[i][pref_list.get(i).get(k)] = 1;
    		}
    	}
    	return app_list;
    }   
    
    private static int[][] getPostList(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts){
    	int[][] post_list =  new int[posts][apps];
    	for (int i = 0; i < pref_list.size(); i++) {
    		for (int k = 0; k < pref_list.get(i).size(); k++) {
    			post_list[pref_list.get(i).get(k)][i] = 1;
    		}
    	}
    	return post_list;
    }
    private static int[] getAppDeg(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts){
    	int[] app_list =  new int[apps];
    	for (int i = 0; i < pref_list.size(); i++) {
    		for (int k = 0; k < pref_list.get(i).size(); k++) {
    			app_list[i] ++;
    		}
    	}
    	return app_list;
    }   

    
    private static int[] getPostDeg(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts){
    	int[] post_list =  new int[posts];
    	for (int i = 0; i < pref_list.size(); i++) {
    		for (int k = 0; k < pref_list.get(i).size(); k++) {
    			post_list[pref_list.get(i).get(k)] ++;
    		}
    	}
    	return post_list;
    }
    
    private static ArrayList<ArrayList<Integer>> parsePrefList(String[] args, Scanner sc, int apps, int posts) throws Exception {
    	
    	ArrayList<ArrayList<Integer>> app_prefs = new ArrayList<ArrayList<Integer>>();
   
    	for (int i = 0; i < apps; i++) {
            String line = sc.nextLine();
            String[] preferences = line.split(",");
            ArrayList<Integer> i_prefs = new ArrayList<Integer>();
            for (Integer j = 0; j < preferences.length; j++) {
                i_prefs.add(Integer.parseInt(preferences[j]));
            }
            app_prefs.add(i_prefs);
        }

    	return app_prefs;
    }
}
