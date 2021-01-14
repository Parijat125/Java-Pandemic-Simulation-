import java.util.LinkedList;
import java.util.Iterator;

/**
 * Places are occupied by people
 * @see HomePlace
 * @see WorkPlace
 */
public abstract class Place {
    // invariant attributes of each place
    public final String name;
    protected double transmissivity; // how infectious is this place
				     // initialized by subclass!

    // dynamic attributes of each place
    protected final LinkedList<Person> occupants = new LinkedList<> ();
    private int infectiousCount = 0; // number of infected occupants;
    double lastCheck = 0.0;  // time of last check on infectiousness

    // contructor (effectively protected
    Place() {
	name = super.toString();
	allPlaces.add( this );
    }

    // manage the infectiousness of this place

    // need a source of random numbers
    private static final MyRandom rand = MyRandom.stream();

    /** see who to infect at this time
     *  @param time, the time of the change
     *  called just before any any change to the population or infection count
     */
    private void whoToInfect( double time ) {
	// note that transmissivities are per hour, so convert time to hours
	double interval = (time - lastCheck) / Simulator.hour;
	double pInfection = transmissivity * infectiousCount * interval;

	if (interval <= 0) return; // short circuit the process for efficiency

	// probability cannot exceed one!
	if (pInfection > 1.0) pInfection = 1.0;
	// BUG -- should it be: pInfection = 1.0 - Math.exp( -pInfection );

	// give everyone a fair chance to catch the infection
	for (Person p: occupants) {
	    if (rand.nextDouble() < pInfection) {
		p.infect( time );
	    }
	}

	lastCheck = time;
    }

    /** another person here has become infectious
     *  @param time, the time of the change
     *  they either arrived while infectous
     *  or transitioned to infectous while here
     */
    public void oneMoreInfectious( double time ) {
	whoToInfect( time );
	infectiousCount = infectiousCount + 1;
    }

    /** one less person here is infectious
     *  @param time, the time of the change
     *  they either departed here while infectous
     *  or transitioned to recovered or dead while here
     */
    public void oneLessInfectious( double time ) {
	whoToInfect( time );
	infectiousCount = infectiousCount - 1;
    }

    // tools for moving people in and out of places

    /** a person arrives at this place
     *  @param p, the person who arrives
     */
    public void arrive( Person p, double time ) {
	occupants.add( p );
	if (p.isInfectious()) {
	    oneMoreInfectious( time );
	} else {
	    whoToInfect( time );
	}

	// DEBUG System.out.println(
	//  (Object)p.toString() + " arrives " + (Object)this + " at " + time
	// ); // DEBUG
    }

    /** a person leaves from this place
     *  @param p, the person who leaves
     */
    public void depart( Person p, double time ) {
	if (p.isInfectious()) {
	    oneLessInfectious( time );
	} else {
	    whoToInfect( time );
	}

	boolean wasPresent = occupants.remove( p );
	assert wasPresent: "p=" + p + " this=" + this;
	assert !occupants.contains( p ): "p=" + p + " this=" + this;

	// DEBUG System.out.println(
	//  (Object)p.toString() + " departs " + (Object)this + " at " + time
	// ); // DEBUG
    }

    // the collection of all instances
    private static final LinkedList <Place> allPlaces =
	new LinkedList <Place> ();

    /** Allow outsiders to iterate over all places
     * @return an iterator over places
     */
    public static Iterator <Place> iterator() {
	return allPlaces.iterator();
    }
}
