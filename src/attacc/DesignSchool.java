package attacc;
import battlecode.common.*;

public class DesignSchool extends Building {
    int soupPreviousTurn = 0;
    int landscapersBuilt = 0;
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
        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 16 && soupPreviousTurn < 150
            || (landscapersBuilt >= 8 && soupPreviousTurn < 250)
            || (landscapersBuilt >= 12 && soupPreviousTurn < 1000)) {
            soupPreviousTurn = rc.getTeamSoup();
            // if you see at least one net gun already, then you can build more landscapers as long as soupPreviousTurn >= 150
            RobotInfo [] nearbyRobots = rc.senseNearbyRobots();
            boolean nearbyNetGun = false;
            for (RobotInfo robot : nearbyRobots) {
                if (robot.type == RobotType.NET_GUN && robot.team == rc.getTeam())
                    nearbyNetGun = true;
            }
            if ((!nearbyNetGun || soupPreviousTurn <= 150) || (landscapersBuilt >= 12 && soupPreviousTurn < 1000))
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

        
        if (Math.random() < 0.5) {
            if (tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight()) || tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft()))
                landscapersBuilt ++;
        } else {
            if (tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft()) || tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight()))
                landscapersBuilt ++;
        }
        if (tryBuild(RobotType.LANDSCAPER, currentDir) || tryBuild(RobotType.LANDSCAPER, currentDir.rotateRight().rotateRight())
                || tryBuild(RobotType.LANDSCAPER, currentDir.rotateLeft().rotateLeft()))
            landscapersBuilt ++;

        // for defense, keep trying other squares as well
        if (rc.getLocation().distanceSquaredTo(hqLoc) <= 16) {
            if (tryBuild(RobotType.LANDSCAPER, currentDir.opposite().rotateLeft()) 
                    || tryBuild(RobotType.LANDSCAPER, currentDir.opposite().rotateRight())
                    || tryBuild(RobotType.LANDSCAPER, currentDir.opposite()))
                landscapersBuilt ++;
        }
        soupPreviousTurn = rc.getTeamSoup();
    }
}
