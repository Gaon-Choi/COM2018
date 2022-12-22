/** Class that prints the Collatz sequence starting from a given number.
 *  @author Gaon Choi
 */
public class Collatz {

    /**
     * A function which returns the next Collatz sequence starting from a given number
     * @source https://en.wikipedia.org/wiki/Collatz_conjecture
     * @param n a given integer
     * @return  n / 2   (when n is even)
     *          3n + 1  (when n is odd)
     */
    public static int nextNumber(int n) {
        // if n is even, the next number is n / 2
        if (n % 2 == 0) {
            return n / 2;
        }
        // if n is odd, the next number is 3n + 1
        else {
            return 3 * n + 1;
        }
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            // recursive calls of nextNumber
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

