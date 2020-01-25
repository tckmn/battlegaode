package attacc;
import battlecode.common.*;

public class HQ extends Shooter {
    int minersBuilt = 0;
    int hqMessageNumber = comms.hqMessageNumber;

    public HQ(RobotController r) throws GameActionException {
        super(r);

        //comms.sendHqLoc(rc.getLocation());
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // TODO: Move this to Communications.java
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
        MapLocation soupLoc = findNearestSoup();
        if (soupLoc != null) lastSoupMined = soupLoc;
        // build only 3 miners (5 if the game goes on too long)
        // TODO: For all miners except the first, spawn in direction of nearest soup
        if (rc.getTeamSoup() >= 60 && (minersBuilt < 4)) {
            // determine best direction to spawn in
            // for first robot this is nearest to MapLocation(X-x, y)
            // otherwise this is nearest to closest soup
            
            MapLocation loc = rc.getLocation();
            MapLocation targetLoc;
            if (minersBuilt == 0) {
                int X = rc.getMapWidth()-1; // correct for 0 indexing of map
                targetLoc = new MapLocation(X - loc.x, loc.y);
            } else {
                targetLoc = findNearestSoup();
                if (targetLoc == null && lastSoupMined != null) targetLoc = lastSoupMined;
            }
            Direction spawnDir = loc.directionTo(targetLoc);
            if (spawnDir != null 
                && (tryBuild(RobotType.MINER, spawnDir) || tryBuild(RobotType.MINER, spawnDir.rotateRight())
                    || tryBuild(RobotType.MINER, spawnDir.rotateLeft()))) {
                minersBuilt ++;
            } else {
            for (Direction dir : Util.directions)
                if(tryBuild(RobotType.MINER, dir))
                    minersBuilt ++;
            }
        }

    }
}