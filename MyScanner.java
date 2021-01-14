import java.util.regex.Pattern;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * Wrapper or Adapter for Scanners that integrates error handling
 * @see java.util.Scanner
 * @see Error
 */
public class MyScanner {
    Scanner self; // the scanner this object wraps

    /**
     * Parameter carrier class for deferred string construction
     * used only for error message parameters to getXXX() methods
     */
    public static interface ErrorMessage {
	String myString();
    }

    // patterns for popular scannables, compiled just once
    static Pattern delimPat = Pattern.compile( "([ \t\r\n]|(//[\\S \t]*\n))*" );
	// allow empty delimiters, and allow Java style comments
    static Pattern intPat = Pattern.compile( "-?[0-9]*" );
	// integers
    static Pattern realPat = Pattern.compile(
	"-?\\d*\\.?\\d*(E(\\+|-)?\\d*)?"
    );

    /** Construct a MyScanner to read from a file
     *  @param f the file to read from
     *  @throws FileNotFoundException if the file could not be read
     */
    public MyScanner( File f ) throws FileNotFoundException {
	self = new Scanner( f );
    }

    // methods we wish could inherit from Scanner but can't beause it's final
    // BUG -- to properly handle end of line delimiters, these need redefinition
    public boolean hasNext( String s ) { return self.hasNext( s ); }
    public boolean hasNextDouble()     { return self.hasNextFloat(); }
    public boolean hasNextFloat()      { return self.hasNextFloat(); }
    public boolean hasNextInt()        { return self.hasNextInt(); }
    public String  next( String s )    { return self.next( s ); }
    public float   nextDouble()        { return self.nextFloat(); }
    public float   nextFloat()         { return self.nextFloat(); }
    public int     nextInt()           { return self.nextInt(); }
    public String  nextLine()          { return self.nextLine(); }

    // redefined methods from class Scanner

    /** Is there a next token?
     *  but first skip optional extended delimiters
     *  @return true if there is a token, otherwise false
     */
    public boolean hasNext() {
	self.skip( delimPat );          // skip the delimiter, if any
	return self.hasNext();
    }

    /** Get the next token,
     *  but first skip optional extended delimiters
     *  @return the token as a string
     */
    public String  next() {
	self.skip( delimPat );          // skip the delimiter, if any
	return self.next();
    }

    // new methods we add to this class

    /** Get the next string, if one is available
     *  @param def the default value if no string is available
     *  @param msg the error message to print if no string is available
     *  @return the token as a String or the default
     */
    public String getNext( String def, ErrorMessage msg ) {
	if (self.hasNext()) return self.next();
	Error.warn( msg.myString() );
	return def;
    }

    /** Get the next match to pattern, if one is available
     *  @param pat the pattern string we are trying to match
     *  @param def the default value if no match available
     *  @param msg the error message to print if no match available
     *  @return the token as a String or the default
     */
    public String getNext( String pat, String def, ErrorMessage msg ) {
	self.skip( delimPat );          // skip the delimiter, if any
	self.skip( "(" + pat + ")?" );  // skip the pattern if present
	String next = self.match().group();
	if (!next.isEmpty()) { // non-empty means next thing matched pat
	    return next;
	} else {
	    Error.warn( msg.myString() );
	    return def;
	}
    }

    /** Get the next double, if one is available
     *  @param def the default value if no float is available
     *  @param msg the error message to print if no double is available
     *  @return the token as a double or the default
     */
    public double getNextDouble( double def, ErrorMessage msg ) {
	self.skip( delimPat ); // skip the delimiter, if any
	self.skip( realPat );  // skip the float, if any
	String next = self.match().group();
	try {
	    return Double.parseDouble( next );
	} catch ( NumberFormatException e ) {
	    Error.warn( msg.myString() );
	    return def;
	}
    }

    /** Get the next float, if one is available
     *  @param def the default value if no float is available
     *  @param msg the error message to print if no float is available
     *  @return the token as a float or the default
     */
    public float getNextFloat( float def, ErrorMessage msg ) {
	self.skip( delimPat ); // skip the delimiter, if any
	self.skip( realPat );  // skip the float, if any
	String next = self.match().group();
	try {
	    return Float.parseFloat( next );
	} catch ( NumberFormatException e ) {
	    Error.warn( msg.myString() );
	    return def;
	}
    }

    /** Get the next int, if one is available
     *  @param def the default value if no int is available
     *  @param msg the error message to print if no int is available
     *  @return the token as an int or the default
     */
    public int getNextInt( int def, ErrorMessage msg ) {
	self.skip( delimPat ); // skip the delimiter, if any
	self.skip( intPat );   // skip the float, if any
	String next = self.match().group();
	try {
	    return Integer.parseInt( next );
	} catch ( NumberFormatException e ) {
	    Error.warn( msg.myString() );
	    return def;
	}
    }
}
