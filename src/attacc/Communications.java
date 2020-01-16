package attacc;
import battlecode.common.*;
import java.util.ArrayList;

public class Communications {
    RobotController rc;

    // state related only to communications should go here

    // all messages from our team should start with this so we can tell them apart
    static final int hqMessageNumber = 18537559;

    public Communications(RobotController r) {
        rc = r;
    }

    MapLocation findHQ() throws GameActionException {
        // read the blockchain until we find the HQ
        // this should only have to read round 1
        int roundNumber = 1;
        outerLoop:
        while (roundNumber < rc.getRoundNum()) {
            Transaction [] block = rc.getBlock(roundNumber);
            for (Transaction t : block)
            {
                int [] message = t.getMessage();
                if(message[0] == hqMessageNumber)
                {
                    MapLocation hqLoc = new MapLocation(message[1], message[2]);
                    System.out.println("Found HQ location");
                    return(hqLoc);
                }
            }
            roundNumber++;
        }
        return null;
    }
}
