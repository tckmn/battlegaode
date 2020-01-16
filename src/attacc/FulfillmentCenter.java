package attacc;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
    boolean hasBuiltDrone = false;

    public FulfillmentCenter(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        
        if (hasBuiltDrone)
            return;
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
    }
}
