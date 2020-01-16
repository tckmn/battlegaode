package attacc;
import battlecode.common.*;

public class Navigation {
    RobotController rc;

    // state related only to navigation should go here

    public Navigation(RobotController r) {
        rc = r;
    }
    
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir) && (!rc.senseFlooding(rc.getLocation().add(dir)) || rc.getType() == RobotType.DELIVERY_DRONE)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    boolean tryMove() throws GameActionException {
        for (Direction dir : Util.directions)
            if (tryMove(dir))
                return true;
        return false;
    }

    // tries to move in the general direction of dir (from lecturePlayer)
    // TODO: Revise this to call the method below
    boolean goTo(Direction dir) throws GameActionException {
        Direction [] toTry;
        if (Math.random() < 0.5) {
            Direction [] temp = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
            toTry = temp;
        }
        else {
            Direction[] temp = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(),dir.rotateLeft().rotateLeft()};
            toTry = temp;
        }
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    // tries to move in the general direction of dir with preference to the right (if preferenceRight is true)
    boolean goTo(Direction dir, boolean preferenceLeft) throws GameActionException {
        Direction [] toTry;
        if (preferenceLeft) {
            Direction [] temp = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
            toTry = temp;
        }
        else {
            Direction[] temp = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(),dir.rotateLeft().rotateLeft()};
            toTry = temp;
        }
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    boolean goTo(MapLocation destination) throws GameActionException {
        System.out.println("Trying to go to " + destination);
        MapLocation myLoc = rc.getLocation();
        double x = destination.x - myLoc.x;
        double y = destination.y - myLoc.y;
        double actualAngle = Math.atan2(y, x);
        Direction dir = myLoc.directionTo(destination);
        double dirAngle = Math.atan2(dir.dy, dir.dx);
        double difference = (actualAngle - dirAngle + 2 * Math.PI) % (2 * Math.PI);
        System.out.println(difference);
        if (difference < Math.PI)
            return goTo(dir, true);
        else
            return goTo(dir, false);
    }
}