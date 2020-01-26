package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class DeliveryDrone extends Unit {
    boolean offense = false;

    boolean hasTransportedMiner = false;
    boolean hasReceivedEnemyHQLocations = false;
    boolean hasRequestedElevator = true; // this gets reset when we pick up miner
    MapLocation elevatorLocation = null;

    // whether to go spin up or spin down around enemy HQ
    boolean spinUp = true;

    MapLocation [] waterTiles = new MapLocation[100];
    int waterTileCounter = 0;

    ArrayList<MapLocation> netGuns = new ArrayList<MapLocation>();

    public DeliveryDrone(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        offense = offense || rc.getRoundNum() < proteccRound; // attack if created before protection round

        if (rc.isReady()) 
            recentLocs[rc.getRoundNum() % 5] = rc.getLocation();

        // add enemy net guns (and HQ) to list of net guns
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if ((robot.type == RobotType.HQ || robot.type == RobotType.NET_GUN) && robot.team != rc.getTeam()) {
                if (!netGuns.contains(robot.location)) netGuns.add(robot.location);
            }
        }
        // also remove net guns from list if they are gone/buried
        for (MapLocation loc : netGuns) {
            if (rc.canSenseLocation(loc)) {
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if (robot == null || robot.team == rc.getTeam() || (robot.type != RobotType.NET_GUN && robot.type != RobotType.HQ))
                    netGuns.remove(loc);
            }
        }

        // add any flooded tiles in vision radius to waterTiles (except duplicates)
        int n = (int)(Math.sqrt(rc.getCurrentSensorRadiusSquared()));
        MapLocation currentLoc = rc.getLocation();
        for (int x = -n; x <= n; x ++) {
            for (int y = -n; y <= n; y ++) {
                if (Clock.getBytecodesLeft() > 5000) {
                    MapLocation loc = new MapLocation(currentLoc.x + x, currentLoc.y + y);
                    if (rc.canSenseLocation(loc) && rc.senseFlooding(loc))
                        waterTiles[waterTileCounter++ % 100] = loc;
                }
            }
        }

        if (offense)
            offensiveDrone();
        else
            defensiveDrone();
    }

    public void offensiveDrone() throws GameActionException {
        if (!hasTransportedMiner)
            transportMiner();
        else
            annoyEnemy();
    }

    // orbit our wall, yoink enemies off it, and yeet them into the water
    public void defensiveDrone () throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        if (!rc.isCurrentlyHoldingUnit()) {
            // if in range of an enemy unit or cow, pick it up
            RobotInfo [] adjacentEnemies = rc.senseNearbyRobots(2);
            for (RobotInfo robot : adjacentEnemies) 
                if (robot.team != rc.getTeam() && rc.canPickUpUnit(robot.ID))
                    rc.pickUpUnit(robot.ID);

            // if wall has missing places, put a landscaper on one of them (if possible)
            int landscapersMissing = 0;
            for (Direction dir : Util.directions) {
                MapLocation loc = hqLoc.add(dir);
                if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc))
                    landscapersMissing ++;
            }

            RobotInfo [] adjacentFriends = rc.senseNearbyRobots(2, rc.getTeam());
            if (landscapersMissing > 0) {
                for (RobotInfo robot : adjacentFriends)
                    if (robot.type == RobotType.LANDSCAPER && !robot.location.isAdjacentTo(hqLoc) && rc.canPickUpUnit(robot.ID))
                        rc.pickUpUnit(robot.ID);
            }

            // if there is a miner who has done its job, pick it up
            for (RobotInfo robot : adjacentFriends) {
                if (robot.type == RobotType.MINER && (robot.location.distanceSquaredTo(hqLoc) == 5 || robot.location.distanceSquaredTo(hqLoc) == 8)
                        && rc.canPickUpUnit(robot.ID)) {
                    int occupiedSquaresNearMiner = 0;
                    for (Direction dir : Util.directions) {
                        MapLocation locAdjacentToMiner = robot.location.add(dir);
                        if (rc.canSenseLocation(locAdjacentToMiner) 
                                && (hqLoc.distanceSquaredTo(locAdjacentToMiner) == 5 || hqLoc.distanceSquaredTo(locAdjacentToMiner) == 8)) {
                            RobotInfo robotNextToMiner = rc.senseRobotAtLocation(locAdjacentToMiner);
                            if (robotNextToMiner != null && robotNextToMiner.team == rc.getTeam()
                                    && (robotNextToMiner.type == RobotType.NET_GUN || robotNextToMiner.type == RobotType.FULFILLMENT_CENTER
                                    || robotNextToMiner.type == RobotType.LANDSCAPER || robotNextToMiner.type == RobotType.DESIGN_SCHOOL))
                                occupiedSquaresNearMiner ++;
                        }
                    }
                    if (occupiedSquaresNearMiner >= 2) {
                        rc.pickUpUnit(robot.ID);
                        hasRequestedElevator = false;
                    }
                }
            }

            // orbit HQ as a sentry
            orbitHQ();

            // reverse orbital direction if needed
            checkIfStuck();
            if (isStuck)
                spinUp = !spinUp;
        } else {
            // determine if the unit is friend or foe (cow = foe here)
            RobotInfo me = rc.senseRobotAtLocation(currentLoc);
            if (rc.senseRobot(me.heldUnitID).team != rc.getTeam()) {
                MapLocation nearestWater = null;
                int nearestWaterDistance = Integer.MAX_VALUE;
                for (MapLocation loc : waterTiles) {
                    if (loc != null && !currentLoc.equals(loc) && currentLoc.distanceSquaredTo(loc) < nearestWaterDistance) {
                        nearestWater = loc;
                        nearestWaterDistance = currentLoc.distanceSquaredTo(loc);
                    }
                }
                // TODO: Do something more intelligent here
                if (nearestWater != null) {
                    if (currentLoc.isAdjacentTo(nearestWater) && rc.canDropUnit(currentLoc.directionTo(nearestWater)))
                        rc.dropUnit(currentLoc.directionTo(nearestWater));
                    nav.goTo(nearestWater);
                } else
                    nav.goTo(new MapLocation((int)(rc.getMapWidth() * Math.random()), (int)(rc.getMapHeight() * Math.random())));
            } else if (rc.senseRobot(me.heldUnitID).type == RobotType.LANDSCAPER) {
                System.out.println("Carrying friendly landscaper to place on wall");
                // find missing wall location to put unit on
                for (Direction dir : Util.directions) {
                    MapLocation loc = currentLoc.add(dir);
                    System.out.println(loc);
                    if (rc.canSenseLocation(loc) && !rc.senseFlooding(loc) && loc.isAdjacentTo(hqLoc) && rc.canDropUnit(dir))
                        rc.dropUnit(dir);
                }
                if (rc.isCurrentlyHoldingUnit()) // didn't succeed in dropping it
                    orbitHQ();
            } else if (rc.senseRobot(me.heldUnitID).type == RobotType.MINER) {
                // go to a place where the miner will have its own corner
                MapLocation loc1, loc2, loc3, loc4, loc5, loc6, loc7, loc8, loc9, loc10, loc11, loc12;
                loc1 = hqLoc.translate(1,2);
                loc2 = hqLoc.translate(2,1);
                loc3 = hqLoc.translate(-1,2);
                loc4 = hqLoc.translate(-2,1);
                loc5 = hqLoc.translate(1,-2);
                loc6 = hqLoc.translate(2,-1);
                loc7 = hqLoc.translate(-1,-2);
                loc8 = hqLoc.translate(-2,-1);
                loc9 = hqLoc.translate(2,2);
                loc10 = hqLoc.translate(-2,2);
                loc11 = hqLoc.translate(2,-2);
                loc12 = hqLoc.translate(-2,-2);
                // for the drone, only two locations need be unoccupied
                // also don't drop the miner in the corner (will get sniped)
                System.out.println(hasRequestedElevator);
                if (!hasRequestedElevator) {
                    if (rc.canSenseLocation(loc1) && rc.canSenseLocation(loc2) && rc.canSenseLocation(loc9)
                            && (!rc.isLocationOccupied(loc1) && !rc.isLocationOccupied(loc2)
                            ||  !rc.isLocationOccupied(loc1) && !rc.isLocationOccupied(loc9)
                            ||  !rc.isLocationOccupied(loc2) && !rc.isLocationOccupied(loc9))
                            && (currentLoc.isAdjacentTo(loc1) || currentLoc.isAdjacentTo(loc2))) {
                        MapLocation [] locsToElevate = {loc1, loc2, loc9};
                        if (comms.requestElevator(locsToElevate)) hasRequestedElevator = true;
                        if (currentLoc.isAdjacentTo(loc1)) elevatorLocation = loc1;
                        if (currentLoc.isAdjacentTo(loc1)) elevatorLocation = loc2;
                    }
                    else if (rc.canSenseLocation(loc3) && rc.canSenseLocation(loc4) && rc.canSenseLocation(loc10)
                            && (!rc.isLocationOccupied(loc3) && !rc.isLocationOccupied(loc4)
                            ||  !rc.isLocationOccupied(loc3) && !rc.isLocationOccupied(loc10)
                            ||  !rc.isLocationOccupied(loc4) && !rc.isLocationOccupied(loc10))
                            && (currentLoc.isAdjacentTo(loc3) || currentLoc.isAdjacentTo(loc4))) {
                        MapLocation [] locsToElevate = {loc3, loc4, loc10};
                        if (comms.requestElevator(locsToElevate)) hasRequestedElevator = true;
                        if (currentLoc.isAdjacentTo(loc3)) elevatorLocation = loc3;
                        if (currentLoc.isAdjacentTo(loc4)) elevatorLocation = loc4;
                    }
                    else if (rc.canSenseLocation(loc5) && rc.canSenseLocation(loc6) && rc.canSenseLocation(loc11)
                            && (!rc.isLocationOccupied(loc5) && !rc.isLocationOccupied(loc6)
                            ||  !rc.isLocationOccupied(loc5) && !rc.isLocationOccupied(loc11)
                            ||  !rc.isLocationOccupied(loc6) && !rc.isLocationOccupied(loc11))
                            && (currentLoc.isAdjacentTo(loc5) || currentLoc.isAdjacentTo(loc6))) {
                        MapLocation [] locsToElevate = {loc5, loc6, loc11};
                        if (comms.requestElevator(locsToElevate)) hasRequestedElevator = true;
                        if (currentLoc.isAdjacentTo(loc5)) elevatorLocation = loc5;
                        if (currentLoc.isAdjacentTo(loc6)) elevatorLocation = loc6;
                    }
                    else if (rc.canSenseLocation(loc7) && rc.canSenseLocation(loc8) && rc.canSenseLocation(loc12)
                            && (!rc.isLocationOccupied(loc7) && !rc.isLocationOccupied(loc8)
                            ||  !rc.isLocationOccupied(loc7) && !rc.isLocationOccupied(loc12)
                            ||  !rc.isLocationOccupied(loc8) && !rc.isLocationOccupied(loc12))
                            && (currentLoc.isAdjacentTo(loc7) || currentLoc.isAdjacentTo(loc8))) {
                        MapLocation [] locsToElevate = {loc7, loc8, loc12};
                        if (comms.requestElevator(locsToElevate)) hasRequestedElevator = true;
                        if (currentLoc.isAdjacentTo(loc7)) elevatorLocation = loc7;
                        if (currentLoc.isAdjacentTo(loc8)) elevatorLocation = loc8;
                    }
                    else
                        orbitHQ();
                } else {
                    if (elevatorLocation != null && rc.canSenseLocation(elevatorLocation) && rc.senseElevation(elevatorLocation) >= 20
                            && !rc.senseFlooding(elevatorLocation))
                        if (rc.canDropUnit(currentLoc.directionTo(elevatorLocation)))
                            rc.dropUnit(currentLoc.directionTo(elevatorLocation));
                }
            }
        }
    }

    public void orbitHQ() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        Direction dirToHQ = currentLoc.directionTo(hqLoc);
        if (spinUp) {
            tryMove(dirToHQ.rotateRight());
            tryMove(dirToHQ.rotateRight().rotateRight());
            tryMove(dirToHQ.rotateRight().rotateRight().rotateRight());
        } else {
            tryMove(dirToHQ.rotateLeft());
            tryMove(dirToHQ.rotateLeft().rotateLeft());
            tryMove(dirToHQ.rotateLeft().rotateLeft().rotateLeft());
        }
        // as a last resort, if no enemies are nearby, move away from HQ
        if (rc.senseNearbyRobots(8, rc.getTeam().opponent()).length == 0)
            tryMove(dirToHQ.opposite());
    }


    public void checkIfStuck() {
        // can't move for first 10 turns anyway, so don't check until then
        if (turnCount > 15) {
            // if soup is stuck at the same amount and >= 3 locations in recentLocs match current one
            // then robot is stuck
            int locationMatches = 0;
            for (int counter = 0; counter < 5; counter ++)
                if (rc.getRoundNum() % 5 != counter && recentLocs[counter] != null && recentLocs[counter].equals(rc.getLocation()))
                    locationMatches ++;
            isStuck = (locationMatches >= 2);
            if (isStuck)
                System.out.println("Woe is I; I'm stuck!");
        }
    }

    public void transportMiner() throws GameActionException {
        if (!hasReceivedEnemyHQLocations) {
            enemyHQPossibilities = comms.receiveEnemyHQ();
            hasReceivedEnemyHQLocations = true;
        }
        MapLocation currentLoc = rc.getLocation();
        if (rc.isCurrentlyHoldingUnit()){

            // try to find enemy HQ
            RobotInfo [] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                    enemyHQ = robot.location;
                    currentDir = currentLoc.directionTo(robot.location);
                    System.out.println("Found enemy HQ!");
                }
            }
            // if only one possible location left and four squares away from that, then this is the enemy HQ
            if (enemyHQPossibilities.size() == 1 && currentLoc.distanceSquaredTo(enemyHQPossibilities.get(0)) <= 20) {
                enemyHQ = enemyHQPossibilities.get(0);
                currentDir = currentLoc.directionTo(enemyHQ);
            }

            // if in sight of enemy HQ, release payload
            if (enemyHQ != null) {
                if (rc.canDropUnit(currentDir) && !rc.senseFlooding(currentLoc.add(currentDir))) {
                    rc.dropUnit(currentDir);
                    hasTransportedMiner = true;
                }
                if (rc.canDropUnit(currentDir.rotateRight()) && !rc.senseFlooding(currentLoc.add(currentDir.rotateRight()))) {
                    rc.dropUnit(currentDir.rotateRight());
                    hasTransportedMiner = true;
                }
                if (rc.canDropUnit(currentDir.rotateLeft()) && !rc.senseFlooding(currentLoc.add(currentDir.rotateLeft()))) {
                    rc.dropUnit(currentDir.rotateLeft());
                    hasTransportedMiner = true;
                }
                return;
            }

            // if you can see that nearest enemy HQ possibility is empty, remove it from list
            if (rc.canSenseLocation(getNearestEnemyHQPossibility())) {
                RobotInfo [] possibleHQ = rc.senseNearbyRobots(getNearestEnemyHQPossibility(), 0, rc.getTeam().opponent());
                if (!(possibleHQ.length > 0 && possibleHQ[0].type == RobotType.HQ))
                    enemyHQPossibilities.remove(getNearestEnemyHQPossibility());
            }


            // otherwise, try to go to the next possible enemy HQ location
            if (!(rc.getLocation().equals(getNearestEnemyHQPossibility())))
                goTo(getNearestEnemyHQPossibility());
            // if already at enemy HQ location, then there is nothing there, so we have the wrong spot
            else
                enemyHQPossibilities.remove(getNearestEnemyHQPossibility()); // probably redundant now
        } else {
            // pick up nearby miner
            System.out.println("looking for nearby robots!");
            // find the nearby miner (hopefully exists and is unique)
            RobotInfo[] neighbors = rc.senseNearbyRobots(2);
            for (RobotInfo robot : neighbors)
                if (robot.getType() == RobotType.MINER && robot.getTeam() == rc.getTeam() && rc.canPickUpUnit(robot.getID()))
                    rc.pickUpUnit(robot.getID());
        }
    }

    // yoink enemy units and yeet them into the water
    public void annoyEnemy() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        if (rc.isCurrentlyHoldingUnit()) {
            MapLocation nearestWater = null;
            int nearestWaterDistance = Integer.MAX_VALUE;
            for (MapLocation loc : waterTiles) {
                if (loc != null && !currentLoc.equals(loc) && currentLoc.distanceSquaredTo(loc) < nearestWaterDistance) {
                    nearestWater = loc;
                    nearestWaterDistance = currentLoc.distanceSquaredTo(loc);
                }
            }
            // TODO: Do something more intelligent here
            if (nearestWater != null) {
                if (currentLoc.isAdjacentTo(nearestWater) && rc.canDropUnit(currentLoc.directionTo(nearestWater)))
                    rc.dropUnit(currentLoc.directionTo(nearestWater));
                goTo(nearestWater);
            } else
                goTo(new MapLocation((int)(rc.getMapWidth() * Math.random()), (int)(rc.getMapHeight() * Math.random())));
        }
        // if in range of an enemy unit, pick it up
        RobotInfo [] adjacentEnemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        for (RobotInfo robot : adjacentEnemies) 
            if (rc.canPickUpUnit(robot.ID))
                rc.pickUpUnit(robot.ID);

        // otherwise, circle enemy HQ looking for fresh meat
        if (currentLoc.distanceSquaredTo(enemyHQ) > 25)
            goTo(enemyHQ);
        else {
            Direction dirToHQ = currentLoc.directionTo(enemyHQ);
            Direction moveDir = dirToHQ;
            for (int counter = 0; counter < 4; counter ++) {
                if (!(currentLoc.add(moveDir).distanceSquaredTo(enemyHQ) > 13 && tryMove(moveDir))) {
                    if (spinUp)
                        moveDir = moveDir.rotateRight();
                    else
                        moveDir = moveDir.rotateLeft();
                }
            }
            // DO NOT try moveDir.rotate[wrong way](); this will bring us within net gun range
            checkIfStuck();
            if (isStuck)
                spinUp = !spinUp;
        }
    }

    // go to location while avoiding enemy HQ
    // TODO: Also avoid enemy net guns
    public void goTo(MapLocation loc) throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        Direction dir = currentLoc.directionTo(loc);
        if (canMove(dir))
            rc.move(dir);
        else if (canMove(dir.rotateLeft()))
            rc.move(dir.rotateLeft());
        else if (canMove(dir.rotateRight()))
            rc.move(dir.rotateRight());
        else if (canMove(dir.rotateLeft().rotateLeft()))
            rc.move(dir.rotateLeft().rotateLeft());
        else if (canMove(dir.rotateRight().rotateRight()))
            rc.move(dir.rotateRight().rotateRight());
    }

    // test if drone can move in direction and there is no net gun that would shoot it down
    // NOTE: If enemy builds net gun right next to our drone, this would immobilize us
    // To correct for this, this method will return true if we are currently in range of a net gun
    // However, we should never move within range of enemy HQ (net gun might be new, but HQ is not)
    public boolean canMove(Direction dir) throws GameActionException {
        if (!rc.canMove(dir))
            return false;
        MapLocation currentLoc = rc.getLocation();
        MapLocation newLoc = currentLoc.add(dir);

        if (enemyHQ != null && newLoc.distanceSquaredTo(enemyHQ) <= 13) return false;

        for (MapLocation gun : netGuns) {
            if (currentLoc.distanceSquaredTo(gun) <= 13) return true;
            if (newLoc.distanceSquaredTo(gun) <= 13) return false;
        }
        return true;
    }

    public boolean tryMove(Direction dir) throws GameActionException {
        if (canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }
}
