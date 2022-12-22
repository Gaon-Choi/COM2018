package gh2;

// TODO: uncomment the following import once you're ready to start this portion

import deque.Deque;
import deque.LinkedListDeque;
// TODO: maybe more imports

//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
    /**
     * Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday.
     */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    // TODO: uncomment the following line once you're ready to start this portion
    private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        // TODO: Create a buffer with capacity = SR / frequency. You'll need to
        //       cast the result of this division operation into an int. For
        //       better accuracy, use the Math.round() function before casting.
        //       Your should initially fill your buffer array with zeros.
        int capacity = (int) Math.round(SR / frequency);
        this.buffer = new LinkedListDeque<>();
        int i = 0;
        while (i < capacity) {
            this.buffer.addFirst(0.0);
            i++;
        }
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        // TODO: Dequeue everything in buffer, and replace with random numbers
        //       between -0.5 and 0.5. You can get such a number by using:
        //       double r = Math.random() - 0.5;
        //
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.

        // generate random number between -0.5 and +0.5
        for (int i = 0; i < this.buffer.size(); i++) {
            double r = Math.random() - 0.5;
            this.buffer.removeLast();
            this.buffer.addFirst(r);
        }

    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        // TODO: Dequeue the front sample and enqueue a new sample that is
        //       the average of the two multiplied by the DECAY factor.
        //       **Do not call StdAudio.play().**
        /* Karplus-Algorithm */

        // 1. Replace every item in a Deque with random noise (double values between -0.5 and 0.5).
        // --> it is done in pluck() method.

        // 2. Remove the front double in the Deque and average it with the next double in the Deque
        double removed = this.buffer.removeFirst();
        double next = this.buffer.get(0);   // second elem -> after deletion -> 1st elem
        double newDouble = avgDouble(removed, next);

        // 3. Then, add newDouble to the back of the Deque.
        this.buffer.addLast(newDouble);

    }

    private double avgDouble(double d1, double d2) {
        return DECAY * (d1 + d2) / 2;
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        // TODO: Return the correct thing.
        return this.buffer.get(0);
    }
}
// TODO: Remove all comments that say TODO when you're done.
