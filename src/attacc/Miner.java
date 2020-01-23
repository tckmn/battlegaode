package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Miner extends Unit {

    MapLocation targetLoc = null; // maybe this goes in superclass?
    MapLocation designSchoolLoc = null;

    int minersBuilt = 0;
    boolean hasBuiltDesignSchool = false;
    boolean hasBuiltFulfillmentCenter = false;
    boolean hasTransmittedEnemyHQLocs = false;
    boolean hasBuiltNetGun = false;
    int designSchoolTurnBuilt = -1;

    boolean firstMiner = false;
    boolean secondMiner = false;

    boolean useBetterNav = false;

    public Miner(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // maybe these should go in constructor
        if (rc.getRoundNum() == 2) {
            firstMiner = true;
            System.out.println("This is the first miner!");
        }
        if (rc.getRoundNum() == 3 && !firstMiner) {
            secondMiner = true;
            System.out.println("This is the second miner!");
        }

        recentSoup[rc.getRoundNum() % 5] = rc.getSoupCarrying();
        if (rc.isReady()) 
            recentLocs[rc.getRoundNum() % 5] = rc.getLocation();


        checkIfStuck();
        if (firstMiner)
            minerAttacc();
        // conditions for defense:
        // * round number >= proteccRound (250)
        // * sees an enemy landscaper in range
        // getTeamSoup() + getroundNum() * 3 > 750
        // Last condition means that we always protect in round 250
        // and start protecting in turn 150 if soup > 300 (more than net gun price)

        // modification: Don't go into early protection mode unless we have at least 200 soup
        // We need to focus on attack first since we are fundamentally a rush bot
        // 200 means will take precedence over net guns but not landscapers
        else if (secondMiner && !hasBuiltDesignSchool && (rc.getRoundNum() >= proteccRound
            || rc.getTeamSoup() >= 200 && (canSenseEnemy(RobotType.LANDSCAPER)
            || (rc.getRoundNum() >= attaccRound && rc.getTeamSoup() + rc.getRoundNum() * earlyProtecc >= proteccRound * earlyProtecc))))
            minerProtecc();
        else
            minerGetSoup();
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
            int currentSoup = rc.getSoupCarrying();
            for (int pastSoup : recentSoup){
                if (pastSoup != currentSoup) {
                    isStuck = false;
                }
            }
            if (isStuck)
                System.out.println("Woe is I; I'm stuck!");
        }
    }

    void minerAttacc() throws GameActionException {

        // try to find enemy HQ
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);
                System.out.println("Found enemy HQ!");
            }
        }


        // if adjacent to enemy HQ, build a design studio and then do nothing else
        if (enemyHQ != null && rc.getLocation().isAdjacentTo(enemyHQ)) {
            if (!hasBuiltDesignSchool) {
                // if we see a drone factory (fulfillment center), build a net gun

                if (rc.getTeamSoup() >= 150) {
                    if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateRight())) {
                        hasBuiltDesignSchool = true;
                        designSchoolLoc = rc.getLocation().add(currentDir.rotateRight());
                        designSchoolTurnBuilt = rc.getRoundNum();
                    }
                    if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateLeft())) {
                        hasBuiltDesignSchool = true;
                        designSchoolLoc = rc.getLocation().add(currentDir.rotateLeft());
                        designSchoolTurnBuilt = rc.getRoundNum();
                    }
                    // only try locations that are also adjacent to enemy HQ
                    if (currentDir == Direction.NORTH || currentDir == Direction.SOUTH || currentDir == Direction.EAST || currentDir == Direction.WEST) {
                        if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateRight().rotateRight())) {
                            hasBuiltDesignSchool = true;
                            designSchoolLoc = rc.getLocation().add(currentDir.rotateRight().rotateRight());
                            designSchoolTurnBuilt = rc.getRoundNum();
                        }
                        if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateLeft().rotateLeft())) {
                            hasBuiltDesignSchool = true;
                            designSchoolLoc = rc.getLocation().add(currentDir.rotateLeft().rotateLeft());
                            designSchoolTurnBuilt = rc.getRoundNum();
                        }
                    }
                }
                return;
            } 
        }

        if (hasBuiltDesignSchool) {
            System.out.println("Already built design school, now annoy opponent");
            MapLocation annoyingLoc = enemyHQ.add(enemyHQ.directionTo(designSchoolLoc).opposite());
            // TODO: Make this loop structure better
            // build the net gun in suboptimal location if you see an enemy drone
            if (canSenseEnemy(RobotType.DELIVERY_DRONE) && !hasBuiltNetGun) {
                System.out.println("Build net gun in potentially suboptimal location");
                Direction dir = rc.getLocation().directionTo(enemyHQ);
                if (tryBuild(RobotType.NET_GUN, dir)
                 || tryBuild(RobotType.NET_GUN, dir.rotateLeft())
                 || tryBuild(RobotType.NET_GUN, dir.rotateRight())
                 || tryBuild(RobotType.NET_GUN, dir.rotateLeft().rotateLeft())
                 || tryBuild(RobotType.NET_GUN, dir.rotateRight().rotateRight()))
                    hasBuiltNetGun = true;
            }
            if (!rc.getLocation().equals(annoyingLoc)) {
                System.out.println("Move to annoying location");
                nav.goTo(annoyingLoc);
            }
            else if (!hasBuiltNetGun && 
                (canSenseEnemy(RobotType.DELIVERY_DRONE) || (rc.getRoundNum() > 13 + designSchoolTurnBuilt))) {
                System.out.println("Build net gun in annoying location");
                Direction dir = rc.getLocation().directionTo(enemyHQ);
                MapLocation currentLoc = rc.getLocation();
                // Note: The maximally annoying locations are those not eligible to be taken by our landscapers
                if (currentLoc.add(dir.rotateLeft().rotateLeft()).isAdjacentTo(enemyHQ) && tryBuild(RobotType.NET_GUN, dir.rotateLeft().rotateLeft())
                    || currentLoc.add(dir.rotateRight().rotateRight()).isAdjacentTo(enemyHQ) && tryBuild(RobotType.NET_GUN, dir.rotateRight().rotateRight())
                    || currentLoc.add(dir.rotateLeft()).isAdjacentTo(enemyHQ) && tryBuild(RobotType.NET_GUN, dir.rotateLeft())
                    || currentLoc.add(dir.rotateRight()).isAdjacentTo(enemyHQ) && tryBuild(RobotType.NET_GUN, dir.rotateRight())
                    || currentLoc.add(dir).isAdjacentTo(enemyHQ) && tryBuild(RobotType.NET_GUN, dir))
                    hasBuiltNetGun = true;
            }
            return;
        }

        if (hasBuiltFulfillmentCenter && !hasTransmittedEnemyHQLocs) {
            hasTransmittedEnemyHQLocs = comms.transmitEnemyHQ(enemyHQPossibilities);
        }

        // if stuck, build a drone factory and then stop moving
        // NOTE: If you're stuck very close to enemy HQ, don't do this since they'll just shoot drones down
        // Being stuck very close to enemy HQ is probably due to enemy workers who will just move out of the way
        if (isStuck) {
            if (enemyHQ == null) {
                if (!hasBuiltFulfillmentCenter) {
                    for (Direction dir : Util.directions) {
                        if (tryBuild(RobotType.FULFILLMENT_CENTER, dir)) {
                            System.out.println("Building fulfillment center");
                            hasBuiltFulfillmentCenter = true;
                            hasTransmittedEnemyHQLocs = comms.transmitEnemyHQ(enemyHQPossibilities);
                        }
                    }
                }
                return;
            }
            else
                useBetterNav = true;
        }


        // if we can see there is nothing at enemyHQPossiblities.get(0), remove from list
        // otherwise go there
        // we can sense at a distance, so no need to physically walk there just to see that it's empty
        MapLocation nextTarget = getNearestEnemyHQPossibility();
        boolean notHere;
        if (rc.canSenseLocation(nextTarget)) {
            RobotInfo robot = rc.senseRobotAtLocation(nextTarget);
            if (robot == null || !(robot.type == RobotType.HQ && robot.team != rc.getTeam()))
                notHere = true;
            else
                notHere = false;
        } else {
            notHere = false;
        }
        if (notHere)
            enemyHQPossibilities.remove(nextTarget);
        
        if (useBetterNav)
            nav.navTo(getNearestEnemyHQPossibility());
        else
            nav.navTo(getNearestEnemyHQPossibility()); // may have changed due to removal

        if (nav.tryMove(currentDir))
          System.out.println("I moved!");
        else
          currentDir = Util.randomDirection();
    }

    boolean[] dirsTried = {false, false, false, false};
    Direction[] dirsToCheck = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};

    
    void tryBuildDefensiveDesignSchool(Direction dir) throws GameActionException {
        if(rc.getLocation().equals(hqLoc.add(dir).add(dir)))
            if(tryBuild(RobotType.DESIGN_SCHOOL, dir))
                hasBuiltDesignSchool = true;
            else if (tryBuild(RobotType.DESIGN_SCHOOL, dir.rotateRight()))
                hasBuiltDesignSchool = true;
            else if (tryBuild(RobotType.DESIGN_SCHOOL, dir.rotateRight().rotateRight()))
                hasBuiltDesignSchool = true;
            else if (tryBuild(RobotType.DESIGN_SCHOOL, dir.rotateLeft()))
                hasBuiltDesignSchool = true;
            else if (tryBuild(RobotType.DESIGN_SCHOOL, dir.rotateLeft().rotateLeft()))
                hasBuiltDesignSchool = true;
    }
    

    //pseudo code for new minerProtecc method  
    void minerProtecc() throws GameActionException {
        System.out.println("In protection mode; trying to build defensive design school");
        for(int counter = 0; counter < 4; counter++)
        {
            if (!dirsTried[counter]) {
                checkDir(counter);
                break;
            }
        }
    }

    void checkDir(int counter) throws GameActionException{
        Direction dir = dirsToCheck[counter];
        MapLocation loc = hqLoc.add(dir).add(dir);
        if (!(rc.onTheMap(loc))) {
            dirsTried[counter] = true;
            return;
        }
        if (!rc.getLocation().equals(loc)){
            nav.goTo(loc);
            // recheck to see if still stuck
            checkIfStuck();
            if(isStuck) 
                dirsTried[counter] = true;
        } else if (rc.isReady() && rc.getTeamSoup() >= 150) {
            tryBuildDefensiveDesignSchool(dir);
            dirsTried[counter] = true;
        }
    }

    void minerGetSoup() throws GameActionException {
        //System.out.println("Design school is built; now just search for soup");
        System.out.println("Current soup carrying: " + rc.getSoupCarrying());
        // if adjacent to HQ and many landscapers nearby, get away from HQ so landscapers can move in
        // move into water if necessary to get out of the way
        if (rc.getRoundNum() > proteccRound && rc.getLocation().distanceSquaredTo(hqLoc) <= 8) {
            RobotInfo [] nearbyBots = rc.senseNearbyRobots();
            int landscaperCount = 0;
            for (RobotInfo robot : nearbyBots)
                if (robot.type == RobotType.LANDSCAPER && robot.getTeam() == rc.getTeam())
                    landscaperCount ++;
            if (landscaperCount >= 8) {
                Direction dir = rc.getLocation().directionTo(hqLoc).opposite();
                if (rc.canMove(dir))
                    rc.move(dir);
                if (rc.canMove(dir.rotateLeft()))
                    rc.move(dir.rotateLeft());
                if (rc.canMove(dir.rotateRight()))
                    rc.move(dir.rotateRight());
            }
        }

        for (Direction dir : Util.directions)
        {
            tryMine(dir);
            tryRefine(dir);
        }
        

        // if reached home, set target loc back to null
        if (rc.getLocation().isAdjacentTo(hqLoc) && hqLoc.equals(targetLoc))
            targetLoc = null;
        // if target loc no longer has soup, set target loc to null
        if (targetLoc != null && !(targetLoc.equals(hqLoc)) && rc.canSenseLocation(targetLoc)
            && rc.senseSoup(targetLoc) == 0)
            targetLoc = null;

        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit){
            System.out.println("Has enough soup; going home");
            targetLoc = hqLoc;
        }
        // if stuck, go home, unless already at home
        if (isStuck){
            if (rc.getLocation().isAdjacentTo(hqLoc))
                targetLoc = null;
            else
                targetLoc = hqLoc;
        }

        if (targetLoc != null){
            nav.goTo(targetLoc);
            return;
        }

        // disintegrate if in the way of defensive wall
        // TODO: also make sure there is landscaper nearby
        if (isStuck && rc.getRoundNum() > emergencyProteccRound-5 && rc.getSoupCarrying() == 0 && rc.getLocation().isAdjacentTo(hqLoc)) {
            System.out.println("Stuck and in the way -- probably providing negative utility to team");
            // only run away if there are nearby landscapers
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2, rc.getTeam());
            boolean adjacentLandscaper = false;
            for (RobotInfo robot : nearbyRobots)
                if (robot.type == RobotType.LANDSCAPER)
                    adjacentLandscaper = true;
            Direction dirAwayFromHQ = rc.getLocation().directionTo(hqLoc).opposite();
            if (adjacentLandscaper && !(nav.tryMove(dirAwayFromHQ) || nav.tryMove(dirAwayFromHQ.rotateLeft()) || nav.tryMove(dirAwayFromHQ.rotateRight())
                || nav.tryMove(dirAwayFromHQ.rotateLeft().rotateLeft()) || nav.tryMove(dirAwayFromHQ.rotateRight().rotateRight())))
                rc.disintegrate();
        }

        // This is wasteful in terms of bytecodes but hopefully we have plenty
        // TODO: Replace this with findNearestSoup (above)
        MapLocation myLoc = rc.getLocation();
        MapLocation soupLoc = findNearestSoup();
        if (soupLoc != null && !isStuck) {
            targetLoc = soupLoc;
            nav.goTo(soupLoc);
            return;
        }
        /*
        for (int n = 1; n <= 5; n ++) {
            for (int x = -n; x <= n; x ++){
                for (int y = -n; y <= n; y ++) {
                    MapLocation possibleLoc = myLoc.translate(x,y);
                    System.out.println("Is there soup at " + possibleLoc + "?");
                    if (rc.canSenseLocation(possibleLoc) && rc.senseSoup(possibleLoc) > 0) {
                        // go to that location and break out of this loop
                        System.out.println("Found soup; now going to " + possibleLoc);
                        targetLoc = possibleLoc;
                        nav.goTo(targetLoc);
                        return;
                    }
                }
            }
        }
        */
        // if it can't find soup, go to last location where it found soup (if it exists) or move randomly
        if (lastSoupMined != null) {
            System.out.println("Going to last soup mined");
            nav.goTo(lastSoupMined);
        } else {
            // try to follow nearest other miner (maybe they are smarter than we are)
            RobotInfo [] nearbyRobots = rc.senseNearbyRobots();
            int minDistance = Integer.MAX_VALUE;
            MapLocation nearestMiner = null;
            for (RobotInfo robot : nearbyRobots) {
                if (robot.type == RobotType.MINER && rc.getLocation().distanceSquaredTo(robot.getLocation()) < minDistance) {
                    minDistance = rc.getLocation().distanceSquaredTo(robot.getLocation());
                    nearestMiner = robot.getLocation();
                }
            }
            if (nearestMiner != null) {
                System.out.println("Couldn't find soup; following nearest miner");
                nav.goTo(nearestMiner);
            } else {
                System.out.println("Couldn't find soup; moving randomly");
                nav.tryMove();
            }
        }

    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }
}
