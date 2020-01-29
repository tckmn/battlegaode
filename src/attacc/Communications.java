package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Communications {
    RobotController rc;

    // state related only to communications should go here

    // all messages from our team should start with this so we can tell them apart
    static final int hqMessageNumber = 18277659;
    static final int enemyHQMessageNumber = 878415678;
    static final int soupLocationMessageNumber = 45786456;
    static final int elevatorMessageNumber = 48944512;

    public Communications(RobotController r) {
        rc = r;
    }

    MapLocation findHQ() throws GameActionException {
        // read the blockchain until we find the HQ
        // this should only have to read round 1
        int roundNumber = 1;
        while (roundNumber < rc.getRoundNum()) {
            Transaction [] block = rc.getBlock(roundNumber);
            for (Transaction t : block)
            {
                int [] message = t.getMessage();
                if(message[0] == hqMessageNumber)
                {
                    MapLocation hqLoc = new MapLocation(~message[1], ~message[2]);
                    System.out.println("Found HQ location");
                    return(hqLoc);
                }
            }
            roundNumber++;
        }
        return null;
    }

    boolean transmitEnemyHQ(ArrayList<MapLocation> enemyHQPossibilities) throws GameActionException {
        int message [] = new int [7];
        message[0] = enemyHQMessageNumber;
        int counter = 1;
        for (MapLocation loc : enemyHQPossibilities) {
            message[counter++] = loc.x;
            message[counter++] = loc.y;
        }
        while (counter < 7) message[counter ++] = -10;
        if (rc.canSubmitTransaction(message, 1)) {
            rc.submitTransaction(message, 1);
            return true;
        }
        return false;
    }

    ArrayList<MapLocation> receiveEnemyHQ() throws GameActionException {
        int roundNumber = rc.getRoundNum() - 1; // read backwards from current round number - 1
        // Note: reading backwards is susceptible to enemy messing up the blockchain
        while (roundNumber-- > 0) {
            Transaction [] block = rc.getBlock(roundNumber);
            for (Transaction t : block) {
                int [] message = t.getMessage();
                if (message[0] == enemyHQMessageNumber) {
                    ArrayList<MapLocation> enemyHQPossibilities = new ArrayList<MapLocation>(3);
                    int counter = 1;
                    while (counter < 7 && message[counter] != -10)
                        enemyHQPossibilities.add(new MapLocation(message[counter++], message[counter++]));
                    return enemyHQPossibilities;
                }
            }
        }
        return null;
    }

    boolean requestElevator(MapLocation [] locsToElevate) throws GameActionException {
        int message [] = new int[7];
        message[0] = elevatorMessageNumber;
        int counter = 1;
        for (MapLocation loc : locsToElevate) {
            message[counter++] = ~loc.x;
            message[counter++] = ~loc.y;
        }
        if (rc.canSubmitTransaction(message, 1)) {
            rc.submitTransaction(message, 1);
            return true;
        }
        return false;
    }

    MapLocation[] receiveElevatorRequest() throws GameActionException {
        Transaction [] block = rc.getBlock(rc.getRoundNum() - 1);
            for (Transaction t : block)
            {
                int [] message = t.getMessage();
                if(message[0] == elevatorMessageNumber)
                {
                    if (message[3] == -10) {
                        MapLocation [] returnValue = {new MapLocation(~message[1], ~message[2])};
                        return returnValue;
                    } else if (message[5] == -10) {
                        MapLocation [] returnValue = {new MapLocation(~message[1], ~message[2]), new MapLocation(~message[3], ~message[4])};
                        return returnValue;
                    } else {
                        MapLocation [] returnValue = {new MapLocation(~message[1], ~message[2]), new MapLocation(~message[3], ~message[4]),
                            new MapLocation(~message[5], ~message[6])};
                        return returnValue;
                    }
                }
            }
        return null;
    }
}
