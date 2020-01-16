package attacc;
import battlecode.common.*;

public class DeliveryDrone extends Unit {
    static boolean hasTransportedMiner = false;

    public DeliveryDrone(RobotController r) {
        super(r);
    }
//testing ignore this
    public void takeTurn() throws GameActionException {
        super.takeTurn();

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
            if (enemyHQ != null && rc.getLocation().isAdjacentTo(enemyHQ)) {
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
            if (!(rc.getLocation().equals(getNearestEnemyHQPossibility())))
                nav.goTo(getNearestEnemyHQPossibility());
            // if already at enemy HQ location, then there is nothing there, so we have the wrong spot
            else
                enemyHQPossibilities.remove(getNearestEnemyHQPossibility());
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
}