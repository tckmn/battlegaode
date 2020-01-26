package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Landscaper extends Unit {
    boolean landscaperInPlace = false;
    MapLocation designSchoolLoc = null;
    MapLocation [] locsToElevate = null;
    boolean finishedElevator = false;

    public Landscaper(RobotController r) {
        super(r);
        RobotInfo [] nearbyRobots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : nearbyRobots)
            if (robot.type == RobotType.DESIGN_SCHOOL)
                designSchoolLoc = robot.getLocation();
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // Highest priority: Remove dirt from adjacent friendly net guns and design schools
        // Do this on both attack and defense
        MapLocation netGun = null;
        MapLocation designSchool = null;
        MapLocation fulfillmentCenter = null;
        RobotInfo [] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.NET_GUN && robot.team == rc.getTeam()) {
                netGun = robot.location;
                System.out.println("Found friendly net gun!");
            }
            if (robot.type == RobotType.DESIGN_SCHOOL && robot.team == rc.getTeam()) {
                designSchool = robot.location;
            }
            if (robot.type == RobotType.FULFILLMENT_CENTER && robot.team == rc.getTeam()) {
                fulfillmentCenter = robot.location;
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
            if (rc.canDigDirt(rc.getLocation().directionTo(fulfillmentCenter))) {
                rc.digDirt(rc.getLocation().directionTo(fulfillmentCenter));
                System.out.println("Removed dirt from friendly fulfillment center!");
            }
        }
        // also remove dirt from design schools
        if (designSchool != null) {
            if (rc.canDigDirt(rc.getLocation().directionTo(designSchool))) {
                rc.digDirt(rc.getLocation().directionTo(designSchool));
                System.out.println("Removed dirt from friendly design school!");
            }
        }

        if (locsToElevate == null) {
            locsToElevate = comms.receiveElevatorRequest();
            if (locsToElevate != null)
                finishedElevator = false;
        }
        else
            performElevation();

        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 36) {
            if (rc.getRoundNum() < emergencyProteccRound)
                runLandscaperProtecc();
            else
                runLandscaperEmergencyProtecc();
        }
        else
            runLandscaperAttacc();
    }

    void performElevation() throws GameActionException {
        if (locsToElevate == null) {
            System.out.println("Something bad happened - should never call performElevation if locsToElevate is null");
            return;
        }
        boolean nothingAdjacent = true;
        MapLocation currentLoc = rc.getLocation();
        if (hqLoc == null || !currentLoc.isAdjacentTo(hqLoc)) return;
        for (MapLocation loc : locsToElevate) {
            if (loc.isAdjacentTo(currentLoc)) {
                if (rc.senseElevation(loc) < 20) {
                    nothingAdjacent = false;
                    Direction dir = currentLoc.directionTo(loc);
                    if (rc.canDepositDirt(dir))
                        rc.depositDirt(dir);
                }
            }
        }
        finishedElevator = finishedElevator || nothingAdjacent;
        if (finishedElevator)
            locsToElevate = null;
    }

    void runLandscaperAttacc() throws GameActionException {
        // find enemy HQ
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);
                System.out.println("Found enemy HQ!");
            }
        }


        // if not adjacent to enemy HQ, try to go there and otherwise dig dirt (don't deposit except on enemy HQ)
        if (enemyHQ == null || !rc.getLocation().isAdjacentTo(enemyHQ)) {
            buryEnemyBuilding();
            nav.goTo(enemyHQ);
            if (rc.isReady() && currentDir != null) rc.digDirt(currentDir.opposite());
        }

        // pile on the dirt
        if (rc.getLocation().isAdjacentTo(enemyHQ) && rc.senseRobotAtLocation(enemyHQ).dirtCarrying >= 20 && rc.canDepositDirt(currentDir)){
          rc.depositDirt(currentDir);
          System.out.println("I deposited dirt!");
        }

        buryEnemyBuilding();

        // pile on the dirt
        if (rc.getLocation().isAdjacentTo(enemyHQ) && rc.canDepositDirt(currentDir)){
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
        MapLocation currentLoc = rc.getLocation();
        if (hqLoc == null) return;
        if (currentLoc.isAdjacentTo(hqLoc)) {
            // if HQ has dirt on it, remove dirt
            Direction dirToHQ = currentLoc.directionTo(hqLoc);
            if (rc.canDigDirt(dirToHQ))
                rc.digDirt(dirToHQ);
        }
        buryEnemyBuilding();

        Direction designSchoolToHQ = designSchoolLoc.directionTo(hqLoc);
        Direction [] wallDirs = {
            designSchoolToHQ,
            designSchoolToHQ.rotateLeft(),
            designSchoolToHQ.rotateRight(),
            designSchoolToHQ.rotateLeft().rotateLeft(),
            designSchoolToHQ.rotateRight().rotateRight(),
            designSchoolToHQ.opposite().rotateRight(),
            designSchoolToHQ.opposite().rotateLeft(),
            designSchoolToHQ.opposite()
        };
        for (Direction dir : wallDirs)
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
        // try to walk to first unoccupied location in list (unless already there)
        if (!landscaperInPlace && wallLocations.size() > 0) {
            if (wallLocations.get(0).equals(currentLoc))
                landscaperInPlace = true;
            else {
                // try to walk to north side of HQ
                if (currentLoc.add(designSchoolToHQ).isAdjacentTo(hqLoc))
                    nav.tryMove(designSchoolToHQ);
                if (currentLoc.add(designSchoolToHQ.rotateLeft()).isAdjacentTo(hqLoc))
                    nav.tryMove(designSchoolToHQ.rotateLeft());
                if (currentLoc.add(designSchoolToHQ.rotateRight()).isAdjacentTo(hqLoc))
                    nav.tryMove(designSchoolToHQ.rotateRight());

                // if still not adjacent to HQ, try to move in random direction
                if (!currentLoc.isAdjacentTo(hqLoc)) {
                    nav.goTo(wallLocations.get(0));
                    return;
                }
            }
        }

        if (currentLoc.isAdjacentTo(hqLoc)) {
            Direction dirToHQ = currentLoc.directionTo(hqLoc);
            // build wall
            if (rc.canDepositDirt(Direction.CENTER))
                rc.depositDirt(Direction.CENTER);
            // finally, dig dirt from direction opposite HQ
            // Note: Don't dig dirt from corners - only N, E, S, W
            switch (dirToHQ) {
                case NORTH: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                case SOUTH: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                case EAST: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                case WEST: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                default: 
                    if (rc.canDigDirt(dirToHQ.rotateRight().rotateRight())) 
                        rc.digDirt(dirToHQ.rotateRight().rotateRight());
                    else if (rc.canDigDirt(dirToHQ.rotateLeft().rotateLeft())) 
                        rc.digDirt(dirToHQ.rotateLeft().rotateLeft());
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

        // if a distance of >= 8 from HQ, get closer
        // in particular, don't stand in corners since these get sniped by drones
        if (currentLoc.distanceSquaredTo(hqLoc) >= 8)
            nav.goTo(hqLoc);


        buryEnemyBuilding();

        // see which of the locations has the smallest amount of dirt on it (so we can put dirt there)
        int minDirt = Integer.MAX_VALUE;
        MapLocation minDirtLocation = null;
        for (MapLocation loc : candidateTiles) {
            if (rc.senseElevation(loc) < minDirt) {
                minDirtLocation = loc;
                minDirt = rc.senseElevation(loc);
            }
        }

        // if there is a tile around the HQ (but not adjacent to landscaper) with dirt < (min dirt in adjacent tiles) - 10, then go in that direction
        for (Direction dir : Util.directions) {
            MapLocation loc = hqLoc.add(dir);
            if (rc.canSenseLocation(loc) && rc.senseElevation(loc) < minDirt - 10) {
                Direction dirToLoc = currentLoc.directionTo(loc);
                nav.tryMove(dirToLoc);
                nav.tryMove(dirToLoc.rotateLeft());
                nav.tryMove(dirToLoc.rotateRight());
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

            if (currentLoc.isAdjacentTo(hqLoc)) {
                // finally, dig dirt from direction opposite HQ
                // Note: Don't dig dirt from corners - only N, E, S, W
                switch (dirToHQ) {
                    case NORTH: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                    case SOUTH: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                    case EAST: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                    case WEST: if (rc.canDigDirt(dirToHQ.opposite())) rc.digDirt(dirToHQ.opposite());
                    default: 
                        if (rc.canDigDirt(dirToHQ.rotateRight().rotateRight())) 
                            rc.digDirt(dirToHQ.rotateRight().rotateRight());
                        else if (rc.canDigDirt(dirToHQ.rotateLeft().rotateLeft()))
                            rc.digDirt(dirToHQ.rotateLeft().rotateLeft());
                }
            }

            // otherwise dig some dirt
            if (rc.canDigDirt(dirToHQ.opposite()))
                rc.digDirt(dirToHQ.opposite());
            if (rc.canDigDirt(dirToHQ.opposite().rotateRight()))
                rc.digDirt(dirToHQ.opposite().rotateRight());
            if (rc.canDigDirt(dirToHQ.opposite().rotateLeft()))
                rc.digDirt(dirToHQ.opposite().rotateLeft());
        }
    }

    void buryEnemyBuilding () throws GameActionException {
        System.out.println("Looking for enemy buildings to bury");
        // put dirt on enemy building with the most dirt already on it
        RobotInfo [] enemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        int maxDirt = 0;
        RobotInfo bestTarget = null;
        for (RobotInfo enemy : enemies) {
            if (enemy.type == RobotType.VAPORATOR || enemy.type == RobotType.DESIGN_SCHOOL 
                || enemy.type == RobotType.FULFILLMENT_CENTER || enemy.type == RobotType.NET_GUN) {
                if (enemy.dirtCarrying >= maxDirt) {
                    bestTarget = enemy;
                    maxDirt = enemy.dirtCarrying;
                }
            }
        }
        MapLocation currentLoc = rc.getLocation();
        // if there is a target but not carrying dirt, then dig some up
        if (bestTarget != null && rc.getDirtCarrying() == 0) {
            MapLocation targetLoc = bestTarget.location;
            if (enemyHQ != null && rc.getLocation().isAdjacentTo(enemyHQ) && rc.canDigDirt(Direction.CENTER))
                rc.digDirt(Direction.CENTER);
            else if (rc.canDigDirt(targetLoc.directionTo(currentLoc)))
                rc.digDirt(targetLoc.directionTo(currentLoc));
            else if (rc.canDigDirt(targetLoc.directionTo(currentLoc).rotateRight()))
                rc.digDirt(targetLoc.directionTo(currentLoc).rotateRight());
            else if (rc.canDigDirt(targetLoc.directionTo(currentLoc).rotateLeft()))
                rc.digDirt(targetLoc.directionTo(currentLoc).rotateLeft());
        }

        if (bestTarget != null && rc.canDepositDirt(currentLoc.directionTo(bestTarget.location))) {
            System.out.println("Burying building at " + bestTarget.location);
            rc.depositDirt(rc.getLocation().directionTo(bestTarget.location));
        }
    }
}