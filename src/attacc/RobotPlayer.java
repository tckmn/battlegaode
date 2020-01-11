package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST
    };
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int hqMessageNumber = 18527548;

    static int turnCount;
    static MapLocation hqLoc;

    static MapLocation enemyHQ = new MapLocation(-10,-10);
    static Direction currentDir = randomDirection();

    static ArrayList<MapLocation> enemyHQPossibilities = new ArrayList<MapLocation>(3);
    static MapLocation targetLoc = null;

    static int minersBuilt = 0;
    static boolean hasBuiltDesignSchool = false;
    static boolean hasBuiltFulfillmentCenter = false;
    static boolean hasBuiltDrone = false;
    static boolean hasTransportedMiner = false;

    static boolean firstMiner = false;

    static MapLocation [] recentLocs = new MapLocation[5];
    static int [] recentSoup = new int[5];
    static boolean isStuck = false;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                // if the robot is created on turn 2, then it is the first miner
                if (rc.getRoundNum() == 2) {
                    firstMiner = true;
                    System.out.println("This is the first miner!");
                }
                //System.out.println("Cooldown left: " + rc.getCooldownTurns());
                findHQ();
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void findHQ() throws GameActionException {
        // read the blockchain until we find the HQ
        // this should only have to read round 1
        int roundNumber = 1;
        while (roundNumber < rc.getRoundNum()) {
            Transaction [] block = rc.getBlock(roundNumber);
            for (Transaction t : block)
            {
                int [] message = t.getMessage();
                if(message[0] == hqMessageNumber)
                {
                    hqLoc = new MapLocation(message[1], message[2]);
                    System.out.println("Found HQ location");
                    break;
                }
            }
            roundNumber++;
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

    // don't actually use this method - just save for debugging
    static void findHQOld() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                }
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

            // TODO later: use blockchain to communicate
            // idea: HQ broadcasts code and location on turn 1, all units check for the special code
        }
    }

    static void runHQ() throws GameActionException {
        if (turnCount == 1) {
            MapLocation loc = rc.getLocation();
            int [] message = new int[7];
            message[0] = hqMessageNumber;
            message[1] = loc.x;
            message[2] = loc.y;
            for (int i = 3; i < 7; i ++) message[i] = 0;
            if (rc.canSubmitTransaction(message, 1))
                rc.submitTransaction(message, 1);
        }
        if (rc.getTeamSoup() >= 60 && minersBuilt < 3)
            for (Direction dir : directions)
                if(tryBuild(RobotType.MINER, dir))
                    minersBuilt ++;
    }

    static void runMiner() throws GameActionException {
        recentSoup[rc.getRoundNum() % 5] = rc.getSoupCarrying();
        recentLocs[rc.getRoundNum() % 5] = rc.getLocation();
        // can't move for first 10 turns anyway, so don't check until then
        if (turnCount > 15) {
            // if soup is stuck at the same amount and >= 3 locations in recentLocs match current one
            // then robot is stuck
            int locationMatches = 0;
            for (MapLocation loc : recentLocs)
                if (loc.equals(rc.getLocation()))
                    locationMatches ++;
            isStuck = (locationMatches >= 3);
            int currentSoup = rc.getSoupCarrying();
            for (int pastSoup : recentSoup){
                if (pastSoup != currentSoup) {
                    isStuck = false;
                }
            }
        }
        if (isStuck)
            System.out.println("Woe is I; I'm stuck!");
        if (firstMiner && !hasBuiltDesignSchool)
            minerAttacc();
        else
            minerGetSoup();
    }

    static void minerAttacc() throws GameActionException {


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
        if (rc.getLocation().isAdjacentTo(enemyHQ)) {
            if (rc.getTeamSoup() >= 150) {
                if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateRight()))
                    hasBuiltDesignSchool = true;
                if(tryBuild(RobotType.DESIGN_SCHOOL, currentDir.rotateLeft()))
                    hasBuiltDesignSchool = true;
            }
            return;
        }

        // if stuck, build a drone factory and then stop moving
        if (isStuck) {
            if (!hasBuiltFulfillmentCenter)
                for (Direction dir : directions)
                    if (tryBuild(RobotType.FULFILLMENT_CENTER, dir))
                        hasBuiltFulfillmentCenter = true;
            return;
        }

        // otherwise, try to go to the next possible enemy HQ location
        if (!(rc.getLocation().equals(enemyHQPossibilities.get(0))))
            goTo(enemyHQPossibilities.get(0));
        // if already at enemy HQ location, then there is nothing there, so we have the wrong spot
        else
            enemyHQPossibilities.remove(0);

        if (tryMove(currentDir))
          System.out.println("I moved!");
        else
          currentDir = randomDirection();
    }

    static void minerGetSoup() throws GameActionException {
        System.out.println("Design school is built; now just search for soup");
        System.out.println("Current soup carrying: " + rc.getSoupCarrying());
        for (Direction dir : directions)
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
        // if stuck, go home
        if (isStuck)
            targetLoc = hqLoc;

        if (targetLoc != null){
            goTo(targetLoc);
            return;
        }
        // TODO: try to sense soup at all visible locations
        MapLocation myLoc = rc.getLocation();
        for (int x = -5; x <= 5; x ++){
            for (int y = -5; y <= 5; y ++) {
                MapLocation possibleLoc = myLoc.translate(x,y);
                System.out.println("Is there soup at " + possibleLoc + "?");
                if (rc.canSenseLocation(possibleLoc) && rc.senseSoup(possibleLoc) > 0) {
                    // go to that location and break out of this loop
                    System.out.println("Found soup; now going to " + possibleLoc);
                    targetLoc = possibleLoc;
                    goTo(targetLoc);
                    return;
                }
            }
        }
        System.out.println("Couldn't find soup; going home");
        goTo(hqLoc);
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);
                System.out.println("Found enemy HQ!");
            }
        }
        tryBuild(RobotType.LANDSCAPER, currentDir);
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight());
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft());
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight().rotateRight());
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft().rotateLeft());
    }

    static void runFulfillmentCenter() throws GameActionException {
        if (hasBuiltDrone)
            return;
        System.out.println("looking for nearby robots!");
        // find the nearby miner (hopefully exists and is unique)
        RobotInfo[] neighbors = rc.senseNearbyRobots(2);
        for (RobotInfo robot : neighbors)
            if (robot.getType() == RobotType.MINER && robot.getTeam() == rc.getTeam()){
                Direction dirToMiner = rc.getLocation().directionTo(robot.getLocation());
                if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateLeft()))
                    hasBuiltDrone = true;
                if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateRight()))
                    hasBuiltDrone = true;
            }
    }

    static void runLandscaper() throws GameActionException {
        // find enemy HQ
        RobotInfo [] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);
                System.out.println("Found enemy HQ!");
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

    static void runDeliveryDrone() throws GameActionException {
        if (rc.isCurrentlyHoldingUnit()){

            // try to find enemy HQ
            RobotInfo [] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                    enemyHQ = robot.location;
                    currentDir = rc.getLocation().directionTo(robot.location);
                    System.out.println("Found enemy HQ!");
                }
            }
            // if adjacent to enemy HQ, release payload
            if (rc.getLocation().isAdjacentTo(enemyHQ)) {
                if (rc.canDropUnit(currentDir.rotateRight()))
                {
                    rc.dropUnit(currentDir.rotateRight());
                    hasTransportedMiner = true;
                }
                if (rc.canDropUnit(currentDir.rotateLeft())) {
                    rc.dropUnit(currentDir.rotateLeft());
                    hasTransportedMiner = true;
                }
                return;
            }

            // otherwise, try to go to the next possible enemy HQ location
            if (!(rc.getLocation().equals(enemyHQPossibilities.get(0))))
                goTo(enemyHQPossibilities.get(0));
            // if already at enemy HQ location, then there is nothing there, so we have the wrong spot
            else
                enemyHQPossibilities.remove(0);
        } else if (!hasTransportedMiner) {
            // pick up nearby miner
            System.out.println("looking for nearby robots!");
            // find the nearby miner (hopefully exists and is unique)
            RobotInfo[] neighbors = rc.senseNearbyRobots(2);
            for (RobotInfo robot : neighbors)
                if (robot.getType() == RobotType.MINER && robot.getTeam() == rc.getTeam()){
                    rc.pickUpUnit(robot.getID());
                }
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir) && (!rc.senseFlooding(rc.getLocation().add(dir)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
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
    // modified to make sure that we don't dump soup in enemy refinery
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir) 
            && rc.senseRobotAtLocation(rc.getLocation().add(dir)).getTeam() == rc.getTeam()) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }

    // tries to move in the general direction of dir (from lecturePlayer)
    static boolean goTo(Direction dir) throws GameActionException {
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    static boolean goTo(MapLocation destination) throws GameActionException {
        System.out.println("Trying to go to " + destination);
        return goTo(rc.getLocation().directionTo(destination));
    }
}
