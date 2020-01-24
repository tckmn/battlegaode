package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class DeliveryDrone extends Unit {
    boolean offense = false;

    boolean hasTransportedMiner = false;
    boolean hasReceivedEnemyHQLocations = false;

    // whether to go spin up or spin down around enemy HQ
    boolean spinUp = true;

    MapLocation [] waterTiles = new MapLocation[100];
    int waterTileCounter = 0;

    public DeliveryDrone(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        offense = offense || rc.getRoundNum() < proteccRound; // attack if created before protection round

        if (rc.isReady()) 
            recentLocs[rc.getRoundNum() % 5] = rc.getLocation();

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
            // if in range of an enemy unit, pick it up
            RobotInfo [] adjacentEnemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
            for (RobotInfo robot : adjacentEnemies) 
                if (rc.canPickUpUnit(robot.ID))
                    rc.pickUpUnit(robot.ID);

            // if wall has missing places, put a landscaper on one of them (if possible)
            int landscapersMissing = 0;
            for (Direction dir : Util.directions) {
                MapLocation loc = hqLoc.add(dir);
                if (rc.canSenseLocation(loc) && !rc.isLocationOccupied(loc))
                    landscapersMissing ++;
            }
            if (landscapersMissing > 0) {
                RobotInfo [] adjacentFriends = rc.senseNearbyRobots(2, rc.getTeam());
                for (RobotInfo robot : adjacentFriends)
                    if (robot.type == RobotType.LANDSCAPER && !robot.location.isAdjacentTo(hqLoc) && rc.canPickUpUnit(robot.ID))
                        rc.pickUpUnit(robot.ID);
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
            } else {
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
            }
        }
    }

    public void orbitHQ() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        Direction dirToHQ = currentLoc.directionTo(hqLoc);
        if (spinUp) {
            nav.tryMove(dirToHQ.rotateRight());
            nav.tryMove(dirToHQ.rotateRight().rotateRight());
            nav.tryMove(dirToHQ.rotateRight().rotateRight().rotateRight());
        } else {
            nav.tryMove(dirToHQ.rotateLeft());
            nav.tryMove(dirToHQ.rotateLeft().rotateLeft());
            nav.tryMove(dirToHQ.rotateLeft().rotateLeft().rotateLeft());
        }
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
                nav.goTo(getNearestEnemyHQPossibility());
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
                cautiouslyGoTo(nearestWater);
            } else
                cautiouslyGoTo(new MapLocation((int)(rc.getMapWidth() * Math.random()), (int)(rc.getMapHeight() * Math.random())));
        }
        // if in range of an enemy unit, pick it up
        RobotInfo [] adjacentEnemies = rc.senseNearbyRobots(2, rc.getTeam().opponent());
        for (RobotInfo robot : adjacentEnemies) 
            if (rc.canPickUpUnit(robot.ID))
                rc.pickUpUnit(robot.ID);

        // otherwise, circle enemy HQ looking for fresh meat
        if (currentLoc.distanceSquaredTo(enemyHQ) > 25)
            cautiouslyGoTo(enemyHQ);
        else {
            Direction dirToHQ = currentLoc.directionTo(enemyHQ);
            Direction moveDir = dirToHQ;
            for (int counter = 0; counter < 4; counter ++) {
                if (!(currentLoc.add(moveDir).distanceSquaredTo(enemyHQ) > 13 && nav.tryMove(moveDir))) {
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
    public void cautiouslyGoTo(MapLocation loc) throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        Direction dir = currentLoc.directionTo(loc);
        if (rc.canMove(dir) && currentLoc.add(dir).distanceSquaredTo(enemyHQ) > 13)
            rc.move(dir);
        else if (rc.canMove(dir.rotateLeft()) && currentLoc.add(dir.rotateLeft()).distanceSquaredTo(enemyHQ) > 13)
            rc.move(dir.rotateLeft());
        else if (rc.canMove(dir.rotateRight()) && currentLoc.add(dir.rotateRight()).distanceSquaredTo(enemyHQ) > 13)
            rc.move(dir.rotateRight());
        else if (rc.canMove(dir.rotateLeft().rotateLeft()) && currentLoc.add(dir.rotateLeft().rotateLeft()).distanceSquaredTo(enemyHQ) > 13)
            rc.move(dir.rotateLeft().rotateLeft());
        else if (rc.canMove(dir.rotateRight().rotateRight()) && currentLoc.add(dir.rotateRight().rotateRight()).distanceSquaredTo(enemyHQ) > 13)
            rc.move(dir.rotateRight().rotateRight());
    }
}
