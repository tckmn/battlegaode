package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Miner extends Unit {

    MapLocation targetLoc = null; // maybe this goes in superclass?
    MapLocation designSchoolLoc = null;

    int minersBuilt = 0;
    boolean hasBuiltDesignSchool = false;
    boolean hasBuiltFulfillmentCenter = false;
    boolean hasBuiltRefinery = false;
    boolean hasTransmittedEnemyHQLocs = false;
    boolean hasBuiltNetGun = false;
    boolean hasRequestedElevator = false;
    int designSchoolTurnBuilt = -1;
    MapLocation nearestEnemyHQPossibility = null;

    boolean firstMiner = false;
    boolean secondMiner = false;

    boolean useBetterNav = false;

    MapLocation refineryLoc = null;

    boolean[] dirsTried = {false, false, false, false};
    Direction[] dirsToCheck = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};

    // places to go for net gun defense
    MapLocation [] locsToCheck;
    boolean [] locsTried = {false, false, false, false, false, false, false, false};

    boolean proteccMode = false;

    public Miner(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        if (refineryLoc == null && hqLoc != null)
            refineryLoc = hqLoc;
        if (!refineryLoc.equals(hqLoc) && rc.canSenseLocation(refineryLoc)) {
            RobotInfo putativeRefinery = rc.senseRobotAtLocation(refineryLoc);
            if (putativeRefinery == null || putativeRefinery.team != rc.getTeam()
                    || putativeRefinery.type != RobotType.HQ)
                refineryLoc = null;
        }

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
        if (secondMiner)
            proteccMode = proteccMode || (rc.getRoundNum() >= proteccRound
                    || rc.getTeamSoup() >= 200 && (canSenseEnemy(RobotType.LANDSCAPER)
                    || (rc.getRoundNum() >= attaccRound && rc.getTeamSoup() + rc.getRoundNum() * earlyProtecc >= proteccRound * earlyProtecc)));
        if (firstMiner && (rc.getRoundNum() < proteccRound || enemyHQ != null)) // just get soup after turn 200 unless found enemy HQ
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
        else if (secondMiner && proteccMode)
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
            MapLocation currentLoc = rc.getLocation();
            if (!currentLoc.equals(annoyingLoc)) {
                System.out.println("Move to annoying location");
                if (currentLoc.isAdjacentTo(enemyHQ) && !currentLoc.isAdjacentTo(designSchoolLoc))
                    nav.tryMove(currentLoc.directionTo(annoyingLoc));
                else
                    nav.goTo(annoyingLoc, true);
            }
            else if (!hasBuiltNetGun && 
                (canSenseEnemy(RobotType.DELIVERY_DRONE) || (rc.getRoundNum() > 13 + designSchoolTurnBuilt))) {
                System.out.println("Build net gun in annoying location");
                Direction dir = rc.getLocation().directionTo(enemyHQ);
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
        // This should never happen
        /*
        if (isStuck) {
            if (enemyHQ == null) {
                if (!hasBuiltFulfillmentCenter) {
                    Direction dir = rc.getLocation().directionTo(getNearestEnemyHQPossibility());
                    if (tryBuild(RobotType.FULFILLMENT_CENTER, dir) || tryBuild(RobotType.FULFILLMENT_CENTER, dir.rotateRight())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.rotateLeft())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.rotateRight().rotateRight())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.rotateLeft().rotateLeft())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.opposite().rotateLeft())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.opposite().rotateRight())
                            || tryBuild(RobotType.FULFILLMENT_CENTER, dir.opposite())) {
                        System.out.println("Building fulfillment center");
                        hasBuiltFulfillmentCenter = true;
                        hasTransmittedEnemyHQLocs = comms.transmitEnemyHQ(enemyHQPossibilities);
                    }
                }
                return;
            }
        }
        */

        if (nearestEnemyHQPossibility == null)
            nearestEnemyHQPossibility = getNearestEnemyHQPossibility();

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
        if (notHere) {
            enemyHQPossibilities.remove(nextTarget);
            nearestEnemyHQPossibility = getNearestEnemyHQPossibility();
        }
        
        nav.goTo(nearestEnemyHQPossibility);

        if (nav.tryMove(currentDir))
          System.out.println("I moved!");
        else
          currentDir = Util.randomDirection();
    }


    
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

    void tryBuildRefinery(Direction dir) throws GameActionException {
        if(rc.getLocation().equals(hqLoc.add(dir).add(dir)))
            if(tryBuild(RobotType.REFINERY, dir) || tryBuild(RobotType.REFINERY, dir.rotateRight()) ||tryBuild(RobotType.REFINERY, dir.rotateRight().rotateRight())
                    || tryBuild(RobotType.REFINERY, dir.rotateLeft()) || tryBuild(RobotType.REFINERY, dir.rotateLeft().rotateLeft())) {
                hasBuiltRefinery = true;
                MapLocation [] temp = {hqLoc.translate(1,2),
                    hqLoc.translate(-1,2),
                    hqLoc.translate(-2,1),
                    hqLoc.translate(2,1),
                    hqLoc.translate(2,-1),
                    hqLoc.translate(-2,-1),
                    hqLoc.translate(-1,-2),
                    hqLoc.translate(1,-2)};
                locsToCheck = temp;
            }
    }
    

    //pseudo code for new minerProtecc method  
    void minerProtecc() throws GameActionException {
        if (rc.getRoundNum() >= 2000 && !(rc.getLocation().isAdjacentTo(hqLoc)))
            return; // don't build things after turn 2000; they just get flooded very soon
        if (!hasBuiltDesignSchool) {
            System.out.println("In protection mode; trying to build defensive design school");
            for(int counter = 0; counter < 4; counter++)
            {
                if (!dirsTried[counter]) {
                    checkDir(counter, true);
                    break;
                }
            }
            // if all directions fail, then reset
            if (dirsTried[3]) {
                boolean [] temp = {false, false, false, false};
                dirsTried = temp;
            }
        } else if (!hasBuiltRefinery) {
            System.out.println("Built defensive design school; now trying to build refinery");
            for(int counter = 0; counter < 4; counter++)
            {
                if (!dirsTried[counter]) {
                    checkDir(counter, false);
                    break;
                }
            }
            // if all directions fail, then reset
            if (dirsTried[3]) {
                boolean [] temp = {false, false, false, false};
                dirsTried = temp;
            }
        } else {
            // start building defensive net guns
            MapLocation currentLoc = rc.getLocation();
            if (currentLoc.distanceSquaredTo(hqLoc) != 5) {
                for (int counter = 0; counter < 8; counter ++) {
                    if (!locsTried[counter]) {
                        checkLoc(counter);
                        break;
                    }
                }
            }
            if (rc.senseElevation(currentLoc) >= ledgeHeight) {
                // see if there is an adjacent empty tile not adjacent to HQ with elevation >= ledgeHeight
                boolean hasBuiltDefensiveNetGun = false;
                MapLocation placeForNetGun = null;
                for (Direction dir : Util.directions) {
                    MapLocation newLoc = currentLoc.add(dir);
                    if (rc.canSenseLocation(newLoc)) {
                        RobotInfo robotAtNewLoc = rc.senseRobotAtLocation(newLoc);
                        if (robotAtNewLoc != null && robotAtNewLoc.type == RobotType.NET_GUN && robotAtNewLoc.team == rc.getTeam())
                            hasBuiltDefensiveNetGun = true;
                        int newElevation = rc.senseElevation(newLoc);
                        if (newElevation >= ledgeHeight && robotAtNewLoc == null && Math.abs(newElevation - rc.senseElevation(currentLoc)) <= 3)
                            placeForNetGun = newLoc;
                    }
                }
                System.out.println("Trying to build things at location " + placeForNetGun);
                if (placeForNetGun != null) {
                    hasBuiltDefensiveNetGun = hasBuiltDefensiveNetGun || tryBuild(RobotType.NET_GUN, currentLoc.directionTo(placeForNetGun));
                    if (hasBuiltDefensiveNetGun) {
                        hasBuiltFulfillmentCenter = hasBuiltFulfillmentCenter 
                            || tryBuild(RobotType.FULFILLMENT_CENTER, currentLoc.directionTo(placeForNetGun));
                        tryBuild(RobotType.DESIGN_SCHOOL, currentLoc.directionTo(placeForNetGun));
                    }
                }
            } else if (!hasRequestedElevator) {
                MapLocation [] locsToElevate = {currentLoc, new MapLocation(-10,-10), new MapLocation(-10,-10)};
                int elevatorCounter = 1;
                boolean landscaperInPlace = false;
                for (Direction dir : Util.directions) {
                    MapLocation newLoc = currentLoc.add(dir);
                    if ((newLoc.distanceSquaredTo(hqLoc) == 5 || newLoc.distanceSquaredTo(hqLoc) == 8) && elevatorCounter < 3)
                        locsToElevate[elevatorCounter ++] = newLoc;
                    if (newLoc.distanceSquaredTo(hqLoc) == 2) {
                        RobotInfo robot = rc.senseRobotAtLocation(newLoc);
                        if (robot != null && robot.type == RobotType.LANDSCAPER && robot.team == rc.getTeam())
                            landscaperInPlace = true;
                    }
                }
                // TODO: Also make sure there is a landscaper that can hear this request
                hasRequestedElevator = landscaperInPlace && comms.requestElevator(locsToElevate);
            }
        }
    }

    void checkDir(int counter, boolean designSchool) throws GameActionException{
        Direction dir = dirsToCheck[counter];
        System.out.println("Checking direction " + dir);
        MapLocation loc = hqLoc.add(dir).add(dir);
        if (!(rc.onTheMap(loc))) {
            dirsTried[counter] = true;
            return;
        }
        if (!rc.getLocation().equals(loc)){
            nav.goTo(loc);
            // recheck to see if still stuck
            checkIfStuck();
            if(isStuck || nav.useBugNav) 
                dirsTried[counter] = true;
        } else if (rc.isReady() && rc.getTeamSoup() >= 150) {
            if (designSchool)
                tryBuildDefensiveDesignSchool(dir);
            else
                tryBuildRefinery(dir);
            dirsTried[counter] = true;
        }
    }

    void checkLoc(int counter) throws GameActionException{
        MapLocation loc = locsToCheck[counter];
        if (!(rc.onTheMap(loc))) {
            locsTried[counter] = true;
            return;
        }
        if (!rc.getLocation().equals(loc)){
            nav.goTo(loc);
            // recheck to see if still stuck
            checkIfStuck();
            if(isStuck || nav.useBugNav) 
                locsTried[counter] = true;
        }
    }

    void minerGetSoup() throws GameActionException {
        // if you see a refinery, add that to list of refinery locations
        RobotInfo [] nearbyBots = rc.senseNearbyRobots();
        for (RobotInfo bot : nearbyBots) {
            if (bot.type == RobotType.REFINERY && bot.team == rc.getTeam())
                refineryLoc = bot.location;
        }

        //System.out.println("Design school is built; now just search for soup");
        System.out.println("Last soup mined at " + lastSoupMined);
        System.out.println("Current soup carrying: " + rc.getSoupCarrying());
        // if adjacent to HQ and many landscapers nearby, get away from HQ so landscapers can move in
        // move into water if necessary to get out of the way
        if (rc.getRoundNum() > proteccRound && rc.getLocation().distanceSquaredTo(hqLoc) <= 2) {
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
            if (rc.getSoupCarrying() >= 98)
                if (tryRefine(dir))
                    targetLoc = null;
            if (tryMine(dir)) lastSoupMined = rc.getLocation().add(dir);
            if (tryRefine(dir)) targetLoc = null;
        }
        

        // if reached home, set target loc back to null
        if (rc.getLocation().isAdjacentTo(hqLoc) && hqLoc.equals(targetLoc))
            targetLoc = null;
        // if target loc no longer has soup, set target loc to null
        if (targetLoc != null && !(targetLoc.equals(hqLoc) || targetLoc.equals(refineryLoc)) && rc.canSenseLocation(targetLoc)
            && rc.senseSoup(targetLoc) == 0)
            targetLoc = null;
        // also zero out lastSoupMined when empty
        if (lastSoupMined != null && rc.canSenseLocation(lastSoupMined) && rc.senseSoup(lastSoupMined) == 0) {
            boolean outOfSoup = true;
            for (Direction dir : Util.directions) {
                MapLocation locToCheck = lastSoupMined.add(dir);
                outOfSoup = outOfSoup && !rc.onTheMap(locToCheck) || (rc.canSenseLocation(locToCheck) && rc.senseSoup(locToCheck) == 0);
            }
            if (outOfSoup) lastSoupMined = null;
        }

        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit){
            System.out.println("Has enough soup; going home");
            targetLoc = refineryLoc;
        }
        // if stuck, go home, unless already at home
        if (isStuck){
            if (rc.getLocation().isAdjacentTo(hqLoc))
                targetLoc = null;
            else
                targetLoc = refineryLoc;
        }

        MapLocation soupLoc = findNearestSoup();
        if (targetLoc != refineryLoc && soupLoc != null && !isStuck) {
            targetLoc = soupLoc;
        }

        checkIfStuck();

        if (targetLoc != null && nav.goTo(targetLoc)){
            
        }
        // disintegrate if in the way of defensive wall
        // TODO: also make sure there is landscaper nearby
        else if (isStuck && rc.getRoundNum() > emergencyProteccRound-5 && (rc.getSoupCarrying() == 0 || rc.getRoundNum() >= 1210)
                && (rc.getLocation().isAdjacentTo(hqLoc) 
                || (rc.getLocation().distanceSquaredTo(hqLoc) <= 8 && rc.senseElevation(rc.getLocation()) >= ledgeHeight))) {
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
        // if it can't find soup, go to last location where it found soup (if it exists) or move randomly
        else if (lastSoupMined != null) {
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
        if (rc.isReady() && rc.canDepositSoup(dir) && rc.senseRobotAtLocation(rc.getLocation().add(dir)).team == rc.getTeam()) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }
}
