/**
 * People occupy places
 * @author Douglas Jones & Parijat Tripathi 
 * @versioin 12/07/2020 -- To be turned in as MP11
 * Status:  Works with new simulation framework
 * @see Place
 * @see Employee
 */

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
/** <p> 
  * Scheduling the state change from the uninfected to the infected
  * does so making a call to beInfectious, and is done from outer class. 
  * </p>
  * @param Person p (instantiate object in person class), double t (time), doubl  *  double delay 
  */
class ToInfectious extends Simulator.Event { 
	final Person state;
	ToInfectious(Person p, double t, double delay){
	   super(delay + t);
	   state = p;
	}
	public void trigger(){
		state.beInfectious(time);
	}
}
/** <p> 
  * the following outer class will schedule infected people, to recovered
  * </p> 
  * @param Person p, double t, double delay
  */
class TobeRecovered extends Simulator.Event{
	final Person state;
	TobeRecovered(Person p, double t, double delay){
		super(delay + t);
		state = p;
	}
	public void trigger(){
		state.beRecovered(time);
	}
}
/** <p> 
  * the following outer class will schedule infected people, to be bedridden
  * </p>
  */
class InfectiousTobeBedridden extends Simulator.Event{
	final Person state;
	/**
    	  * @param Person p (object in class Person) , double t (time), delay (the log normal delay of scheduling a sickness).
          */
	InfectiousTobeBedridden(Person p, double t, double delay){
		super(delay  + t);
		state = p; 
	}
	//trigger will schedule the next event if this class is invoked. 
	public void trigger(){
		state.beBedridden(time);
	}
}
/** <p> 
  *  the following outer class will schedule all bedridden people to become recovered
  *  </p>
  */
class beBedridden_Recovered extends Simulator.Event{
	final Person state;
	/** 
          * @param Person p (object in class Person), double t (time of scheduling), delay (log normal time between next event to schedule).
          */
	beBedridden_Recovered(Person p, double t, double delay){
		super(delay + t);
		state = p;
	}
	//trigger will invoke method for individual to become recovered. 
	public void trigger(){
		state.beRecovered(time);
	}
}
/** <p> 
  *  the following outer class will schedule all bedridden people to die
  * </p> 
  */
class beBedridden_beDead extends Simulator.Event{
	final Person target;
	/** 
  	  * @param Person p (object in class Person), double t (time of scheduling), delay (log normal time between next event)
   	  */
	beBedridden_beDead(Person p, double t, double delay){
		super(delay + t);
		target = p;
	}
 	//trigger will invoke method for individual to die.
	public void trigger(){
		target.beDead(time);
	}
}
/** <p> 
  * This outer class will create the report at midnight when invoked
  * </p> 
  */
class createReport extends Simulator.Event{
	/**
     	  * @param takes in double t (time of event scheduling).
          */
	createReport(  double t){
		//this will convert the time based on simulator days. 
		super(t + Simulator.day);
	}
	//trigger will invoke the method to invoke time. 
	public void trigger(){
		Person.report(time);
	}
}
/** <p> 
  * The following outer class will move individuals from one destination to another. 
  * </p>
  */
class travelTotoArriveAt extends Simulator.Event{
	//we instantiate object in both place and person to move perso to a place. 
	final Place location;
	final Person target;
	/**
  	  * @param Person p (object in class person), Place pl (object in class place), double t (time for scheduling event).
	  */
	travelTotoArriveAt ( Person p, Place pl, double t ){
		super(t);
		location = pl;
		target = p;
	}
	//trigger will move person to a given place at a given time. 
	public void trigger(){
		target.arriveAt(time, location);
	}
}
/**
 * People occupy places
 * @author Douglas Jones
 * @version 11/16/2020 -- To be turned in as MP10
 * Status:  Works with new simulation framework
 * @see Place
 * @see Employee
 */
class Person {
    // private stuff needed for instances

    protected enum States {
	uninfected, latent, infectious, bedridden, recovered, dead
	// the order of the above is significant: >= uninfected is infected
    }

    // static attributes describing progression of infection
    // BUG --  These should come from model description file, not be hard coded
    double latentMedT = 2 * Simulator.day;
    double latentScatT = 1 * Simulator.day;
    double bedriddenProb = 0.7;
    double infectRecMedT = 1 * Simulator.week;
    double infectRecScatT = 6 * Simulator.day;
    double infectBedMedT = 3 * Simulator.day;
    double infectBedScatT = 5 * Simulator.day;
    double deathProb = 0.2;
    double bedRecMedT = 2 * Simulator.week;
    double bedRecScatT = 1 * Simulator.week;
    double bedDeadMedT = 1.5 * Simulator.week;
    double bedDeadScatT = 1 * Simulator.week;

    // static counts of infection progress
    private static int numUninfected = 0;
    private static int numLatent = 0;
    private static int numInfectious = 0;
    private static int numBedridden = 0;
    private static int numRecovered = 0;
    private static int numDead = 0;

    // fixed attributes of each instance
    private final HomePlace home;  // all people have homes
    public final String name;      // all people have names

    // instance variables
    protected Place place;         // when not in transit, where the person is
    public States infectionState;  // all people have infection states

    // the collection of all instances
    private static final LinkedList <Person> allPeople =
	new LinkedList <Person> ();

    // need a source of random numbers
    private static final MyRandom rand = MyRandom.stream();

    /** The only constructor
     *  @param h the home of the newly constructed person
     */
    public Person( HomePlace h ) {
	name = super.toString();
	home = h;
	place = h; // all people start out at home
	infectionState = States.uninfected;
	numUninfected = numUninfected + 1;
	h.addResident( this );

	allPeople.add( this ); // this is the only place items are added!
    }

    /** Predicate to test person for infectiousness
     *  @return true if the person can transmit infection
     */
    public boolean isInfectious() {
	return (infectionState == States.infectious)
	    || (infectionState == States.bedridden);
    }

    /** Primarily for debugging
     *  @return textual name and home of this person
     */
    public String toString() {
	return name ;// DEBUG  + " " + home.name + " " + infectionState;
    }

    /** Shuffle the population
     *  This allows correlations between attributes of people to be broken
     */
    public static void shuffle() {
	Collections.shuffle( allPeople, rand );
    }

    /** Allow outsiders to iterate over all people
     *  @return an iterator over people
     */
    public static Iterator <Person> iterator() {
	return allPeople.iterator();
    }

    // simulation methods relating to infection process

    /** Infect a person
     *  @param t, the time at which the person is infected
     *  called when circumstances call for a person to become infected
     */
    public void infect( double t ) {
	if (infectionState == States.uninfected) {
	    // infecting an already infected person has no effect
	    double delay = rand.nextLogNormal( latentMedT, latentScatT );

	    numUninfected = numUninfected - 1;
	    infectionState = States.latent;
	    numLatent = numLatent + 1;
	    Simulator.schedule(new ToInfectious(this, t, delay ) ); 
	}
    }
    /** An infected but latent person becomes infectous
     *  scheduled by infect() to make a latent person infectious.
     *  @param t, double for scheduling 
     */
    public void beInfectious( double t ) {
	numLatent = numLatent - 1;
	infectionState = States.infectious;
	numInfectious = numInfectious + 1;

	if (place != null) place.oneMoreInfectious( t );

	if (rand.nextFloat() > bedriddenProb) { // person stays asymptomatic
	    double delay = rand.nextLogNormal( infectRecMedT, infectRecScatT );
            Simulator.schedule( new TobeRecovered(this, t, delay) );
	} else { // person becomes bedridden
	    double delay = rand.nextLogNormal( infectBedMedT, infectBedScatT );
            Simulator.schedule( new InfectiousTobeBedridden(this,t, delay) );
	}
    }

    /** An infectious person becomes bedridden
     *  scheduled by beInfectious() to make an infectious person bedridden
     *  @param t, a double 
     */
    public void beBedridden( double t ) {
	numInfectious = numInfectious - 1;
	infectionState = States.bedridden;
	numBedridden = numBedridden + 1;

	if (rand.nextFloat() > deathProb) { // person recovers
	    double delay = rand.nextLogNormal( bedRecMedT, bedRecScatT );
            Simulator.schedule( new beBedridden_Recovered(this, t, delay) );
	} else { // person dies
	    double delay = rand.nextLogNormal( bedDeadMedT, bedDeadScatT );
       
            Simulator.schedule( new beBedridden_beDead(this, t, delay) );
	}

	// if in a place (not in transit) that is not home, go home now!
	if ((place != null) && (place != home)) goHome( t );
    }

    /** A infectious or bedridden person recovers
     *  scheduled by beInfectious() or beBedridden to make a person recover.
     *  @param time, scheduling for time. 
     */
    public void beRecovered( double time ) {
	if (infectionState == States.infectious) {
	    numInfectious = numInfectious - 1;
	} else {
	    numBedridden = numBedridden - 1;
	}
	infectionState = States.recovered;
	numRecovered = numRecovered + 1;

	if (place != null) place.oneLessInfectious( time );
    }

    /** A bedridden person dies
     *  scheduled by beInfectious() to make a bedridden person die.
     *  @param time, double time for scheduling. 
     */
    public void beDead( double time ) {
	numBedridden = numBedridden - 1;
	infectionState = States.dead; // needed to prevent resurrection
	numDead = numDead + 1;

	// if the person died in a place, make them leave it!
	if (place != null) place.depart( this, time );

	// BUG: leaves them in the directory of residents and perhaps employees
    }

    // simulation methods relating to daily reporting

    /** Make the daily midnight report
     *  @param t, the current time
     */
    public static void report( double t ) {
	System.out.println(
	    "at " + t
	  + ", un = " + numUninfected
	  + ", lat = " + numLatent
	  + ", inf = " + numInfectious
	  + ", bed = " + numBedridden
	  + ", rec = " + numRecovered
	  + ", dead = " + numDead
	);

	// make this happen cyclically
        Simulator.schedule( new createReport( t) );
    }

    // simulation methods relating to personal movement

    /** Make a person arrive at a new place
     *  @param p new place
     *  @param time, the current time
     *  scheduled
     */
    public void arriveAt( double time, Place p ) {
	if ((infectionState == States.bedridden) && (p != home)) {
	    // go straight home if you arrive at work while sick
	    goHome( time );

	} else if (infectionState == States.dead) { // died on the way to work
	    // allow this person to be forgotten

	} else { // only really arrive if not sick
	    p.arrive( this, time );
	    this.place = p;
	}
    }

    /** Move a person to a new place
     *  @param p, the place where the person travels
     *  @param t, time at which the move will be completed
     *  BUG -- if time was the time the trip started:
     *  travelTo could do the call to this.place.depart()
     *  and it could compute the travel time
     */
    public void travelTo( Place p, double t ) {
	this.place = null;
        Simulator.schedule( new travelTotoArriveAt( this, p, t) );
    }

    /** Simulate the trip home from wherever
     * @param time of departure
     */
    public void goHome( double time ) {
	double travelTime = rand.nextLogNormal(
	    20 * Simulator.minute, // mean travel time
	    3 * Simulator.minute   // scatter in travel time
	);

	// the possibility of arriving at work after falling ill requires this
	if (this.place != null) this.place.depart( this, time );

	this.travelTo( this.home, time + travelTime );
    }
}
