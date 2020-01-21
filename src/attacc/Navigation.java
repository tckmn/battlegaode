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

    // more intelligent navigation (for non-drones)
    // radius is half-edge of square to search in
    boolean navTo(MapLocation destination, int radius, int iterations) throws GameActionException {
        int initialBytecodeCount = Clock.getBytecodeNum();
        int n = 2 * radius + 1;
        System.out.println("Intelligent navigation to " + destination);
        MapLocation myLoc = rc.getLocation();
        boolean [][] validLocs = new boolean [n][n];
        int [][] elevations = new int [n][n];
        // initialize this array
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y ++) {
                MapLocation loc = new MapLocation(myLoc.x + x - radius, myLoc.y + y - radius);
                // check if location is valid movement place (within sensor radius, on map, unoccupied, and not flooded)
                // TODO: Implement occupation check more efficiently (currently takes 20 bytecode)
                boolean valid = rc.canSenseLocation(loc) && !rc.senseFlooding(loc) && !rc.isLocationOccupied(loc);
                validLocs[x][y] = valid;
                if (valid) elevations[x][y] = rc.senseElevation(loc);
            }
        }
        // own location is also valid
        validLocs[radius][radius] = true;
        elevations[radius][radius] = rc.senseElevation(myLoc);

        System.out.println("Bytecodes used in checking location validity: " + (Clock.getBytecodeNum() - initialBytecodeCount));


        // figure out which edges are valid
        // This is a 4 by (n-1) by (n-1) array where the first entry loops over the edges {\,-,/,|}
        // Note: This omits some edges around the top right of the grid but oh well (for now)
        boolean [][] validEdges0 = new boolean [n-1][n-1];
        boolean [][] validEdges1 = new boolean [n-1][n-1];
        boolean [][] validEdges2 = new boolean [n-1][n-1];
        boolean [][] validEdges3 = new boolean [n-1][n-1];
        for (int x = 0; x < n-1; x ++) {
            for (int y = 0; y < n-1; y ++) {
                validEdges0[x][y] = (validLocs[x][y+1] && validLocs[x+1][y] && Math.abs(elevations[x][y+1] - elevations[x+1][y]) <= 3);
                validEdges1[x][y] = (validLocs[x][y] && validLocs[x+1][y] && Math.abs(elevations[x][y] - elevations[x+1][y]) <= 3);
                validEdges2[x][y] = (validLocs[x][y] && validLocs[x+1][y+1] && Math.abs(elevations[x][y] - elevations[x+1][y+1]) <= 3);
                validEdges3[x][y] = (validLocs[x][y] && validLocs[x][y+1] && Math.abs(elevations[x][y] - elevations[x][y+1]) <= 3);
            }
        }
        System.out.println("Bytecodes used in setup: " + (Clock.getBytecodeNum() - initialBytecodeCount));

        // initialize grid with (large number) * distance to enemy HQ
        int [][] distances = new int[n][n];
        for (int x = 0; x < n; x ++)
            for (int y = 0; y < n; y ++)
                distances[x][y] = (int)(Math.sqrt(Math.pow(myLoc.x+x-radius-destination.x,2) + Math.pow(myLoc.y+y-radius-destination.y,2)) * 100 + 0.5);

        System.out.println("Bytecodes used in setup and distance initialization: " + (Clock.getBytecodeNum() - initialBytecodeCount));

        /*
        int [] dummy = {1,2,3,4,5,6,7,8,9,10};
        int dummy0 = 3;
        int dummy1 = 5;
        int dummy2 = 6;
        boolean [] isDummy = {true, true, false, true, false};
        boolean isDummy0 = true;
        boolean isDummy1 = true;
        boolean isDummy2 = false;
        boolean isDummy3 = true;
        boolean isDummy4 = false;
        int dummyX = 0;
        int dummyY = 0;
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        dummy[5] = Math.min(dummy[3], dummy[8]+1);
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        dummy2 = Math.min(dummy0, dummy1+1);
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        isDummy[0] = isDummy[3] && isDummy[2];
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        isDummy0 = isDummy3 && isDummy2;
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        validEdges[0][dummyX][dummyY] = (validLocs[dummyX][dummyY+1] && validLocs[dummyX+1][dummyY] 
            && Math.abs(elevations[dummyX][dummyY+1] - elevations[dummyX+1][dummyY]) <= 3);
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        isDummy0 = isDummy1 && isDummy3 && Math.abs(dummy1 - dummy2) <= 3;
        System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        System.out.println("Bytecodes used in setup and dummy testing: " + (Clock.getBytecodeNum() - initialBytecodeCount));
        */


        // now iterate
        for (int counter = 0; counter < iterations; counter ++) {
            for (int x = 0; x < n-1; x ++) {
                for (int y = 0; y < n-1; y ++) {
                    //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
                    if (validEdges0[x][y]){
                        //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
                        distances[x][y+1] = Math.min(distances[x][y+1], distances[x+1][y] + 1);
                        //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
                        distances[x+1][y] = Math.min(distances[x][y+1] + 1, distances[x+1][y]);
                        //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
                    }
                    //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
                    if (validEdges1[x][y]){
                        distances[x][y] = Math.min(distances[x][y], distances[x+1][y] + 1);
                        distances[x+1][y] = Math.min(distances[x][y] + 1, distances[x+1][y]);
                    }
                    if (validEdges2[x][y]){
                        distances[x][y] = Math.min(distances[x][y], distances[x+1][y+1] + 1);
                        distances[x+1][y+1] = Math.min(distances[x][y] + 1, distances[x+1][y+1]);
                    }
                    if (validEdges3[x][y]){
                        distances[x][y] = Math.min(distances[x][y], distances[x][y+1] + 1);
                        distances[x][y+1] = Math.min(distances[x][y] + 1, distances[x][y+1]);
                    }
                }
            }
            //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
            //System.out.println((Clock.getBytecodeNum() - initialBytecodeCount));
        }

        System.out.println("Bytecodes used in setup and iteration: " + (Clock.getBytecodeNum() - initialBytecodeCount));

        
        // now find minimum value among neighbors adjacent to self (with a valid edge)
        int bestX = radius;
        int bestY = radius;
        int minDistance = Integer.MAX_VALUE;
        // Unfortunately, due to how the edges are stored, we have to try the adjacent tiles manually
        if (validEdges0[radius-1][radius] && distances[radius-1][radius+1] < minDistance) {
            bestX = radius-1;
            bestY = radius+1;
            minDistance = distances[radius-1][radius+1];
            System.out.println(minDistance);
        }
        if (validEdges0[radius][radius-1] && distances[radius+1][radius-1] < minDistance) {
            bestX = radius+1;
            bestY = radius-1;
            minDistance = distances[radius+1][radius-1];
            System.out.println(minDistance);
        }
        
        if (validEdges1[radius-1][radius] && distances[radius-1][radius] < minDistance) {
            bestX = radius-1;
            bestY = radius;
            minDistance = distances[radius][radius-1];
            System.out.println(minDistance);
        }
        if (validEdges1[radius][radius] && distances[radius+1][radius] < minDistance) {
            bestX = radius+1;
            bestY = radius;
            minDistance = distances[radius][radius+1];
            System.out.println(minDistance);
        }

        if (validEdges2[radius-1][radius-1] && distances[radius-1][radius-1] < minDistance) {
            bestX = radius-1;
            bestY = radius-1;
            minDistance = distances[radius-1][radius-1];
            System.out.println(minDistance);
        }
        if (validEdges2[radius][radius] && distances[radius+1][radius+1] < minDistance) {
            bestX = radius+1;
            bestY = radius+1;
            minDistance = distances[radius+1][radius+1];
            System.out.println(minDistance);
        }

        if (validEdges3[radius][radius-1] && distances[radius][radius-1] < minDistance) {
            bestX = radius;
            bestY = radius-1;
            minDistance = distances[radius][radius-1];
            System.out.println(minDistance);
        }
        if (validEdges3[radius][radius] && distances[radius][radius+1] < minDistance) {
            bestX = radius;
            bestY = radius+1;
            minDistance = distances[radius][radius+1];
            System.out.println(minDistance);
        }

        if (minDistance == Integer.MAX_VALUE)
            return false;

        System.out.println("Best location found (relative to current position): (" + (bestX - radius) + "," + (bestY - radius) + ")");
        Direction bestDir = myLoc.directionTo(new MapLocation(myLoc.x + (bestX - radius), myLoc.y + (bestY - radius)));
        System.out.println("Total bytecodes used: " + (Clock.getBytecodeNum() - initialBytecodeCount));

        return tryMove(bestDir);
    }

    // default parameters
    boolean navTo(MapLocation destination) throws GameActionException {
        return navTo(destination, 2, 3);
    }

}