import java.util.LinkedList;

/**
 * WorkPlaces are occupied by employees
 * @see Place
 * @see Employee
 */
public class WorkPlace extends Place {
    private final LinkedList <Employee> employees = new LinkedList <Employee>();

    // transmissivity median and scatter for workplaces
    // BUG --  These should come from model description file, not be hard coded
    private static final double transMed = 0.02 * Simulator.hour;
    private static final double transScat = 0.25 * Simulator.hour;

    // need a source of random numbers
    private static final MyRandom rand = MyRandom.stream();

    /** The only constructor for WorkPlace
     *  WorkPlaces are constructed with no residents
     */
    public WorkPlace() {
	super(); // initialize the underlying place
	super.transmissivity = rand.nextLogNormal( transMed, transScat );

	// make the workplace open at 8 AM
 	//Simulator.schedule(
	    //8 * Simulator.hour,
	    //(double t)-> this.open( t )
	//);
	class Event extends Simulator.Event{
	  Event(){ super(8*Simulator.hour); }
	    public void trigger(){ open(time); }
	}
	Simulator.schedule(new Event());
    }

    /** Add an employee to a WorkPlace
     *  Should only be called from the person constructor
     *  @param r an Employee, the new worker
     */
    public void addEmployee( Employee r ) {
	employees.add( r );
	// no need to check to see if the person already works there?
    }

    /** Primarily for debugging
     * @return textual name and employees of the workplace
     */
    public String toString() {
	String res = name;
	// DEBUG for (Employee p: employees) { res = res + " " + p.name; }
	return res;
    }

    // simulation methods

    /** open the workplace for business
     *  @param t the time of day
     *  Note that this workplace will close itself 8 hours later, and
     *  opening plus closing should create a 24-hour cycle.
     *  @see close
     */
    private void open( double t ) {
	// System.out.println( this.toString() + " opened at time " + time );
	// BUG -- we should probably do something useful too

	// close this workplace 8 hours later
	//Simulator.schedule(
	    //time + 8 * Simulator.hour,
	    //(double t)-> this.close( t )
	//);
	class Event extends Simulator.Event{
	  Event(){super(t + 8*Simulator.hour);}
	    public void trigger(){ close(time); } 
	}
	Simulator.schedule(new Event());
    }

    /** close the workplace for the day
     *  @param t the time of day
     *  note that this workplace will reopen 16 hours later, and
     *  opening plus closing should create a 24-hour cycle.
     *  @see open
     */
    private void close( double t ) {
	//System.out.println( this.toString() + " closed at time " + time );

	// open this workplace 16 hours later, with no attention to weekends
	//Simulator.schedule(
	    //time + 16 * Simulator.hour, /* opens 8 hours later */
	    //(double t)-> this.open( t )
	//);
	class Event extends Simulator.Event{
	  Event(){ super(t + 16*Simulator.hour); }
           public void trigger(){ open(time); } 
	}
	Simulator.schedule(new Event());
	// send everyone home
	//for (Person p: occupants) {
	    // schedule it for now in order to avoid modifying list inside loop
	    // not doing this gives risk of ConcurrentModificationException
	   // Simulator.schedule( time, (double t)-> p.goHome( t ) );
	   //class Event extends Simulator.Event{
	     //Event(){ super(t); }
             //public void trigger(){ p.goHome(time); }
	   //}
	   //Simulator.schedule(new Event());
	//}
    }
}
