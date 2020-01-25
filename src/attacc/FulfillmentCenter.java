package attacc;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
    boolean hasBuiltDrone = false;
    int soupPreviousTurn = 0;

    public FulfillmentCenter(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // in order to build defensive drone, reset hasBuiltDrone on proteccRound
        if (rc.getRoundNum() == proteccRound)
            hasBuiltDrone = false;
        
        // water reaches height 5 on turn 1210
        if (hasBuiltDrone && (rc.getRoundNum() <= 1210 || soupPreviousTurn <= 250)) {
            soupPreviousTurn = rc.getTeamSoup();
            return;
        }
        System.out.println("looking for nearby robots!");
        // find the nearby miner (hopefully exists and is unique)
        RobotInfo[] neighbors = rc.senseNearbyRobots(2);
        for (RobotInfo robot : neighbors)
            if (robot.getType() == RobotType.MINER && robot.getTeam() == rc.getTeam()){
                Direction dirToMiner = rc.getLocation().directionTo(robot.getLocation());
                // preferentially build on tiles that are flooded or at elevation > 3 + miner elevation
                MapLocation rightLoc = rc.getLocation().add(dirToMiner.rotateRight());
                if ((rc.senseFlooding(rightLoc) 
                        || Math.abs(rc.senseElevation(rightLoc) - rc.senseElevation(rc.getLocation().add(dirToMiner))) > 3)
                        && tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateRight()))
                    hasBuiltDrone = true;
                if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateLeft()))
                    hasBuiltDrone = true;
                if (tryBuild(RobotType.DELIVERY_DRONE, dirToMiner.rotateRight()))
                    hasBuiltDrone = true;
            }

        // if there are no miners nearby (probably if we're building defensive drone), build in direction of our HQ
        if (hqLoc != null) {
            MapLocation currentLoc = rc.getLocation();
            Direction dirToHQ = currentLoc.directionTo(hqLoc);
            if (tryBuild(RobotType.DELIVERY_DRONE, dirToHQ) || tryBuild(RobotType.DELIVERY_DRONE, dirToHQ.rotateLeft())
                    || tryBuild(RobotType.DELIVERY_DRONE, dirToHQ.rotateRight()))
                hasBuiltDrone = true;
        }
        soupPreviousTurn = rc.getTeamSoup();
    }
}
