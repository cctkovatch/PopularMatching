package para;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import generator.PreferenceListGenerator;

public class Main {
	
    public static void main(String[] args) throws Exception {
    	
    	PreferenceListGenerator x = new PreferenceListGenerator(12,14);
    	x.writeToFile(x.getPrefList());
    	
    	int apps = 0;
    	int posts = 0;
    	String inputFile = args[0];
    	Scanner sc = new Scanner(new File(inputFile));
        String[] inputSizes = sc.nextLine().split(" ");
    	apps = Integer.parseInt(inputSizes[0]);
    	posts = Integer.parseInt(inputSizes[1]);
    	
    	ArrayList<ArrayList<Integer>> pref_list = parsePrefList(args, sc, apps, posts);
    	ArrayList<ArrayList<Integer>> red_G = getReducedGraph(pref_list);
    	int[][] post_list = getPostList(red_G, apps, posts);
    	int[][] app_list = getAppList(red_G, apps, posts);


    	int[] deg_apps = getAppDeg(red_G, apps, posts);
    	int[] deg_posts = getPostDeg(red_G, apps, posts);
    	int[] matching = new int[posts]; //post indexed matching
    	int[] matching_apps = new int[apps]; //app indexed matching
		Arrays.parallelSetAll(matching_apps,  e-> {return -1;});
		Arrays.parallelSetAll(matching,  e-> {return -1;});

    	ArrayList<Integer> deg1_posts = new ArrayList<Integer>();
    	int deg0_apps_count = 0;
    	int deg1_posts_count = 0;
    	for (int i = 0; i < deg_posts.length; i++) {
    		if (deg_posts[i] == 1) {
    			deg1_posts.add(i);
    			deg1_posts_count++;
    		}
    	}    	
    	
    	for (int i = 0; i < deg_apps.length; i++) {
    		if (deg_apps[i] == 0) {
    			deg0_apps_count++;
    		}
    	}
    	
    	int deg1_indexer = 0;
		int num_match = 0;		

    	deg1_loop:
    	while (deg1_posts_count > 0) {
    		int p = -1;
    		int[] even_posts = new int[posts]; 
    		int[] even_apps = new int[apps]; 
    		Arrays.parallelSetAll(even_apps,  e-> {return -1;});
    		Arrays.parallelSetAll(even_posts,  e-> {return -1;});
			//these are the posts-app edges that are even dist away from p (including p), to be added in parallel
    		
    		while(p == -1) {
    			if (deg_posts[deg1_indexer] == 1) {
    				p = deg1_indexer;
    				System.out.println("next deg 1 post is" + p);
    			}
    			deg1_indexer++;
    			if (deg1_indexer >= posts)
    				deg1_indexer %= posts;
    		
    		}

			int[] list = post_list[p];
			boolean has_next_path = true;
			int a = 0;
	
			System.out.println(Arrays.toString(deg_posts) + " degrees" + num_match);
			//finding all edges an even distance away from p
			while (has_next_path) {
				if(matching[a] == -1 && even_apps[a] == -1 && list[a] == 1) {
					even_posts[p] = a;
					even_apps[a] = p;
					num_match++;
					has_next_path = false;
					System.out.println("adding post " + p + " with app " + a);
					for (int i = 0; i < app_list[a].length; i++) {
						if (i != p && app_list[a][i] == 1) { 	//find the p's attached to app that have degree 2
							
							if (deg_posts[i] == 2 && even_posts[i] == -1) {
								System.out.println("setting next post for " + a + " as post: " + i);
								list = post_list[i];
								has_next_path = true;
								a = 0;
								p = i;
							}
						}
						
					}
				}
				a++;
			
			}
			System.out.println(Arrays.toString(even_posts) + " posts found" + num_match);
			System.out.println(Arrays.toString(even_apps) + " apps found" + num_match);
			//matching and degree posts is parallel set
    		Arrays.parallelSetAll(matching,  e -> { 
    			if (even_posts[e] != -1) 
    				return even_posts[e];
    			else
    				return matching[e];
    			});
    		Arrays.parallelSetAll(matching_apps,  e -> { 
    			if (even_apps[e] != -1) 
    				return even_apps[e];
    			else
    				return matching_apps[e];
    			});
    		Arrays.parallelSetAll(deg_posts, e->{
    			if (matching[e] != -1) {
    				return 0;
    			}
    			else{
    				int k = deg_posts[e];
    				for(int i = 0; i < post_list[e].length; i++){
    					if (post_list[e][i] == 1 && even_apps[i] != -1)
    						k--;
    				}
    				return k;
    			}
    		});  
    		
    		
			
			System.out.println(Arrays.toString(matching) + " matched" + num_match);
			System.out.println(Arrays.toString(matching_apps) + " matched apps" + num_match);

			System.out.println(Arrays.toString(deg_posts) + " degrees" + num_match);
			deg1_posts_count = (int)Arrays.stream(deg_posts).parallel().filter(i -> i == 1).count();
			System.out.println(deg1_posts_count);

    	}
		long num_deg0_posts = Arrays.stream(deg_posts).parallel().filter(i -> i == 0).count();
		int not_match = apps-num_match;
		
		
		if (posts-num_deg0_posts < apps-num_match)
			System.out.println(num_deg0_posts + "no app complete matching");
		
		else {
			System.out.println("break matching");

			//break remainder of G into perfect matching
			int next_app = -1;
			for(int i = 0; i < apps; i++) {
				if (matching_apps[i] == -1)
					next_app = i;
			}
			//while there is still an umatched applicant
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
    	

		System.out.println(Arrays.toString(matching) + " matched" + num_match);
		System.out.println(Arrays.toString(matching_apps) + " matched apps" + num_match);
//    	int matching[] = {-1,-1,-1,-1};
//    	int test[]  = {-1, 3, 0, -1};
//    	int is[] = {0, 1, 2, 3};
//    	Arrays.stream(is).filter(i -> test[i] != -1)
//    		      .forEach(i -> {
//    		    	  if (i != -1)
//    		    		  matching[test[i]] = i;
//    		      });
//
//
//    	System.out.println(Arrays.toString(matching));
    	
    	
//    	Runnable r = new PathFinder();
//    	Thread t = new Thread(r);
//    	t.start();
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
    	Arrays.parallelSetAll(app_list, i -> {
    		int t = 0;
    		for (int k = 0; k < pref_list.get(i).size(); k++) {
    			t ++;
    		}
    		return t;
    	});
    	return app_list;
    }   
    
    private static int[] getAppDegSeq(ArrayList<ArrayList<Integer>> pref_list, int apps, int posts){
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
