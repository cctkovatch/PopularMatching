package dist;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class MatchingMain {
    final static boolean SILENT = false;
    static int apps = 0;
    static int posts = 0;

    public static void main(String[] args) throws FileNotFoundException {
        String inputFile = args[0];
        Scanner sc = new Scanner(new File(inputFile));
        String[] inputSizes = sc.nextLine().split(" ");

        /* Grab the number of unique applicants and posts from the input file */
        apps = Integer.parseInt(inputSizes[0]);
        posts = Integer.parseInt(inputSizes[1]);

        /* Generate a preference list and then the reduced graph from the input file */
        ArrayList<ArrayList<Integer>> prefList = parsePrefList(sc, apps);
        ArrayList<ArrayList<Integer>> reducedGraph = getReducedGraph(prefList);

        /* Output progress to console */
        if(!SILENT){
            System.out.println("Printing reduced graph");
            for (ArrayList<Integer> integers : reducedGraph) {
                for (Integer integer : integers) System.out.print(integer);
                System.out.println();
            }
        }

        Matcher master = new Matcher(reducedGraph, apps, posts);
        master.StartMatching();
    }

    private static ArrayList<ArrayList<Integer>> getReducedGraph(ArrayList<ArrayList<Integer>> prefList) {
        ArrayList<ArrayList<Integer>> reducedG = new ArrayList<>();
        ArrayList<Integer> fPosts = new ArrayList<>();

        for (ArrayList<Integer> integers : prefList) {
            fPosts.add(integers.get(0));
        }
        for (int p = 0; p < prefList.size(); p++) {
            reducedG.add(new ArrayList<>());
            ArrayList<Integer> lst = prefList.get(p);
            reducedG.get(p).add(lst.get(0));

            for (Integer i:lst) {
                if (!fPosts.contains(i)) {
                    reducedG.get(p).add(i);
                    break;
                }
            }
        }
        return reducedG;
    }



    private static ArrayList<ArrayList<Integer>> parsePrefList(Scanner sc, int apps) {
        ArrayList<ArrayList<Integer>> appPrefs = new ArrayList<>();

        for (int i = 0; i < apps; i++) {
            String line = sc.nextLine();
            String[] preferences = line.split(",");
            ArrayList<Integer> i_prefs = new ArrayList<>();
            for (String preference : preferences) {
                i_prefs.add(Integer.parseInt(preference));
            }
            appPrefs.add(i_prefs);
        }

        if(!SILENT){
            System.out.println("Parsed preference list:");
            int i = 0;
            for (ArrayList<Integer> app_pref : appPrefs) {
                System.out.print(i++ + "-");
                for (Integer integer : app_pref) System.out.print(integer);
                System.out.println();
            }
            System.out.println();
        }


        return appPrefs;
    }
}
