package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Unit extends Robot {

    Navigation nav;



    static MapLocation [] recentLocs = new MapLocation[5];
    static int [] recentSoup = new int[5]; // maybe move to Miner.java
    static boolean isStuck = false;

    public Unit(RobotController r) {
        super(r);
        nav = new UnrolledNavigation(rc);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        
    }


}