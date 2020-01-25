package attacc;
import battlecode.common.*;

public class DesignSchool extends Building {
    int soupPreviousTurn = 0;
    public DesignSchool(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        
        currentDir = rc.getLocation().directionTo(hqLoc); // for defensive design school; this gets overridden on offense
        RobotInfo [] robots = rc.senseNearbyRobots();

        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ && robot.team != rc.getTeam()) {
                enemyHQ = robot.location;
                currentDir = rc.getLocation().directionTo(robot.location);

                System.out.println("Found enemy HQ!");
            }
        }

        // defensive design school should lose race conditions by delaying everything one turn
        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 16 && soupPreviousTurn < 150) {
            soupPreviousTurn = rc.getTeamSoup();
            return;
        }

        // if we see an enemy drone but no friendly net gun, stop building landscapers
        boolean stopBuilding = false;
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.DELIVERY_DRONE && robot.team != rc.getTeam())
                stopBuilding = true;
        }
        for (RobotInfo robot : robots) {
            if ((robot.type == RobotType.NET_GUN || robot.type == RobotType.HQ)
                    && robot.team == rc.getTeam())
                stopBuilding = false;
        }
        if (stopBuilding) return;

        tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight());
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft());
        tryBuild(RobotType.LANDSCAPER, currentDir);
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight().rotateRight());
        tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft().rotateLeft());

        // for defense, keep trying other squares as well
        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 16) {
            tryBuild(RobotType.LANDSCAPER, currentDir.opposite().rotateLeft());
            tryBuild(RobotType.LANDSCAPER, currentDir.opposite().rotateRight());
            tryBuild(RobotType.LANDSCAPER, currentDir.opposite());
        }
        
        soupPreviousTurn = rc.getTeamSoup();
    }
}
