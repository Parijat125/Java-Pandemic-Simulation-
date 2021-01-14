import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Collections;
import java.io.File;

/**
 * Main class builds model and will someday simulate it
 * @see Person
 * @see Place
 */
public class Epidemic {

    // the following are set by readCommunity and used by buildCommunity
    // default values are used to check for failure to initialize

    static int pop = -1;         /* the target population */
    static double houseMed = -1; /* median household size */
    static double houseSc = -1;  /* household size scatter */
    static double workMed = -1;  /* median workplace size */
    static double workSc = -1;   /* workplace size scatter */
    static int infected = -1;    /* the target number of infected people */
    static double employed = -1; /* the likelihood that someone is employed */

    /** Read and check the simulation parameters
     *  @param sc the scanner to read the community description from
     *  Called only from the main method.
     */
    private static void readCommunity( MyScanner sc ) {

	while (sc.hasNext()) {
	    // until the input file is finished
	    String command = sc.next();
	    if ("pop".equals( command )) {
		if (pop > 0) Error.warn( "population already set" );
		pop = sc.getNextInt( 1, ()-> "pop with no argument" );
		sc.getNext( ";", "", ()-> "pop " +pop+ ": missed semicolon" );
		if (pop < 1) { /* sanity check on value given */
		    Error.warn( "pop " +pop+ ": non-positive population?" );
		    pop = 0;
		}

	    } else if ("house".equals( command )) {
		if (houseMed > 0) Error.warn( "household size already set" );
		if (houseSc >= 0) Error.warn( "household scatter already set" );
		houseMed = sc.getNextDouble( 1, ()-> "house with no argument" );
		sc.getNext( ",", "",
		    ()-> "house "+houseMed+": missed comma"
		);
		houseSc = sc.getNextDouble( 0,
		    ()-> "house "+houseMed+", missing argument "
		);
		sc.getNext( ";", "",
		    ()-> "house "+houseMed+", "+houseSc+": missed semicolon"
		);
		if (houseMed < 1) { /* sanity check on value given */
		    Error.warn(
			"house "+houseMed+", "+houseSc+": median nonpositive?"
		    );
		    houseMed = 0;
		}
		if (houseSc < 0) { /* sanity check on value given */
		    Error.warn(
			"house "+houseMed+", "+houseSc+": scatter negative?"
		    );
		    houseSc = 0;
		}

	    } else if ("workplace".equals( command )) {
		if (workMed > 0) Error.warn( "workplace size already set" );
		if (workSc >= 0) Error.warn( "workplace scatter already set" );
		workMed = sc.getNextDouble( 1,
		    ()-> "workplace with no argument"
		);
		sc.getNext( ",", "",
		    ()-> "workplace "+workMed+": missed comma"
		);
		workSc = sc.getNextDouble( 0,
		    ()-> "workplace "+workMed+", missed argument "
		);
		sc.getNext( ";", "",
		    ()-> "workplace "+workMed+", "+workSc+": missed semicolon"
		);
		if (workMed < 1) { /* sanity check on value given */
		    Error.warn(
			"workplace "+workMed+", "+workSc+": median nonpositive?"
		    );
		    workMed = 0;
		}
		if (workSc < 0) { /* sanity check on value given */
		    Error.warn(
			"workplace "+workMed+", "+workSc+": scatter negative?"
		    );
		    workSc = 0;
		}

	    } else if ("infected".equals( command )) {
		if (infected > 0) Error.warn( "infected already set" );
		infected = sc.getNextInt( 1, ()-> "infected with no argument" );
		sc.getNext( ";", "",
		    ()-> "infected " +infected+ ": missed semicolon"
		);
		if (infected < 0) { /* sanity check on value given */
		    Error.warn(
			"infected "+infected+": negative value?"
		    );
		    infected = 0;
		}
		if (infected > pop) { /* sanity check on value given */
		    Error.warn(
			"infected "+infected+": greater than population?"
		    );
		    infected = pop;
		}

	    } else if ("employed".equals( command )) {
		if (employed >= 0) Error.warn( "employed rate already set" );
		employed = sc.getNextDouble( 1,
		    ()-> "employed with no argument"
		);
		sc.getNext( ";", "",
		    ()-> "employed "+employed+": missed semicolon"
		);
		if (employed < 0) { /* sanity check on value given */
		    Error.warn(
			"employed "+employed+": negative value?"
		    );
		    employed = 0;
		}
		if (employed > 1) { /* sanity check on value given */
		    Error.warn(
			"employed "+employed+": greater than 1.0?"
		    );
		    employed = 1.0;
		}

	    } else if ("end".equals( command )) {
		Double endTime = sc.getNextDouble(
		    1, ()-> "end: floating point end time expected"
		);
		if (endTime <= 0) {
		    Error.warn(
			"end "+endTime+": non positive end of time?"
		    );
		}
		sc.getNext( ";", "",
		    ()-> "end "+endTime+": missed semicolon"
		);
		//Simulator.schedule( endTime, (double t)->System.exit( 0 ) );
		class Event extends Simulator.Event{
		  Event(){ super(endTime); }
		   public void trigger(){ System.exit(0); }
		}
		Simulator.schedule(new Event());
		// BUG -- A better end mechanism would output a results report
	    } else {
		Error.warn( "unknown command: "+command );
	    }
	}

	// BUG -- if there were errors, it might be best to quit now

	// check for complete initialization
	if (pop < 0)      Error.warn( "population not initialized" );
	if (houseMed < 0) Error.warn( "median household size not set" );
	if (houseSc < 0)  Error.warn( "household scatter not set" );
	if (workMed < 0)  Error.warn( "median workplace size not set" );
	if (workSc < 0)   Error.warn( "workplace scatter not set" );
	if (infected < 0) Error.warn( "infected number not given" );
	if (employed < 0) Error.warn( "employment rate not given" );
    }

    /** Build a community that the simulation parameters describe
     *  Called only from the main method.
     */
    private static void buildCommunity() {
	// must always have a home available as we create people
	int currentHomeCapacity = 0;
	int currentWorkCapacity = 0;
	HomePlace currentHome = null;
	WorkPlace currentWork = null;

	// need a source of random numbers
	final MyRandom rand = MyRandom.stream();

	// create the population
	for (int i = 0; i < pop; i++) {
	    Person p = null;
	    if (currentHomeCapacity < 1) { // must create a new home
		currentHome = new HomePlace();
		currentHomeCapacity = (int)Math.ceil(
		    rand.nextLogNormal( houseMed, houseSc )
		);
	    }
	    currentHomeCapacity = currentHomeCapacity - 1;

	    // create the right kind of person
	    if (rand.nextDouble() <= employed) { // this is as an employee
		p = new Employee( currentHome );
	    } else { // this is an unemployed generic person
		p = new Person( currentHome );
	    }

	    // decide who to infect
	    //   note: pop - i = number of people not yet considered to infect
	    //   and   infected = number we need to infect, always <= (pop - i)
	    if (rand.nextInt( pop - i ) < infected) {
		p.infect( 0 ); // infected from the beginning of time
		infected = infected - 1;
	    }
	}

	Person.shuffle(); // shuffle the population to break correlations

	// go through the population again
	for (Iterator<Person> i = Person.iterator(); i.hasNext(); ){
	    Person p = i.next(); // for each person
	    if (p instanceof Employee) {
		Employee e = (Employee)p;
		if (currentWorkCapacity < 1) { // must create new workplace
		    currentWork = new WorkPlace();
		    currentWorkCapacity = (int)Math.ceil(
			rand.nextLogNormal( workMed, workSc )
		    );
		}
		currentWorkCapacity = currentWorkCapacity - 1;
		e.setWorkplace( currentWork );
	    }
	}
	//Simulator.schedule( 0.0, (double t)->Person.report( t ) );
	class Event extends Simulator.Event{
	  Event(){ super(0.0); }
	    public void trigger(){ Person.report(time); } 
	}
	Simulator.schedule(new Event());
    }

    /** Output the community
      * Called only from the main method.
      * This code exists only for debugging.
      */
    private static void writeCommunity() {

	System.out.println( "People" ); // Note:  Not required in assignment
	for (Iterator<Person> i = Person.iterator(); i.hasNext(); ){
	    System.out.println( i.next().toString() );
	}

	System.out.println( "Places" ); // Note:  Not required in assignment
	for (Iterator<Place> i = Place.iterator(); i.hasNext(); ){
	    System.out.println( i.next().toString() );
	}
    }

    /** The main method
     *  This handles the command line arguments.
     *  If the args are OK, it calls other methods to build and test a model.
     */
    public static void main( String[] args ) {
	if (args.length < 1) {
	    Error.fatal( "Missing file name argument\n" );
	} else try {
	    readCommunity( new MyScanner( new File( args[0] ) ) );
	    Error.quitIfAny();
	    buildCommunity();  // build what was read above
	    // writeCommunity();  // DEBUG -- this is just for debugging
	    Simulator.run();
	} catch ( FileNotFoundException e) {
	    Error.fatal( "Can't open file: " + args[0] + "\n" );
	}
    }
}
