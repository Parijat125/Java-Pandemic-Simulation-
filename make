# Makefile for the Epidemic Simulator
# Author Parijat Tripathi
# Author Douglas Jones (Source Code MP9 and MP10)
# Version 2020- 11- 25
# The following commands are supported 
#	make	-- equivalent to make the Epidemic.class
#	make test	-- run a demo showing how the program works
#	make html	-- makes web site of internal documentation 
#	make clean	-- deletes all files created by any of the above
##################
#All the source files are broken up by category. 

UtilityJava = Error.java MyScanner.java MyRandom.java Simulator.java 

PersonSubClassJava = Employee.java  
PlaceSubClassJava = HomePlace.java WorkPlace.java
ModelJava = Person.java Place.java $(PersonSubClassJava) $(PlaceSubClassJava)

MainJava = Epidemic.java

AllJava = $(UtilityJava) $(ModelJava) $(MainJava)
#################
#primary make target

Epidemic.class: AllJava
	javac @AllJava

UtilityClasses = Error.class MyScanner.class Simulator.class
SimulationClasses = Person.class Place.class 

AllJava: Makefile
	echo $(AllJava) > AllJava

#Secondary make target -- Simulation model 

Person.class: Person.java Place.java $(UtilityClasses)
	javac Person.java

PersonDepends = Employee.class 
Employee.class: Employee.java $(PersonDepends) $(UtilityClasses)

PlaceDepends = Place.java WorkPlace.class HomePlace.class
Place.class = Person.java Place.java $(PlaceDepends) $(UtilityClasses)

HomePlace.class: HomePlace.java Person.class Place.class $(UtilityClasses)
	javac HomePlace.java

WorkPlace.class: WorkPlace.java Person.class Place.class $(UtilityClasses)
	javac WorkPlace.java 

#tertiary make target -- utility classes

MyScanner.class: MyScanner.java Error.class
	javac MyScanner.java

Error.class: Error.java
	javac Error.java

Random.class: Random.java
	javac Random.java

Simulator.class: Simulator.java 
	javac Simulator.java

##################
# utility make targets

test: Epidemic.class
	java Epidemic testfile

html: AllJava 
	javadoc @AllJava

clean: 
	rm -f *.zip *.js *.class *.html AllJava  package-list script.js stylesheet.css
