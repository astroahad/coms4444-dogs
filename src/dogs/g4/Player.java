package dogs.g4; // TODO modify the package name to reflect your team

import java.util.*;

import dogs.sim.*;
import dogs.sim.Dictionary;


public class Player extends dogs.sim.Player {

    private String firstSignal = "zythum";
    private List<Owner> coOwners = new ArrayList<>();
    // private List<Owner> collaboraters new ArrayList<>();
    private List<Owner> g1Owners = new ArrayList<>();
    private List<Owner> g2Owners = new ArrayList<>();
    private List<Owner> g3Owners = new ArrayList<>();
    private List<Owner> g4Owners = new ArrayList<>();
    private List<Owner> g5Owners = new ArrayList<>();
    // private List<Owner> unknownOwners = new ArrayList<>();




    HashMap<String, Owner> collaboraters = new HashMap<String, Owner>();//Creating HashMap   
	
    /**
     * Player constructor
     *
     * @param rounds           number of rounds
     * @param numDogsPerOwner  number of dogs per owner
     * @param numOwners	       number of owners
     * @param seed             random seed
     * @param simPrinter       simulation printer
     *
     */
     public Player(Integer rounds, Integer numDogsPerOwner, Integer numOwners, Integer seed, Random random, SimPrinter simPrinter) {
         super(rounds, numDogsPerOwner, numOwners, seed, random, simPrinter);
     }

    /**
     * Choose command/directive for next round
     *
     * @param round        current round
     * @param myOwner      my owner
     * @param otherOwners  all other owners in the park
     * @return             a directive for the owner's next move
     *
     */
    public Directive chooseDirective(Integer round, Owner myOwner, List<Owner> otherOwners) {
        Directive myDirective = new Directive();

        if (round == 1) {
            myDirective.signalWord = firstSignal;
            myDirective.instruction = Directive.Instruction.CALL_SIGNAL;
            return myDirective;
        }

        if (round > 0) {
            for (Owner owner : otherOwners) {
                if (owner.getCurrentSignal().equals(firstSignal)) {
                    this.coOwners.add(owner);
                }
                if (owner.getCurrentSignal().equals("papaya")) {
                    this.g1Owners.add(owner);
                }
                if (owner.getCurrentSignal().equals("two")) {
                    this.g2Owners.add(owner);
                }
                if (owner.getCurrentSignal().equals("three")) {
                    this.g3Owners.add(owner);
                }
                if (owner.getCurrentSignal().equals("zythum")) {
                    this.g4Owners.add(owner);
                }
                if (owner.getCurrentSignal().equals("cuprodescloizite")) {
                    this.g5Owners.add(owner);
                }
                // else {
                //     this.unknownOwners.add(owner);
                // }
            }
        }

        // if (round == 1) {

        //     for (Owner owner : otherOwners) {
        //         if (owner.getCurrentSignal().equals("papaya")) {
        //             this.g1Owners.add(owner);
        //         }
        //         if (owner.getCurrentSignal().equals("two")) {
        //             this.g2Owners.add(owner);
        //         }
        //         if (owner.getCurrentSignal().equals("three")) {
        //             this.g3Owners.add(owner);
        //         }
        //         if (owner.getCurrentSignal().equals("zythum")) {
        //             this.g4Owners.add(owner);
        //         }
        //         if (owner.getCurrentSignal().equals("cuprodescloizite")) {
        //             this.g5Owners.add(owner);
        //         }
        //     }
            

        // }

        // simPrinter.println("Owner List" + g4Owners);
        // System.out.println("g4 Owner List" + g4Owners);

        Map<Owner, ParkLocation> locations = getCircularLocations(200, 200, myOwner, 40.0);
        ParkLocation finalLocation = locations.get(myOwner);

        if (finalLocation.getRow().intValue() != myOwner.getLocation().getRow().intValue() ||
                finalLocation.getColumn().intValue() != myOwner.getLocation().getColumn().intValue()){
            myDirective.instruction = Directive.Instruction.MOVE;
            myDirective.parkLocation = getMyCircularNextLocation(myOwner, finalLocation);
            return myDirective;
        }

        List<OwnerDistance> ownersDistances = getDistances(otherOwners, myOwner);
        Collections.sort(ownersDistances);
        //simPrinter.println(ownersDistances);

        List<String> otherSignals = getOtherOwnersSignals(otherOwners);
        List<Dog> waitingDogs = getWaitingDogs(myOwner, otherOwners);

        Comparator<Dog> bySpeed = (Dog d1, Dog d2) -> Double.compare(d1.getRunningSpeed(), d2.getRunningSpeed());
        Collections.sort(waitingDogs, bySpeed.reversed());

        if(waitingDogs.size() > 0) {
            myDirective.instruction = Directive.Instruction.THROW_BALL;
            myDirective.dogToPlayWith = waitingDogs.get(0);
            myDirective.parkLocation = ownersDistances.get(0).location;
        }

        return myDirective;
    }

    private List<OwnerDistance> getDistances(List<Owner> owners, Owner myOwner) {
        List<OwnerDistance> coDistances = new ArrayList<>();

        for (Owner owner : owners) {
            coDistances.add(new OwnerDistance(owner, getDist(owner.getLocation(), myOwner.getLocation()), owner.getLocation()));
        }

        return coDistances;
    }

    private Double getDist(ParkLocation l1, ParkLocation l2) {
        Double x1 = l1.getRow();
        Double y1 = l1.getColumn();
        Double x2 = l2.getRow();
        Double y2 = l2.getColumn();

        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    private ParkLocation getMyCircularNextLocation(Owner myOwner, ParkLocation finalLocation) {
        Double x = myOwner.getLocation().getRow();
        Double y = myOwner.getLocation().getColumn();

//        simPrinter.println(myOwner.getNameAsString());
//        simPrinter.println(myOwner.getLocationAsString());
//        simPrinter.println(finalLocation.toString());

        if (finalLocation.getRow().intValue() != x.intValue()) {
            if (finalLocation.getRow() >= x + 5) {
                x = x + 5;
            }
            else if (finalLocation.getRow() < x + 5) {
                x = x + Math.abs(finalLocation.getRow() - x);
            }
        }
        else if (finalLocation.getColumn().intValue() != y.intValue()) {
            if (finalLocation.getColumn() >= y + 5) {
                y = y + 5;
            }
            else if (finalLocation.getColumn() < y + 5) {
                y = y + Math.abs(finalLocation.getColumn() - y);
            }
        }

        return new ParkLocation(x, y);
    }

    private Map<Owner, ParkLocation> getCircularLocations(Integer ySize, Integer xSize, Owner myOwner, Double maxDist) {
        Map<Owner, ParkLocation> circularLocations = new HashMap<>();
        List<Owner> owners = new ArrayList<>(this.coOwners);
        owners.add(myOwner);

        Comparator<Owner> byName = (Owner o1, Owner o2) -> o1.getNameAsString().compareTo(o2.getNameAsString());
        Collections.sort(owners, byName);

        Double dividedAngle = 360.0/(owners.size());
        Double angle = 0.0;
        Double maxRadius = Math.min(ySize, xSize)/2.0;
        Double radius = (maxDist/2)/Math.sin(Math.toRadians(dividedAngle/2));

        for (Owner owner : owners) {
            Double x = maxRadius - radius*Math.cos(Math.toRadians(angle));
            Double y = maxRadius - radius*Math.sin(Math.toRadians(angle));
            circularLocations.put(owner, new ParkLocation(x,y));
            angle = angle + dividedAngle;
        }

        return circularLocations;
    }

    private Map<Owner, ParkLocation> getIdealLocations(List<Owner> coOwners, Integer ySize, Integer xSize, Double dLimit, Owner myOwner) {
        Map<Owner, ParkLocation> idealLocations = new HashMap<>();
        coOwners.add(myOwner);

        List<String> coOwnersNames = new ArrayList<>();
        for (Owner owner : coOwners) {
            coOwnersNames.add(owner.getNameAsString());
        }

        Collections.sort(coOwnersNames);


        return idealLocations;
    }

    private List<String> getOtherOwnersSignals(List<Owner> otherOwners) {
        List<String> otherOwnersSignals = new ArrayList<>();
        for (Owner otherOwner : otherOwners)
            if (!otherOwner.getCurrentSignal().equals("_"))
                otherOwnersSignals.add(otherOwner.getCurrentSignal());
        return otherOwnersSignals;
    }

    private List<Dog> getWaitingDogs(Owner myOwner, List<Owner> otherOwners) {
        List<Dog> waitingDogs = new ArrayList<>();
        for (Dog dog : myOwner.getDogs()) {
            if (dog.isWaitingForItsOwner())
                waitingDogs.add(dog);
        }
        for (Owner otherOwner : otherOwners) {
            for (Dog dog : otherOwner.getDogs()) {
                if (dog.isWaitingForOwner(myOwner))
                    waitingDogs.add(dog);
            }
        }
        return waitingDogs;
    }

    private class OwnerDistance implements Comparable<OwnerDistance> {
        Owner owner;
        Double dist;
        ParkLocation location;

        OwnerDistance(Owner owner, Double dist, ParkLocation location) {
            this.owner = owner;
            this.dist = dist;
            this.location = location;
        }

        @Override
        public int compareTo(OwnerDistance other) {
            return Double.compare(this.dist, other.dist);
        }

        public String toString() {
            return this.owner.getNameAsString() + ": " + this.dist + ": " + this.location.toString();
        }

    }

}

