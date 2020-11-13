package para;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
	
    public static void main(String[] args) throws Exception {
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

//    	for (int i = 0; i < red_G.size(); i++) {
//    		for(int k = 0; k < red_G.get(i).size(); k++)
//    			System.out.print(red_G.get(i).get(k));
//    		System.out.println();
//    	}
//    	
//    	
//    	for (int i = 0; i < post_list.length; i++) {
//    		for(int k = 0; k < post_list[0].length; k++)
//    			System.out.print(post_list[i][k]);
//    		System.out.println();
//    	}
//		System.out.println("------");
//
//    	for (int i = 0; i < app_list.length; i++) {
//    		for(int k = 0; k < app_list[0].length; k++)
//    			System.out.print(app_list[i][k]);
//    		System.out.println();
//    	}
//    	System.out.println("------");
//    	System.out.println(Arrays.toString(getAppDeg(red_G, apps, posts)));
//    	System.out.println(Arrays.toString(getPostDeg(red_G, apps, posts)));
    	
    	
    	int matching[] = {-1,-1,-1,-1};
    	int test[]  = {-1, 3, 0, -1};
    	int is[] = {0, 1, 2, 3};
    	Arrays.stream(is).filter(i -> test[i] != -1)
    		      .forEach(i -> {
    		    	  if (i != -1)
    		    		  matching[test[i]] = i;
    		      });


    	System.out.println(Arrays.toString(matching));
    	
    	
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
