package protecc;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    static int hqMessageNumber = 18527549;

    static int maxMiners = 10;
    static int currentMiners = 0;

    static int soupTarget = 150*9;

    static MapLocation hqLoc;

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
                if (hqLoc == null) findHQ();
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
        while (roundNumber < turnCount) {
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
    }

    static void runHQ() throws GameActionException {
        // on turn 1, broadcast location
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
        if (currentMiners < maxMiners)
            for (Direction dir : directions)
                if(tryBuild(RobotType.MINER, dir))
                    currentMiners ++;
    }

    static void runMiner() throws GameActionException {
        // once we hit the soup target, go 4 squares below HQ and build a design studio
        if (rc.getTeamSoup() > soupTarget) {
            if(rc.getLocation().equals(hqLoc.translate(0,-4)))
                tryBuild(RobotType.DESIGN_SCHOOL, Direction.NORTH);
            tryMove(rc.getLocation().directionTo(hqLoc.translate(0,-4)));
        }

        for (Direction dir : directions)
            if (tryMine(dir))
                System.out.println("I mined soup! " + rc.getSoupCarrying());
        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        // when full, go home to refine
        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit)
            tryMove(rc.getLocation().directionTo(hqLoc));

        tryMove(randomDirection());
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        // build landscapers, preferentially in the north
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.NORTH))
            rc.buildRobot(RobotType.LANDSCAPER, Direction.NORTH);
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.NORTHEAST))
            rc.buildRobot(RobotType.LANDSCAPER, Direction.NORTHEAST);
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.NORTHWEST))
            rc.buildRobot(RobotType.LANDSCAPER, Direction.NORTHWEST);
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.EAST))
            rc.buildRobot(RobotType.LANDSCAPER, Direction.EAST);
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.WEST))
            rc.buildRobot(RobotType.LANDSCAPER, Direction.WEST);
        
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
        // try to walk to north side of HQ
        MapLocation currentLoc = rc.getLocation();
        if (currentLoc.translate(0,1).isAdjacentTo(hqLoc))
            tryMove(Direction.NORTH);
        if (currentLoc.translate(1,1).isAdjacentTo(hqLoc))
            tryMove(Direction.NORTHEAST);
        if (currentLoc.translate(-1,1).isAdjacentTo(hqLoc))
            tryMove(Direction.NORTHWEST);

        // if still not adjacent to HQ, try to move in random direction
        if (!currentLoc.isAdjacentTo(hqLoc)) {
            tryMove(randomDirection());
            return;
        }

        // if HQ has dirt on it, remove dirt
        Direction dirToHQ = currentLoc.directionTo(hqLoc);
        if (rc.canDigDirt(dirToHQ))
            rc.digDirt(dirToHQ);
        // TODO: second-best thing: put dirt on enemy building in range
        // build wall
        if (rc.canDepositDirt(Direction.CENTER))
            rc.depositDirt(Direction.CENTER);
        // finally, dig dirt from direction opposite HQ
        if (rc.canDigDirt(dirToHQ.opposite()))
            rc.digDirt(dirToHQ.opposite());
    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
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
        if (rc.isReady() && rc.canMove(dir)) {
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
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
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
}
