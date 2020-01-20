package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Landscaper extends Unit {
    static boolean landscaperInPlace = false;

    public Landscaper(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 36) {
            if (rc.getRoundNum() < emergencyProteccRound)
                runLandscaperProtecc();
            else
                runLandscaperEmergencyProtecc();
        }
        else
            runLandscaperAttacc();
    }

    void runLandscaperAttacc() throws GameActionException {
        // find enemy HQ
        MapLocation netGun = null;
        MapLocation designSchool = null;
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);
                System.out.println("Found enemy HQ!");
            }
        }
        // now look for friendly net gun and design school ON ADJACENT TILES
        robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.NET_GUN && robot.team == rc.getTeam()) {
                netGun = robot.location;
                System.out.println("Found friendly net gun!");
            }
            if (robot.type == RobotType.DESIGN_SCHOOL && robot.team == rc.getTeam()) {
                designSchool = robot.location;
            }
        }
        // If there is a nearby net gun with dirt on it, remove the dirt from that
        if (netGun != null) {
            if (rc.canDigDirt(rc.getLocation().directionTo(netGun))) {
                rc.digDirt(rc.getLocation().directionTo(netGun));
                System.out.println("Removed dirt from friendly net gun!");
            }
        }
        // also remove dirt from fulfillment centers
        if (designSchool != null) {
            if (rc.canDigDirt(rc.getLocation().directionTo(designSchool))) {
                rc.digDirt(rc.getLocation().directionTo(designSchool));
                System.out.println("Removed dirt from friendly fulfillment center!");
            }
        }

        // pile on the dirt
        if (rc.canDepositDirt(currentDir)){
          rc.depositDirt(currentDir);
          System.out.println("I deposited dirt!");
        }

        // TODO: Would it be better to dig dirt from current location to mess up walls?
        if (rc.canDigDirt(Direction.CENTER)) {
          rc.digDirt(Direction.CENTER);
          System.out.println("I dug dirt from my own location!");
        } else if (rc.canDigDirt(currentDir.opposite())) {
          rc.digDirt(currentDir.opposite());
          System.out.println("I dug dirt from " + currentDir.opposite() + "!");
        } else {
            System.out.println("Could not dig dirt from current location or direction " + currentDir.opposite());
        }
        if (rc.canDigDirt(currentDir.opposite().rotateLeft()))
          rc.digDirt(currentDir.opposite().rotateLeft());
        if (rc.canDigDirt(currentDir.opposite().rotateRight()))
          rc.digDirt(currentDir.opposite().rotateRight());    
        
    }

    void runLandscaperProtecc() throws GameActionException {
        ArrayList<MapLocation> wallLocations = new ArrayList<MapLocation>(8);
            for (Direction dir : Util.directions)
                wallLocations.add(hqLoc.add(dir));
        
        // now try to see if any are occupied by landscapers already
        for (int counter = wallLocations.size() - 1; counter >= 0; counter --){
            MapLocation loc = wallLocations.get(counter);
            if (rc.canSenseLocation(loc)){
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if (robot != null) {
                    if (robot.getID() != rc.getID() && robot.getType() == RobotType.LANDSCAPER)
                        wallLocations.remove(counter);
                }
            }
        }
        System.out.println(wallLocations.size() + " locations around HQ not occupied by other landscapers!");
        MapLocation currentLoc = rc.getLocation();
        // try to walk to first unoccupied location in list (unless already there)
        if (wallLocations.size() > 0) {
            if (wallLocations.get(0).equals(currentLoc))
                landscaperInPlace = true;
            else
                nav.goTo(wallLocations.get(0));
        } else {
            // try to walk to north side of HQ
            if (currentLoc.translate(0,1).isAdjacentTo(hqLoc))
                nav.tryMove(Direction.NORTH);
            if (currentLoc.translate(1,1).isAdjacentTo(hqLoc))
                nav.tryMove(Direction.NORTHEAST);
            if (currentLoc.translate(-1,1).isAdjacentTo(hqLoc))
                nav.tryMove(Direction.NORTHWEST);

            // if still not adjacent to HQ, try to move in random direction
            if (!currentLoc.isAdjacentTo(hqLoc)) {
                nav.tryMove(Util.randomDirection());
                return;
            }
        }

        if (currentLoc.isAdjacentTo(hqLoc)) {
            // if HQ has dirt on it, remove dirt
            Direction dirToHQ = currentLoc.directionTo(hqLoc);
            if (rc.canDigDirt(dirToHQ))
                rc.digDirt(dirToHQ);
            // TODO: second-best thing: put dirt on enemy building in range
            if (landscaperInPlace) {
                // build wall
                if (rc.canDepositDirt(Direction.CENTER))
                    rc.depositDirt(Direction.CENTER);
                // finally, dig dirt from direction opposite HQ
                if (rc.canDigDirt(dirToHQ.opposite()))
                    rc.digDirt(dirToHQ.opposite());
            }
        }
    }

    void runLandscaperEmergencyProtecc() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        Direction dirToHQ = currentLoc.directionTo(hqLoc);

        // get adjacent tiles that are also adjacent to HQ (but not HQ itself)
        ArrayList<MapLocation> candidateTiles = new ArrayList<MapLocation>(8);
        for (Direction dir : Util.directions) {
            MapLocation loc = currentLoc.add(dir);
            if (rc.onTheMap(loc) && loc.isAdjacentTo(hqLoc) && !loc.equals(hqLoc))
                candidateTiles.add(loc);
        }
        if (currentLoc.isAdjacentTo(hqLoc))
            candidateTiles.add(currentLoc);
        // if HQ is covered in dirt, removing it is highest priority
        if (currentLoc.isAdjacentTo(hqLoc) && rc.canDigDirt(dirToHQ))
            rc.digDirt(dirToHQ);

        // if any candidate tile is unoccupied and you are not adjacent to HQ, walk there
        if (!currentLoc.isAdjacentTo(hqLoc)) {
            for (MapLocation loc : candidateTiles) {
                if (rc.canSenseLocation(loc) && rc.senseRobotAtLocation(loc) == null
                        && currentLoc.isAdjacentTo(loc))
                    nav.tryMove(rc.getLocation().directionTo(loc));
            }
        }

        // see which of the locations has the smallest amount of dirt on it (so we can put dirt there)
        int minDirt = Integer.MAX_VALUE;
        MapLocation minDirtLocation = null;
        for (MapLocation loc : candidateTiles) {
            if (rc.senseElevation(loc) < minDirt) {
                minDirtLocation = loc;
                minDirt = rc.senseElevation(loc);
            }
        }

        // also some self-defense: If current tile is within 2 squares of HQ
        // and current tile has < 10% of dirt of lowest candidate tile
        // then defend own tile before building wall (so that landscaper can survive longer)
        if (currentLoc.distanceSquaredTo(hqLoc) <= 8 && rc.senseElevation(currentLoc) * 10 < minDirt)
            minDirtLocation = currentLoc;

        // if this doesn't find anything, go to the normal protection routine

        if (minDirtLocation == null) {
            runLandscaperProtecc();
        } else {
            Direction dir = currentLoc.directionTo(minDirtLocation);
            if (rc.canDepositDirt(dir))
                rc.depositDirt(dir);

            // otherwise dig some dirt
            if (rc.canDigDirt(dirToHQ.opposite()))
                rc.digDirt(dirToHQ.opposite());
            if (rc.canDigDirt(dirToHQ.opposite().rotateRight()))
                rc.digDirt(dirToHQ.opposite().rotateRight());
            if (rc.canDigDirt(dirToHQ.opposite().rotateLeft()))
                rc.digDirt(dirToHQ.opposite().rotateLeft());
        }
    }
}