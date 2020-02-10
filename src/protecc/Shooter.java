package protecc;
import battlecode.common.*;

public class Shooter extends Building {

    public Shooter(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        shoot();
    }

    public void shoot() throws GameActionException {
        // shoot nearby enemies
        MapLocation currentLoc = rc.getLocation();
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] enemiesInRange = rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, enemy);

        for (RobotInfo e : enemiesInRange) {
            for (int n = 1; n <= 4; n ++) {
                if (e.type == RobotType.DELIVERY_DRONE && currentLoc.distanceSquaredTo(e.location) < n*n
                        && e.currentlyHoldingUnit && rc.senseRobot(e.heldUnitID).team == rc.getTeam().opponent()) {
                    if (rc.canShootUnit(e.ID)){
                        rc.shootUnit(e.ID);
                        return;
                    }
                }
            }
        }

        for (RobotInfo e : enemiesInRange) {
            for (int n = 1; n <= 4; n ++) {
                if (e.type == RobotType.DELIVERY_DRONE && currentLoc.distanceSquaredTo(e.location) < n*n) {
                    if (rc.canShootUnit(e.ID)){
                        rc.shootUnit(e.ID);
                        return;
                    }
                }
            }
        }
    }
}