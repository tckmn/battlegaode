package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Robot {
    RobotController rc;
    Communications comms;

    int turnCount = 0;
    MapLocation lastSoupMined = null; // also used for HQ spawning miners, so can't put in Miner.java

    int proteccRound = 250; // turn to shift to defensive strategy (should be 250, set lower for testing)
    int emergencyProteccRound = 450; // needs to be before first sea level rise
    int earlyProtecc = 4; // extra soup needed to start building wall early (units of soup/early round)

    MapLocation enemyHQ = null;
    Direction currentDir = Util.randomDirection();

    MapLocation hqLoc;
    ArrayList<MapLocation> enemyHQPossibilities = new ArrayList<MapLocation>(3);


    public Robot(RobotController r) {
        this.rc = r;
        comms = new Communications(rc);
    }

    public void takeTurn() throws GameActionException {
        turnCount += 1;
        findHQ();
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }

    public void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                }
            }
            if(hqLoc == null) {
                // if still null, search the blockchain
                hqLoc = comms.findHQ();
            }

        // if it has found HQ, then list possible enemy HQ locations
            if (hqLoc != null) {
                int x, y, X, Y;
                x = hqLoc.x;
                y = hqLoc.y;
                X = rc.getMapWidth()-1; // correct for 0 indexing of map
                Y = rc.getMapHeight()-1;
                enemyHQPossibilities.add(new MapLocation(X-x,y));
                enemyHQPossibilities.add(new MapLocation(X-x,Y-y));
                enemyHQPossibilities.add(new MapLocation(x,Y-y));
            }
        }
    }

    // routine to sense enemies of given type
    // use this more often so we don't have to keep rewriting it
    boolean canSenseEnemy(RobotType type) throws GameActionException {
        RobotInfo [] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots)
            if (robot.type == type && robot.getTeam() != rc.getTeam())
                return true;
        return false;
    }

        // TODO: I think there's a more efficient method in the new specs
    MapLocation findNearestSoup() throws GameActionException {
        int rSq = rc.getCurrentSensorRadiusSquared();
        int k = (int)(Math.sqrt(rSq));
        MapLocation closestSoup = null;
        int minDistance = Integer.MAX_VALUE;
        for (int x = -k; x <= k; x ++){
            for (int y = -k; y <= k; y ++) {
                if (x*x + y*y <= Math.min(rSq, minDistance)) {
                    MapLocation possibleLoc = rc.getLocation().translate(x,y);
                    if (rc.canSenseLocation(possibleLoc) && rc.senseSoup(possibleLoc) > 0) {
                        // go to that location and break out of this loop
                        System.out.println("Found soup at " + possibleLoc);
                        closestSoup = possibleLoc;
                        minDistance = x*x + y*y;
                    }
                }
            }
        }
        System.out.println("Closest soup is at " + closestSoup);
        return closestSoup;
    }

    MapLocation getNearestEnemyHQPossibility() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        MapLocation nearestEnemyHQ = null;
        int distanceToEnemyHQ = Integer.MAX_VALUE;
        for (MapLocation possibility : enemyHQPossibilities) {
            if (currentLoc.distanceSquaredTo(possibility) < distanceToEnemyHQ) {
                nearestEnemyHQ = possibility;
                distanceToEnemyHQ = currentLoc.distanceSquaredTo(possibility);
            }
        }
        return nearestEnemyHQ;
    }
}