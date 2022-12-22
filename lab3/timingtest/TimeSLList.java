package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int N = 1000;
        int M = 10000;

        AList<Integer> Ns = new AList();
        AList<Double> times = new AList();
        AList<Integer> opCounts = new AList();

        for (int i = 0; i < 8; i++) {
            // temporary empty AList
            SLList<Integer> tmp = new SLList();

            // insert element into AList Ns
            Ns.addLast(N);

            for (int j = 0; j < N; j++) {
                tmp.addLast(j);
            }

            Stopwatch sw = new Stopwatch();
            for (int k = 0; k < M; k++) {
                tmp.getLast();
            }
            double timeInSeconds = sw.elapsedTime();

            // time stops!
            times.addLast(timeInSeconds);

            // insert the number of ops into opCounts
            opCounts.addLast(M);

            // double N
            N *= 2;
        }
        printTimingTable(Ns, times, opCounts);
    }
}