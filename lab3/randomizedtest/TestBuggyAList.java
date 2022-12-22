package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> good = new AListNoResizing<>();
        BuggyAList<Integer> bad = new BuggyAList<>();

        // insert 4, 5, 6 into the two arrays respectively!
        good.addLast(4);
        good.addLast(5);
        good.addLast(6);

        bad.addLast(4);
        bad.addLast(5);
        bad.addLast(6);

        // test their sizes
        assertEquals(good.size(), bad.size());

        // test their removeLast method
        assertEquals(good.removeLast(), bad.removeLast());
        assertEquals(good.removeLast(), bad.removeLast());
        assertEquals(good.removeLast(), bad.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> Lb = new BuggyAList<>();

        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                Lb.addLast(randVal);
                // System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size1 = L.size();
                int size2 = Lb.size();
                // System.out.println("size: " + size1 + " / " + size2);
            } else if (L.size() == 0)
                continue;
            else if (operationNumber == 2) {
                // removeLast
                int rm = L.removeLast();
                // System.out.println("removeLast: " + rm);

                int rmb = Lb.removeLast();
                // System.out.println("removeLast: " + rmb);
                assertEquals(rm, rmb);
            }
        }
        // test size
        assertEquals(L.size(), Lb.size());

        // test last element
        assertEquals(L.getLast(), Lb.getLast());
    }
}
