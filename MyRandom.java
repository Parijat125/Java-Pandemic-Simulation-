import java.util.Random;

/** Singleton wrapper for Java's Random class
 */
public class MyRandom extends Random {
    private MyRandom() {
	// uncomment exactly one of the following!
	super();                // let Java pick a random seed
	// super( 3004 );       // set seed so we can debug
    }

    /** the only stream visible to users
     */
    static final MyRandom stream = new MyRandom();

    /** an alternate way to expose users to the stream
     *  @return handle on the stream
     */
    public static MyRandom stream() {
	return stream;
    }

    /** get the next exponentially distributed pseudo-random number
     *  @param mean value of the distribution
     *  @return the next number drawn from this distribution
     */
    public double nextExponential( double mean ) {
	return -Math.log( this.nextDouble() ) * mean;
    }

    /** get the next log-normally distributed pseudo-random number
     *  @param median value of the distribution
     *  @param scatter of the distribution
     *  @return the next number drawn from this distribution
     */
    public double nextLogNormal( double median, double scatter ) {
	double sigma = Math.log( (scatter + median) / median );
	return Math.exp( sigma * this.nextGaussian() ) * median;
    }
}
