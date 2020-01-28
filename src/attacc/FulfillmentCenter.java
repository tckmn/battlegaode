package attacc;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
    int dronesBuilt = 0;
    int soupPreviousTurn = 0;

    public FulfillmentCenter(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // in order to build defensive drone, reset hasBuiltDrone on proteccRound
        if (rc.getRoundNum() == proteccRound)
            dronesBuilt = 0;
        
        // water reaches height 5 on turn 1210
        // build 2 defensive drones
        if (dronesBuilt >= 2 && (rc.getRoundNum() <= 1210 || soupPreviousTurn <= 250)) {
            soupPreviousTurn = rc.getTeamSoup();
            return;
        }
        System.out.println("looking for nearby robots!");
        // find the nearby miner (hopefully exists and is unique)
        RobotInfo[] neighbors = rc.senseNearbyRobots(2);
        if (rc.getRoundNum() < proteccRound) {
            for (RobotInfo robot : neighbors)
                if (robot.getType() == RobotType.MINER && robot.getTeam() == rc.getTeam()){
                    Direction dirToMiner = rc.getLocation().directionTo(robot.getLocation());
                    // preferentially build on tiles that are flooded or at elevation > 3 + miner elevation
                    MapLocation rightLoc = rc.getLocation().add(dirToMiner.rotateRight());
                    if (rc.canSenseLocation(rightLoc) && (rc.senseFlooding(rightLoc) 
                            || Math.abs(rc.senseElevation(rightLoc) - rc.senseElevation(rc.getLocation().add(dirToMiner))) > 3)
                            && tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateRight()))
                        dronesBuilt ++;
                    if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateLeft()))
                        dronesBuilt ++;
                    if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateRight()))
                        dronesBuilt ++;
                }
        } else {
            for (Direction dir : Util.directions)
                if (tryBuild(RobotType.DELIVERY_DRONE, dir))
                    dronesBuilt ++;
        }

        // if there are no miners nearby (probably if we're building defensive drone), build in direction of our HQ
        if (hqLoc != null) {
            MapLocation currentLoc = rc.getLocation();
            Direction dirToHQ = currentLoc.directionTo(hqLoc);
            if (tryBuild(RobotType.DELIVERY_DRONE, dirToHQ) || tryBuild(RobotType.DELIVERY_DRONE, dirToHQ.rotateLeft())
                    || tryBuild(RobotType.DELIVERY_DRONE, dirToHQ.rotateRight()))
                dronesBuilt ++;
        }
        soupPreviousTurn = rc.getTeamSoup();
    }
}
