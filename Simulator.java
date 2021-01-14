import java.util.PriorityQueue;

/**
 * Framework for discrete event simulation.
 * @author Douglas Jones
 * @version 11/13/2020 -- new simulation framework with time units
 * Status: New code!  Unstable?
 */
public abstract class Simulator {
    private Simulator(){} // prevent anyone from instantiating this class

    // BUG -- this may not be the right place to specify time units
    public static final double day = 1.0;
    public static final double hour = day / 24.0;
    public static final double minute = day / (24.0 * 60.0);
    public static final double second = day / (24.0 * 60.0 * 60.0);
    public static final double week = day * 7;

    /** Users create and schedule subclasses of events
     */  
    public static abstract class Event {

	/** The time of the event, set by the constructor */
	public final double time; // the time of this event

	/** Construct a new event and set its time
	 *  @param t, the event's time
	 */
	Event( double t ) {
	    time = t;
	}

	/** What to do when this event is triggered
	 *  Within trigger, this.time is the time of this event,
	 *  Each subclass of event must provide a trigger method.
	 */
	public abstract void trigger(); // what to do at that time
    }

    private static PriorityQueue<Event> eventSet
	= new PriorityQueue<Event> (
	    (Event e1, Event e2)-> Double.compare( e1.time, e2.time )
	);

    /** Call schedule to make an event happen at its time.
     *  Users create events with trigger method and a time, then schedule it
     */
    static void schedule( Event e ) {
	eventSet.add( e );
    }

    /** run the simulation.
     *  Call <TT>run()</TT> after scheduling some initial events
     *  to run the simulation.
     *  This becomes the main loop of the program; typically, some scheduled
     *  event will terminate the program by calling <TT>System.exit()</TT>.
     */
    static void run() {
	while (!eventSet.isEmpty()) {
	    eventSet.remove().trigger();
	}
    }
}
