/**
 * Error handling
 */
public class Error{
    private static int errorCount = 0;
    private static final int errorLimit = 10;

    /** Report a warning to System.err
     *  @param message the text of the warning
     */
    public static void warn( String message ) {
	System.err.println( message );
	errorCount = errorCount + 1;
	if (errorCount > errorLimit) System.exit( 1 );
    }

    /** Report a fatal error to System.err
     *  @param message the text reporting the error
     *  Note that this code exits the program with an error indication
     */
    public static void fatal( String message ) {
	warn( message );
	System.exit( 1 );
    }

    /** Quit if there were any errors
     */
    public static void quitIfAny() {
	if (errorCount > 0) System.exit( 1 );
    }
}
