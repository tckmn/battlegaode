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

    int enemyTeam = -1;

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

    /*
    Team numbers:
    0: Java Best Waifu
    1: Bowl of Chowder
    2. Citric Sky
    3. levee builders
    4. jenkinsmafia
    5. horsepaste
    6. kryptonite
    7. Anomalous Pandas
    8. Bagger288
    9. NP-ez
    10. Prasici
    11. smite
    12. The High Ground
    13. Blue Dragon
    14. Steam Locomotive
    15. lectureplayer
    */
    MapLocation determineEnemyTeam(ArrayList<MapLocation> enemyHQPossibilities) throws GameActionException {
        Transaction [] block = rc.getBlock(rc.getRoundNum() - 1);
        for (Transaction t : block) {
            int [] message = t.getMessage();
            if (message[6] > 4534000 && message[6] < 4534999) {
                enemyTeam = 0;
                return null;
            } else if (message[0] == -1234917945) {
                enemyTeam = 1;
            } else if (message[0] == 51351235) {
                enemyTeam = 2;
            } else if (message[0] == 2) {
                MapLocation putativeEnemyHQ = new MapLocation(message[3], message[4]);
                if (enemyHQPossibilities.contains(putativeEnemyHQ)) {
                    enemyTeam = 3;
                    return putativeEnemyHQ;
                }
            } else if (message[6] == -276956202) {
                enemyTeam = 6;
            } else if (message[3] != 0 && message[3] == message[4] && message[3] == message[5] && message[3] == message[6]) {
                enemyTeam = 7;
            } else if (message[0] == 483608781) {
                enemyTeam = 9;
            } else if (message[3] == 32005) {
                MapLocation putativeEnemyHQ = new MapLocation(message[0], message[1]);
                if (enemyHQPossibilities.contains(putativeEnemyHQ)) {
                    enemyTeam = 10;
                    return putativeEnemyHQ;
                }
            } else if (message[1] == 0 && message[2] == 0 && message[3] == 0 && message[4] == 0 && message[5] == 0 && message[6] == 0) {
                enemyTeam = 11;
            } else if (message[0] == 274216) {
                enemyTeam = 12;
            } else if (message[0] != 0 && message[1] != 0 && message[2] != 0 && message[3] != 0 && message[4] == 0 && message[5] == 0 && message[6] == 0) {
                enemyTeam = 14;
            } else if (message[0] == 444444444) {
                MapLocation putativeEnemyHQ = new MapLocation(message[2], message[3]);
                if (enemyHQPossibilities.contains(putativeEnemyHQ)) {
                    enemyTeam = 15;
                    return putativeEnemyHQ;
                }
            }
        }
        return null;
    }

    // see if the other team has transmitted their message describing our HQ location
    // only possible for teams 0, 1, 2, 3, 6, 7, 9, 10, 11, 14
    boolean detectEnemyAttackTransmission(MapLocation hqLoc) throws GameActionException {
        if (hqLoc == null) return false;
        if (enemyTeam != 0 && enemyTeam != 1 && enemyTeam != 2 && enemyTeam != 3 && enemyTeam != 6
                && enemyTeam != 7 && enemyTeam != 9 && enemyTeam != 10 && enemyTeam != 11 && enemyTeam != 14)
            return false;

        Transaction [] block = rc.getBlock(rc.getRoundNum() - 1);
        for (Transaction t : block) {
            int [] message = t.getMessage();
            if (enemyTeam == 0 && message[0] == 6 && message[1] == 0 && message[2] == 0
                    && message[3] == 0 && message[4] == 0 && message[5] == 0 && (message[6] >= 4534000 && message[6] <= 4534999))
                return true;
            else if (enemyTeam == 1 && message[0] == -1234917945 && message[1] == 13)
                return true;
            else if (enemyTeam == 2 && message[0] != hqMessageNumber && message[1] == hqLoc.x && message[2] == hqLoc.y)
                return true;
            else if (enemyTeam == 3 && message[0] == 5 && message[1] == hqLoc.x && message[2] == hqLoc.y)
                return true;
            else if (enemyTeam == 6 && message[3] == -571490437 && message[4] == -1142980875 && message[5] == 2009005547)
                return true;
            else if (enemyTeam == 7 && 378969000 <= message[1] && message[1] <= 378969999 && message[1] == message[2] 
                    && message[1] == message[3] && message[1] == message[4] && message[1] == message[5] && message[1] == message[6])
                return true;
            else if (enemyTeam == 9 && message[0] == 483608772)
                return true;
            else if (enemyTeam == 10 && message[0] == hqLoc.x && message[1] == hqLoc.y)
                return true;
            else if (enemyTeam == 11 && message[1] == 134217728)
                return true;
            else if (enemyTeam == 14 && message[0] != 0 && message[1] == 0 && message[2] == 0 && message[3] == 0
                    && message[4] == 0 && message[5] == 0 && message[6] != 0)
                return true;
        }
        return false;
    }

    boolean sabotageBlockchain() throws GameActionException {
        if (enemyTeam != 0 && enemyTeam != 1 && enemyTeam != 2 && enemyTeam != 3 && enemyTeam != 6
                && enemyTeam != 7 && enemyTeam != 8 && enemyTeam != 9 && enemyTeam != 10 && enemyTeam != 11 && enemyTeam != 14)
            return false;

        int message [];
        if (enemyTeam == 0) {
            int[] temp = {6,0,0,0,0,0,4534341};
            message = temp;
        } else if (enemyTeam == 1) {
            int [] temp = {-1234917945,13,2441,1544223,4282708,716220,3047649};
            message = temp;
        } else if (enemyTeam == 2) {
            int [] temp = {2130985,62,33,0,0,-1287958128,-819107665};
            message = temp;
        } else if (enemyTeam == 3) {
            int [] temp = {5,10,5,8,18,0,-320};
            message = temp;
        } else if (enemyTeam == 6) {
            int [] temp = {2001649273,-1216614433,1861738428,-571490437,-1142980875,2009005547,-276956202};
            message = temp;
        } else if (enemyTeam == 7) {
            int [] temp = {348424808,378969008,378969008,378969008,378969008,378969008,378969008,378969008};
            message = temp;
        } else if (enemyTeam == 8) {
            int [] temp = {354115159, -500568180, 644538898, -1181113915, 265534331, 1839458437, -787156269};
            message = temp;
        } else if (enemyTeam == 9) {
            int [] temp = {483608772,1381611016,33213801,157067759,1704169077,1285648416,1172763767};
            message = temp;
        } else if (enemyTeam == 10) {
            int [] temp = {10,5,0,44763,1645346967,151,0};
            temp[5] = rc.getRoundNum();
            message = temp;
        } else if (enemyTeam == 11) {
            int [] temp = {818184970,134217728,0,0,0,0,0};
            message = temp;
        } else if (enemyTeam == 14) {
            int [] temp = {150481,0,0,0,0,0,6235205};
            message = temp;
        }
        else
            return false;

        if (message.length == 0) return false;
        if (enemyTeam == 10) {
            int [] message0 = {10,5,0,44763,29329111,0,0};
            if (rc.canSubmitTransaction(message0, 2))
                rc.submitTransaction(message0, 2);
            else
                return false;
        }
        if (rc.canSubmitTransaction(message, 1)) {
            rc.submitTransaction(message, 1);
            return true;
        }
        return false;
    }
}
