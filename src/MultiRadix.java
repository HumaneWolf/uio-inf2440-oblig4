import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class MultiRadix {

    // STATIC VARS
    private static int n;
    private static int k;

    private final static int NUM_BIT = 7;

    private static final int runs = 7;
    private static final int medianIndex = 4;
    private static double[] seqTiming = new double[runs];
    private static double[] parTiming = new double[runs];

    // INSTANCE VARS
    private RadixWorker[] workers;
    private int numBit = 1;
    private int[] bit, globalCount;

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
        int max = findMax(a, 0, a.length);

        int numBit = 2, numDigits;
        int[] bit;

        while (max >= (1L<<numBit)) numBit++;

        numDigits = Math.max(1, numBit / NUM_BIT);
        bit = new int[numDigits];
        int rest = numBit % NUM_BIT, sum = 0;

        for (int i = 0; i < bit.length; i++){
            bit[i] = numBit / numDigits;
            if (rest-- > 0) {
                bit[i]++;
            }
        }

        int[] t, b = new int[n];
        for (int aBit : bit) {
            seqRadix(a, b, aBit, sum);
            sum += aBit;

            t = a;
            a = b;
            b = t;
        }

        if ((bit.length & 1) != 0 ) {
            System.arraycopy(a, 0, b, 0, a.length);
        }
    }

    /**
     * Sort an array from a into b sequentially.
     * @param a The array to sort.
     * @param b The array to store results in.
     * @param maskLen The length of the mask, aka the size of a digit.
     * @param shift The number of slots to shift a digit during this sorting round.
     */
    private void seqRadix(int[] a, int[] b, int maskLen, int shift) {
        int acumVal = 0, j;
        int mask = (1 << maskLen) - 1;

        int[] count = new int[mask + 1];

        for (int anA : a) {
            count[(anA >>> shift) & mask]++;
        }

        for (int i = 0; i <= mask; i++) {
            j = count[i];
            count[i] = acumVal;
            acumVal += j;
        }

        for (int anA : a) {
            b[count[(anA >>> shift) & mask]++] = anA;
        }
    }

    /**
     * Initialize and sort the array in parallel.
     * @param a The array to sort.
     */
    private void par(int[] a) {
        Thread[] threads = new Thread[k];
        workers = new RadixWorker[threads.length];
        CyclicBarrier cb = new CyclicBarrier(threads.length);

        int[] b = new int[a.length];

        for (int i = 0; i < threads.length; i++) {
            workers[i] = new RadixWorker(i, a, b, cb);
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
        }
    }

    private class RadixWorker implements Runnable {
        int id;
        int[] a, b;

        int localMax;
        int[] localCount;

        CyclicBarrier cb;

        RadixWorker(int id, int[] a, int[] b, CyclicBarrier cb) {
            this.id = id;
            this.a = a;
            this.b = b;
            this.cb = cb;
        }

        @Override
        public void run() {
            int arrSegmentSize = n / workers.length;
            int arrStart = arrSegmentSize * id;
            int arrStop = arrSegmentSize * (id + 1);
            arrStop = (id == (workers.length - 1)) ? n : arrStop;

            // Find local max.
            localMax = findMax(a, arrStart, arrStop);

            try {
                cb.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            // Combine max
            if (id == 0) {
                int max = localMax, numDigits;
                for (RadixWorker w : workers) {
                    if (w.localMax > max) {
                        max = w.localMax;
                    }
                }

                // Sets some global stuff and stuffs numBit.
                while (max >= (1L<<numBit)) numBit++;
                numDigits = Math.max(1, numBit / NUM_BIT);
                bit = new int[numDigits];

                int rest = numBit % NUM_BIT;

                for (int i = 0; i < bit.length; i++){
                    bit[i] = numBit / numDigits;
                    if (rest-- > 0) {
                        bit[i]++;
                    }
                }
            }

            try {
                cb.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

            // Do the radix loop
            int sum = 0, acumVal, tempInt, mask;
            int digitSegmentSize, digitStart, digitStop;
            int[] t;
            for (int aBit : bit) {
                // seqRadix(a, b, aBit (masklen), sum (shift));
                mask = (1 << aBit) - 1;
                localCount = new int[mask + 1];
                acumVal = 0;

                // Count locally their own part of the array.
                for (int i = arrStart; i < arrStop; i++) {
                    localCount[(a[i] >>> sum) & mask]++;
                }

                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                // Combine results and calculate accumulated val.
                if (id == 0) {
                    globalCount = new int[localCount.length];

                    // Combine
                    for (RadixWorker w : workers) {
                        for (int i = 0; i < globalCount.length; i++) {
                            globalCount[i] += w.localCount[i];
                        }
                    }

                    // Accumulate
                    for (int i = 0; i < localCount.length; i++) {
                        tempInt = globalCount[i];
                        globalCount[i] = acumVal;
                        acumVal += tempInt;
                    }
                }

                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                // Copy global to local count.
                // System.arraycopy(localCount, 0, globalCount, 0, localCount.length);

                // Move the numbers over
                // Giving each thread responsibility for their set of digits, they will only move those.
                digitSegmentSize = localCount.length / workers.length;
                digitStart = digitSegmentSize * id;
                digitStop = digitSegmentSize * (id + 1);
                digitStop = (id == (workers.length - 1)) ? localCount.length : digitStop;

                for (int i : a) {
                    tempInt = (i >>> sum) & mask; // Store the digit already masked.
                    // If in range
                    if (digitStart < tempInt && tempInt < digitStop) {
                        b[globalCount[tempInt]++] = i;
                    }
                }

                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }

                sum += aBit;

                // Swap local references
                t = a;
                a = b;
                b = t;

                // Sync before next round.
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }

            if (id == 0 && (bit.length & 1) != 0) {
                System.arraycopy(a, 0, b, 0, a.length);
            }
        }
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
                System.out.println("FEIL på plass: a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
                return;
            }
        }
    }
}
