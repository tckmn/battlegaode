package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Robot {
    RobotController rc;
    Communications comms;

    int turnCount = 0;
    MapLocation lastSoupMined = null; // also used for HQ spawning miners, so can't put in Miner.java

    int attaccRound = 150; // don't stop attack before this turn unless necessary
    int proteccRound = 200; // turn to shift to defensive strategy (should be 250, set lower for testing)
    int emergencyProteccRound = 400; // needs to be before first sea level rise
    int earlyProtecc = 12; // extra soup needed to start building wall early (units of soup/early round)

    MapLocation enemyHQ = null;
    Direction currentDir = Util.randomDirection();

    MapLocation hqLoc;
    ArrayList<MapLocation> enemyHQPossibilities = new ArrayList<MapLocation>(3);
    ArrayList<MapLocation> soupLocations = new ArrayList<MapLocation>();

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

    MapLocation findNearestSoup() throws GameActionException {
        MapLocation [] soupLocations = rc.senseNearbySoup();
        MapLocation currentLoc = rc.getLocation();
        MapLocation closestSoup = null;
        int minDistance = Integer.MAX_VALUE;
        for (MapLocation loc : soupLocations) {
            //System.out.println(loc + " " + currentLoc.distanceSquaredTo(loc));
            if (currentLoc.distanceSquaredTo(loc) < minDistance) {
                minDistance = currentLoc.distanceSquaredTo(loc);
                closestSoup = loc;
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