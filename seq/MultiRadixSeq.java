import java.util.Random;

public class MultiRadixSeq {

    public static void main(String[] args) {
        new MultiRadixSeq();
    }

    private MultiRadixSeq() {
        int[] a = new int[1000];
        Random rng = new Random();

        for (int i = 0; i < a.length; i++) {
            a[i] = rng.nextInt(a.length);
        }

        radixMulti(a);
    }

    /**
     * N.B. Sorterer a[] stigende – antar at: 0 ≤ a[i]) < 232
     */
    // viktig konstant
    private final static int NUM_BIT = 7; // eller 6,8,9,10..

    private void radixMulti(int[] a) {
        long tt = System.nanoTime();

        // 1-5 digit radixSort of : a[]
        int max = a[0], numBit = 2, numDigits, n = a.length;
        int[] bit ;

        // a) finn max verdi i a[]
        for (int i = 1 ; i < n ; i++)
            if (a[i] > max) max = a[i];

        while (max >= (1L<<numBit) )numBit++; // antall siffer i max

        // bestem antall bit i numBits sifre
        numDigits = Math.max(1, numBit/NUM_BIT);
        bit = new int[numDigits];
        int rest = numBit%NUM_BIT, sum =0;

        // fordel bitene vi skal sortere paa jevnt
        for (int i = 0; i < bit.length; i++){
            bit[i] = numBit/numDigits;
            if (rest-- >0) bit[i]++;
        }

        int[] t, b = new int[n];
        for (int aBit : bit) {
            radixSort(a, b, aBit, sum); // i-te siffer fra a[] til b[]
            sum += aBit;
            // swap arrays (pointers only)
            t = a;
            a = b;
            b = t;
        }

        if ((bit.length&1) != 0 ) {
            // et odde antall sifre, kopier innhold tilbake til original a[] (nå b)
            System.arraycopy (a,0,b,0,a.length);
        }

        double tid = (System.nanoTime() -tt)/1000000.0;
        System.out.println("\nSorterte "+n+" tall paa:" + tid + "millisek.");

        testSort(a);
    } // end radix2

    /** Sort a[] on one digit ; number of bits = maskLen, shiftet up 'shift' bits */
    private void radixSort(int[] a, int[] b, int maskLen, int shift){
        // System.out.println(" radixSort maskLen:"+maskLen+", shift :"+shift);
        int acumVal = 0, j;
        int mask = (1<<maskLen) -1;
        int[] count = new int[mask+1];

        // b) count=the frequency of each radix value in a
        for (int anA : a) {
            count[(anA >>> shift) & mask]++;
        }

        // c) Add up in 'count' - accumulated values, i.e pointers
        for (int i = 0; i <= mask; i++) {
            j = count[i];
            count[i] = acumVal;
            acumVal += j;
        }

        // d) move numbers in sorted order a to b
        for (int anA : a) {
            b[count[(anA >>> shift) & mask]++] = anA;
        }
    }// end radixSort

    private void testSort(int[] a){
        for (int i = 0; i< a.length-1;i++) {
            if (a[i] > a[i+1]){
                System.out.println("SorteringsFEIL på plass: "+
                        i +" a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
                return;
            }
        }
    }// end simple sorteingstest
}
