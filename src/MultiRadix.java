import java.util.Arrays;
import java.util.Random;

public class MultiRadix {

    private static int n;
    private static int k;

    private static final int runs = 7;
    private static final int medianIndex = 4;
    private static double[] seqTiming = new double[runs];
    private static double[] parTiming = new double[runs];

    /**
     * Main.
     * @param args Main args.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MultiRadix [Number of numbers to sort] [Number of threads to use]");
            return;
        }
        n = Integer.parseInt(args[0]);
        k = Integer.parseInt(args[1]);
        if (k <= 0) k = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < runs; i++) {
            new MultiRadix(i);
        }

        Arrays.sort(seqTiming);
        Arrays.sort(parTiming);

        System.out.printf("Sequential median : %.3f\n", seqTiming[medianIndex]);
        System.out.printf(
                "Parallel median: %.3f Speedup from sequential: %.3f\n",
                parTiming[medianIndex], (seqTiming[medianIndex] / parTiming[medianIndex])
        );
        System.out.println("\nn = " + n);
    }

    /**
     * Create a multiradix class and start tests.
     * @param run The run id number.
     */
    private MultiRadix(int run) {
        int[] a = new int[n];
        Random rng = new Random();

        for (int i = 0; i < a.length; i++) {
            a[i] = rng.nextInt(n);
        }
        int[] seqArray = a.clone();
        int[] parArray = a.clone();

        // Do sequential tests
        System.out.println("Starting sequential");
        long startTime = System.nanoTime();
        seq(seqArray);
        seqTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Sequential time: " + seqTiming[run] + "ms.");
        testSort(seqArray);

        // Do parallel tests
        System.out.println("Starting Parallel");
        startTime = System.nanoTime();
        par(parArray);
        parTiming[run] = (System.nanoTime() - startTime) / 1000000.0;
        System.out.println("Parallel time: " + parTiming[run] + "ms.");
        testSort(parArray);
    }

    /**
     * Initialize and sort the array sequentially.
     * @param a The array to sort.
     */
    private void seq(int[] a) {
        //
    }

    /**
     * Initialize and sort the array in parallel.
     * @param a The array to sort.
     */
    private void par(int[] a) {
        //
    }

    /**
     * Find the highest value in a key range in the array.
     * @param a The array to search.
     * @param start The place to start. Inclusive.
     * @param stop The place to stop. Exclusive.
     * @return The highest number.
     */
    private int findMax(int[] a, int start, int stop) {
        int max = a[start];
        for (int i = start + 1; i < stop; i++) {
            if (a[i] > max) {
                max = a[i];
            }
        }
        return max;
    }

    /**
     * Test that the given array is sorted in increasing order.
     * @param a The array to test.
     */
    private void testSort(int[] a){
        for (int i = 0; i< a.length-1;i++) {
            if (a[i] > a[i+1]){
                System.out.println("FEIL på plass: "+
                        i +" a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
                return;
            }
        }
    }
}
