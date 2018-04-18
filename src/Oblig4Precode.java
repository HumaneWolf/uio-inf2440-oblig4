import java.io.PrintWriter;
import java.util.Random;

/**
 *
 * @author Magnus Espeland <magnuesp@ifi.uio.no>
 * @changed 2018.03.19
 *
 * Class for ensuring unified output from Oblig 4, INF2440 - Spring 2018
 *
 * Usage:
 *
 * Your code should take n (int) and a seed (int) as command line parameters.
 *
 * To get the array you are going to sort:
 * --
 * int[] arr = Oblig4Precode.generateArray(n, seed);
 * --
 *
 * When you are done sorting, call this method to save some of your results:
 * (Note: Replace "username" with your username.)
 * --
 * Oblig4Precode.printResults("username", arr);
 * --
 *
 *
 *
 */

public class Oblig4Precode {

    public static int[] generateArray(int n, int seed) {
        int[] ret = new int[n];

        Random rnd = new Random(seed);

        for(int i=0; i < ret.length; i++)
            ret[i] = rnd.nextInt(n);

        return ret;

    }

    public static void printResults(String username, int[] arr) {
        String filename = "O4Result_" + username + "_" + arr.length + ".txt";

        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.printf("Results for n=%d\n", arr.length);

            for(int i=0; i < arr.length; i++) {
                writer.println(i + " : " + arr[i]);

                if(i == 49)
                    i = arr.length - 51;

            }


            writer.flush();
            writer.close();

        } catch(Exception e) {
            System.out.printf("Got exception when trying to write file %s : ",filename, e.getMessage());
        }



    }






}