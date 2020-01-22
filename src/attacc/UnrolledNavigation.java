package attacc;
import battlecode.common.*;
public class UnrolledNavigation extends Navigation {
public UnrolledNavigation(RobotController r) {super(r);}
boolean navTo (MapLocation destination) throws GameActionException {
int initialBytecodeCount = Clock.getBytecodeNum();
System.out.println("Intelligent navigation to " + destination);
MapLocation myLoc = rc.getLocation();
int elevations00;
MapLocation loc00 = new MapLocation(myLoc.x + -2, myLoc.y + -2);
boolean validLocs00 = rc.canSenseLocation(loc00) && !rc.senseFlooding(loc00) && !rc.isLocationOccupied(loc00);
if (validLocs00) elevations00 = rc.senseElevation(loc00); else elevations00 = 0;
int elevations01;
MapLocation loc01 = new MapLocation(myLoc.x + -2, myLoc.y + -1);
boolean validLocs01 = rc.canSenseLocation(loc01) && !rc.senseFlooding(loc01) && !rc.isLocationOccupied(loc01);
if (validLocs01) elevations01 = rc.senseElevation(loc01); else elevations01 = 0;
int elevations02;
MapLocation loc02 = new MapLocation(myLoc.x + -2, myLoc.y + 0);
boolean validLocs02 = rc.canSenseLocation(loc02) && !rc.senseFlooding(loc02) && !rc.isLocationOccupied(loc02);
if (validLocs02) elevations02 = rc.senseElevation(loc02); else elevations02 = 0;
int elevations03;
MapLocation loc03 = new MapLocation(myLoc.x + -2, myLoc.y + 1);
boolean validLocs03 = rc.canSenseLocation(loc03) && !rc.senseFlooding(loc03) && !rc.isLocationOccupied(loc03);
if (validLocs03) elevations03 = rc.senseElevation(loc03); else elevations03 = 0;
int elevations04;
MapLocation loc04 = new MapLocation(myLoc.x + -2, myLoc.y + 2);
boolean validLocs04 = rc.canSenseLocation(loc04) && !rc.senseFlooding(loc04) && !rc.isLocationOccupied(loc04);
if (validLocs04) elevations04 = rc.senseElevation(loc04); else elevations04 = 0;
int elevations10;
MapLocation loc10 = new MapLocation(myLoc.x + -1, myLoc.y + -2);
boolean validLocs10 = rc.canSenseLocation(loc10) && !rc.senseFlooding(loc10) && !rc.isLocationOccupied(loc10);
if (validLocs10) elevations10 = rc.senseElevation(loc10); else elevations10 = 0;
int elevations11;
MapLocation loc11 = new MapLocation(myLoc.x + -1, myLoc.y + -1);
boolean validLocs11 = rc.canSenseLocation(loc11) && !rc.senseFlooding(loc11) && !rc.isLocationOccupied(loc11);
if (validLocs11) elevations11 = rc.senseElevation(loc11); else elevations11 = 0;
int elevations12;
MapLocation loc12 = new MapLocation(myLoc.x + -1, myLoc.y + 0);
boolean validLocs12 = rc.canSenseLocation(loc12) && !rc.senseFlooding(loc12) && !rc.isLocationOccupied(loc12);
if (validLocs12) elevations12 = rc.senseElevation(loc12); else elevations12 = 0;
int elevations13;
MapLocation loc13 = new MapLocation(myLoc.x + -1, myLoc.y + 1);
boolean validLocs13 = rc.canSenseLocation(loc13) && !rc.senseFlooding(loc13) && !rc.isLocationOccupied(loc13);
if (validLocs13) elevations13 = rc.senseElevation(loc13); else elevations13 = 0;
int elevations14;
MapLocation loc14 = new MapLocation(myLoc.x + -1, myLoc.y + 2);
boolean validLocs14 = rc.canSenseLocation(loc14) && !rc.senseFlooding(loc14) && !rc.isLocationOccupied(loc14);
if (validLocs14) elevations14 = rc.senseElevation(loc14); else elevations14 = 0;
int elevations20;
MapLocation loc20 = new MapLocation(myLoc.x + 0, myLoc.y + -2);
boolean validLocs20 = rc.canSenseLocation(loc20) && !rc.senseFlooding(loc20) && !rc.isLocationOccupied(loc20);
if (validLocs20) elevations20 = rc.senseElevation(loc20); else elevations20 = 0;
int elevations21;
MapLocation loc21 = new MapLocation(myLoc.x + 0, myLoc.y + -1);
boolean validLocs21 = rc.canSenseLocation(loc21) && !rc.senseFlooding(loc21) && !rc.isLocationOccupied(loc21);
if (validLocs21) elevations21 = rc.senseElevation(loc21); else elevations21 = 0;
int elevations22;
MapLocation loc22 = new MapLocation(myLoc.x + 0, myLoc.y + 0);
boolean validLocs22 = rc.canSenseLocation(loc22) && !rc.senseFlooding(loc22) && !rc.isLocationOccupied(loc22);
if (validLocs22) elevations22 = rc.senseElevation(loc22); else elevations22 = 0;
int elevations23;
MapLocation loc23 = new MapLocation(myLoc.x + 0, myLoc.y + 1);
boolean validLocs23 = rc.canSenseLocation(loc23) && !rc.senseFlooding(loc23) && !rc.isLocationOccupied(loc23);
if (validLocs23) elevations23 = rc.senseElevation(loc23); else elevations23 = 0;
int elevations24;
MapLocation loc24 = new MapLocation(myLoc.x + 0, myLoc.y + 2);
boolean validLocs24 = rc.canSenseLocation(loc24) && !rc.senseFlooding(loc24) && !rc.isLocationOccupied(loc24);
if (validLocs24) elevations24 = rc.senseElevation(loc24); else elevations24 = 0;
int elevations30;
MapLocation loc30 = new MapLocation(myLoc.x + 1, myLoc.y + -2);
boolean validLocs30 = rc.canSenseLocation(loc30) && !rc.senseFlooding(loc30) && !rc.isLocationOccupied(loc30);
if (validLocs30) elevations30 = rc.senseElevation(loc30); else elevations30 = 0;
int elevations31;
MapLocation loc31 = new MapLocation(myLoc.x + 1, myLoc.y + -1);
boolean validLocs31 = rc.canSenseLocation(loc31) && !rc.senseFlooding(loc31) && !rc.isLocationOccupied(loc31);
if (validLocs31) elevations31 = rc.senseElevation(loc31); else elevations31 = 0;
int elevations32;
MapLocation loc32 = new MapLocation(myLoc.x + 1, myLoc.y + 0);
boolean validLocs32 = rc.canSenseLocation(loc32) && !rc.senseFlooding(loc32) && !rc.isLocationOccupied(loc32);
if (validLocs32) elevations32 = rc.senseElevation(loc32); else elevations32 = 0;
int elevations33;
MapLocation loc33 = new MapLocation(myLoc.x + 1, myLoc.y + 1);
boolean validLocs33 = rc.canSenseLocation(loc33) && !rc.senseFlooding(loc33) && !rc.isLocationOccupied(loc33);
if (validLocs33) elevations33 = rc.senseElevation(loc33); else elevations33 = 0;
int elevations34;
MapLocation loc34 = new MapLocation(myLoc.x + 1, myLoc.y + 2);
boolean validLocs34 = rc.canSenseLocation(loc34) && !rc.senseFlooding(loc34) && !rc.isLocationOccupied(loc34);
if (validLocs34) elevations34 = rc.senseElevation(loc34); else elevations34 = 0;
int elevations40;
MapLocation loc40 = new MapLocation(myLoc.x + 2, myLoc.y + -2);
boolean validLocs40 = rc.canSenseLocation(loc40) && !rc.senseFlooding(loc40) && !rc.isLocationOccupied(loc40);
if (validLocs40) elevations40 = rc.senseElevation(loc40); else elevations40 = 0;
int elevations41;
MapLocation loc41 = new MapLocation(myLoc.x + 2, myLoc.y + -1);
boolean validLocs41 = rc.canSenseLocation(loc41) && !rc.senseFlooding(loc41) && !rc.isLocationOccupied(loc41);
if (validLocs41) elevations41 = rc.senseElevation(loc41); else elevations41 = 0;
int elevations42;
MapLocation loc42 = new MapLocation(myLoc.x + 2, myLoc.y + 0);
boolean validLocs42 = rc.canSenseLocation(loc42) && !rc.senseFlooding(loc42) && !rc.isLocationOccupied(loc42);
if (validLocs42) elevations42 = rc.senseElevation(loc42); else elevations42 = 0;
int elevations43;
MapLocation loc43 = new MapLocation(myLoc.x + 2, myLoc.y + 1);
boolean validLocs43 = rc.canSenseLocation(loc43) && !rc.senseFlooding(loc43) && !rc.isLocationOccupied(loc43);
if (validLocs43) elevations43 = rc.senseElevation(loc43); else elevations43 = 0;
int elevations44;
MapLocation loc44 = new MapLocation(myLoc.x + 2, myLoc.y + 2);
boolean validLocs44 = rc.canSenseLocation(loc44) && !rc.senseFlooding(loc44) && !rc.isLocationOccupied(loc44);
if (validLocs44) elevations44 = rc.senseElevation(loc44); else elevations44 = 0;
validLocs22 = true;
elevations22 = rc.senseElevation(loc22);
System.out.println("Bytecodes used in checking location validity: " + (Clock.getBytecodeNum() - initialBytecodeCount));
boolean validEdges000 = (validLocs01 && validLocs10 && Math.abs(elevations01 - elevations10) <= 3);
boolean validEdges100 = (validLocs00 && validLocs10 && Math.abs(elevations00 - elevations10) <= 3);
boolean validEdges200 = (validLocs00 && validLocs11 && Math.abs(elevations00 - elevations11) <= 3);
boolean validEdges300 = (validLocs00 && validLocs01 && Math.abs(elevations00 - elevations01) <= 3);
boolean validEdges001 = (validLocs02 && validLocs11 && Math.abs(elevations02 - elevations11) <= 3);
boolean validEdges101 = (validLocs01 && validLocs11 && Math.abs(elevations01 - elevations11) <= 3);
boolean validEdges201 = (validLocs01 && validLocs12 && Math.abs(elevations01 - elevations12) <= 3);
boolean validEdges301 = (validLocs01 && validLocs02 && Math.abs(elevations01 - elevations02) <= 3);
boolean validEdges002 = (validLocs03 && validLocs12 && Math.abs(elevations03 - elevations12) <= 3);
boolean validEdges102 = (validLocs02 && validLocs12 && Math.abs(elevations02 - elevations12) <= 3);
boolean validEdges202 = (validLocs02 && validLocs13 && Math.abs(elevations02 - elevations13) <= 3);
boolean validEdges302 = (validLocs02 && validLocs03 && Math.abs(elevations02 - elevations03) <= 3);
boolean validEdges003 = (validLocs04 && validLocs13 && Math.abs(elevations04 - elevations13) <= 3);
boolean validEdges103 = (validLocs03 && validLocs13 && Math.abs(elevations03 - elevations13) <= 3);
boolean validEdges203 = (validLocs03 && validLocs14 && Math.abs(elevations03 - elevations14) <= 3);
boolean validEdges303 = (validLocs03 && validLocs04 && Math.abs(elevations03 - elevations04) <= 3);
boolean validEdges010 = (validLocs11 && validLocs20 && Math.abs(elevations11 - elevations20) <= 3);
boolean validEdges110 = (validLocs10 && validLocs20 && Math.abs(elevations10 - elevations20) <= 3);
boolean validEdges210 = (validLocs10 && validLocs21 && Math.abs(elevations10 - elevations21) <= 3);
boolean validEdges310 = (validLocs10 && validLocs11 && Math.abs(elevations10 - elevations11) <= 3);
boolean validEdges011 = (validLocs12 && validLocs21 && Math.abs(elevations12 - elevations21) <= 3);
boolean validEdges111 = (validLocs11 && validLocs21 && Math.abs(elevations11 - elevations21) <= 3);
boolean validEdges211 = (validLocs11 && validLocs22 && Math.abs(elevations11 - elevations22) <= 3);
boolean validEdges311 = (validLocs11 && validLocs12 && Math.abs(elevations11 - elevations12) <= 3);
boolean validEdges012 = (validLocs13 && validLocs22 && Math.abs(elevations13 - elevations22) <= 3);
boolean validEdges112 = (validLocs12 && validLocs22 && Math.abs(elevations12 - elevations22) <= 3);
boolean validEdges212 = (validLocs12 && validLocs23 && Math.abs(elevations12 - elevations23) <= 3);
boolean validEdges312 = (validLocs12 && validLocs13 && Math.abs(elevations12 - elevations13) <= 3);
boolean validEdges013 = (validLocs14 && validLocs23 && Math.abs(elevations14 - elevations23) <= 3);
boolean validEdges113 = (validLocs13 && validLocs23 && Math.abs(elevations13 - elevations23) <= 3);
boolean validEdges213 = (validLocs13 && validLocs24 && Math.abs(elevations13 - elevations24) <= 3);
boolean validEdges313 = (validLocs13 && validLocs14 && Math.abs(elevations13 - elevations14) <= 3);
boolean validEdges020 = (validLocs21 && validLocs30 && Math.abs(elevations21 - elevations30) <= 3);
boolean validEdges120 = (validLocs20 && validLocs30 && Math.abs(elevations20 - elevations30) <= 3);
boolean validEdges220 = (validLocs20 && validLocs31 && Math.abs(elevations20 - elevations31) <= 3);
boolean validEdges320 = (validLocs20 && validLocs21 && Math.abs(elevations20 - elevations21) <= 3);
boolean validEdges021 = (validLocs22 && validLocs31 && Math.abs(elevations22 - elevations31) <= 3);
boolean validEdges121 = (validLocs21 && validLocs31 && Math.abs(elevations21 - elevations31) <= 3);
boolean validEdges221 = (validLocs21 && validLocs32 && Math.abs(elevations21 - elevations32) <= 3);
boolean validEdges321 = (validLocs21 && validLocs22 && Math.abs(elevations21 - elevations22) <= 3);
boolean validEdges022 = (validLocs23 && validLocs32 && Math.abs(elevations23 - elevations32) <= 3);
boolean validEdges122 = (validLocs22 && validLocs32 && Math.abs(elevations22 - elevations32) <= 3);
boolean validEdges222 = (validLocs22 && validLocs33 && Math.abs(elevations22 - elevations33) <= 3);
boolean validEdges322 = (validLocs22 && validLocs23 && Math.abs(elevations22 - elevations23) <= 3);
boolean validEdges023 = (validLocs24 && validLocs33 && Math.abs(elevations24 - elevations33) <= 3);
boolean validEdges123 = (validLocs23 && validLocs33 && Math.abs(elevations23 - elevations33) <= 3);
boolean validEdges223 = (validLocs23 && validLocs34 && Math.abs(elevations23 - elevations34) <= 3);
boolean validEdges323 = (validLocs23 && validLocs24 && Math.abs(elevations23 - elevations24) <= 3);
boolean validEdges030 = (validLocs31 && validLocs40 && Math.abs(elevations31 - elevations40) <= 3);
boolean validEdges130 = (validLocs30 && validLocs40 && Math.abs(elevations30 - elevations40) <= 3);
boolean validEdges230 = (validLocs30 && validLocs41 && Math.abs(elevations30 - elevations41) <= 3);
boolean validEdges330 = (validLocs30 && validLocs31 && Math.abs(elevations30 - elevations31) <= 3);
boolean validEdges031 = (validLocs32 && validLocs41 && Math.abs(elevations32 - elevations41) <= 3);
boolean validEdges131 = (validLocs31 && validLocs41 && Math.abs(elevations31 - elevations41) <= 3);
boolean validEdges231 = (validLocs31 && validLocs42 && Math.abs(elevations31 - elevations42) <= 3);
boolean validEdges331 = (validLocs31 && validLocs32 && Math.abs(elevations31 - elevations32) <= 3);
boolean validEdges032 = (validLocs33 && validLocs42 && Math.abs(elevations33 - elevations42) <= 3);
boolean validEdges132 = (validLocs32 && validLocs42 && Math.abs(elevations32 - elevations42) <= 3);
boolean validEdges232 = (validLocs32 && validLocs43 && Math.abs(elevations32 - elevations43) <= 3);
boolean validEdges332 = (validLocs32 && validLocs33 && Math.abs(elevations32 - elevations33) <= 3);
boolean validEdges033 = (validLocs34 && validLocs43 && Math.abs(elevations34 - elevations43) <= 3);
boolean validEdges133 = (validLocs33 && validLocs43 && Math.abs(elevations33 - elevations43) <= 3);
boolean validEdges233 = (validLocs33 && validLocs44 && Math.abs(elevations33 - elevations44) <= 3);
boolean validEdges333 = (validLocs33 && validLocs34 && Math.abs(elevations33 - elevations34) <= 3);
System.out.println("Bytecodes used in setup: " + (Clock.getBytecodeNum() - initialBytecodeCount));
int shiftedX = myLoc.x - destination.x - 2;
int shiftedY = myLoc.y - destination.y - 2;
int distances00 = (int)(Math.sqrt(Math.pow(shiftedX + 0, 2) + Math.pow(shiftedY + 0, 2)) * 100 + 0.5);
int distances01 = (int)(Math.sqrt(Math.pow(shiftedX + 0, 2) + Math.pow(shiftedY + 1, 2)) * 100 + 0.5);
int distances02 = (int)(Math.sqrt(Math.pow(shiftedX + 0, 2) + Math.pow(shiftedY + 2, 2)) * 100 + 0.5);
int distances03 = (int)(Math.sqrt(Math.pow(shiftedX + 0, 2) + Math.pow(shiftedY + 3, 2)) * 100 + 0.5);
int distances04 = (int)(Math.sqrt(Math.pow(shiftedX + 0, 2) + Math.pow(shiftedY + 4, 2)) * 100 + 0.5);
int distances10 = (int)(Math.sqrt(Math.pow(shiftedX + 1, 2) + Math.pow(shiftedY + 0, 2)) * 100 + 0.5);
int distances11 = (int)(Math.sqrt(Math.pow(shiftedX + 1, 2) + Math.pow(shiftedY + 1, 2)) * 100 + 0.5);
int distances12 = (int)(Math.sqrt(Math.pow(shiftedX + 1, 2) + Math.pow(shiftedY + 2, 2)) * 100 + 0.5);
int distances13 = (int)(Math.sqrt(Math.pow(shiftedX + 1, 2) + Math.pow(shiftedY + 3, 2)) * 100 + 0.5);
int distances14 = (int)(Math.sqrt(Math.pow(shiftedX + 1, 2) + Math.pow(shiftedY + 4, 2)) * 100 + 0.5);
int distances20 = (int)(Math.sqrt(Math.pow(shiftedX + 2, 2) + Math.pow(shiftedY + 0, 2)) * 100 + 0.5);
int distances21 = (int)(Math.sqrt(Math.pow(shiftedX + 2, 2) + Math.pow(shiftedY + 1, 2)) * 100 + 0.5);
int distances22 = (int)(Math.sqrt(Math.pow(shiftedX + 2, 2) + Math.pow(shiftedY + 2, 2)) * 100 + 0.5);
int distances23 = (int)(Math.sqrt(Math.pow(shiftedX + 2, 2) + Math.pow(shiftedY + 3, 2)) * 100 + 0.5);
int distances24 = (int)(Math.sqrt(Math.pow(shiftedX + 2, 2) + Math.pow(shiftedY + 4, 2)) * 100 + 0.5);
int distances30 = (int)(Math.sqrt(Math.pow(shiftedX + 3, 2) + Math.pow(shiftedY + 0, 2)) * 100 + 0.5);
int distances31 = (int)(Math.sqrt(Math.pow(shiftedX + 3, 2) + Math.pow(shiftedY + 1, 2)) * 100 + 0.5);
int distances32 = (int)(Math.sqrt(Math.pow(shiftedX + 3, 2) + Math.pow(shiftedY + 2, 2)) * 100 + 0.5);
int distances33 = (int)(Math.sqrt(Math.pow(shiftedX + 3, 2) + Math.pow(shiftedY + 3, 2)) * 100 + 0.5);
int distances34 = (int)(Math.sqrt(Math.pow(shiftedX + 3, 2) + Math.pow(shiftedY + 4, 2)) * 100 + 0.5);
int distances40 = (int)(Math.sqrt(Math.pow(shiftedX + 4, 2) + Math.pow(shiftedY + 0, 2)) * 100 + 0.5);
int distances41 = (int)(Math.sqrt(Math.pow(shiftedX + 4, 2) + Math.pow(shiftedY + 1, 2)) * 100 + 0.5);
int distances42 = (int)(Math.sqrt(Math.pow(shiftedX + 4, 2) + Math.pow(shiftedY + 2, 2)) * 100 + 0.5);
int distances43 = (int)(Math.sqrt(Math.pow(shiftedX + 4, 2) + Math.pow(shiftedY + 3, 2)) * 100 + 0.5);
int distances44 = (int)(Math.sqrt(Math.pow(shiftedX + 4, 2) + Math.pow(shiftedY + 4, 2)) * 100 + 0.5);
System.out.println("Bytecodes used in setup and distance initialization: " + (Clock.getBytecodeNum() - initialBytecodeCount));
if (validEdges000){
distances01=Math.min(distances01, distances10+1);
distances10=Math.min(distances01+1, distances10);
}
if (validEdges100){
distances00=Math.min(distances00, distances10+1);
distances10=Math.min(distances00+1, distances10);
}
if (validEdges200){
distances00=Math.min(distances00, distances11+1);
distances11=Math.min(distances00+1, distances11);
}
if (validEdges300){
distances00=Math.min(distances00, distances01+1);
distances01=Math.min(distances00+1, distances01);
}
if (validEdges001){
distances02=Math.min(distances02, distances11+1);
distances11=Math.min(distances02+1, distances11);
}
if (validEdges101){
distances01=Math.min(distances01, distances11+1);
distances11=Math.min(distances01+1, distances11);
}
if (validEdges201){
distances01=Math.min(distances01, distances12+1);
distances12=Math.min(distances01+1, distances12);
}
if (validEdges301){
distances01=Math.min(distances01, distances02+1);
distances02=Math.min(distances01+1, distances02);
}
if (validEdges002){
distances03=Math.min(distances03, distances12+1);
distances12=Math.min(distances03+1, distances12);
}
if (validEdges102){
distances02=Math.min(distances02, distances12+1);
distances12=Math.min(distances02+1, distances12);
}
if (validEdges202){
distances02=Math.min(distances02, distances13+1);
distances13=Math.min(distances02+1, distances13);
}
if (validEdges302){
distances02=Math.min(distances02, distances03+1);
distances03=Math.min(distances02+1, distances03);
}
if (validEdges003){
distances04=Math.min(distances04, distances13+1);
distances13=Math.min(distances04+1, distances13);
}
if (validEdges103){
distances03=Math.min(distances03, distances13+1);
distances13=Math.min(distances03+1, distances13);
}
if (validEdges203){
distances03=Math.min(distances03, distances14+1);
distances14=Math.min(distances03+1, distances14);
}
if (validEdges303){
distances03=Math.min(distances03, distances04+1);
distances04=Math.min(distances03+1, distances04);
}
if (validEdges010){
distances11=Math.min(distances11, distances20+1);
distances20=Math.min(distances11+1, distances20);
}
if (validEdges110){
distances10=Math.min(distances10, distances20+1);
distances20=Math.min(distances10+1, distances20);
}
if (validEdges210){
distances10=Math.min(distances10, distances21+1);
distances21=Math.min(distances10+1, distances21);
}
if (validEdges310){
distances10=Math.min(distances10, distances11+1);
distances11=Math.min(distances10+1, distances11);
}
if (validEdges011){
distances12=Math.min(distances12, distances21+1);
distances21=Math.min(distances12+1, distances21);
}
if (validEdges111){
distances11=Math.min(distances11, distances21+1);
distances21=Math.min(distances11+1, distances21);
}
if (validEdges211){
distances11=Math.min(distances11, distances22+1);
distances22=Math.min(distances11+1, distances22);
}
if (validEdges311){
distances11=Math.min(distances11, distances12+1);
distances12=Math.min(distances11+1, distances12);
}
if (validEdges012){
distances13=Math.min(distances13, distances22+1);
distances22=Math.min(distances13+1, distances22);
}
if (validEdges112){
distances12=Math.min(distances12, distances22+1);
distances22=Math.min(distances12+1, distances22);
}
if (validEdges212){
distances12=Math.min(distances12, distances23+1);
distances23=Math.min(distances12+1, distances23);
}
if (validEdges312){
distances12=Math.min(distances12, distances13+1);
distances13=Math.min(distances12+1, distances13);
}
if (validEdges013){
distances14=Math.min(distances14, distances23+1);
distances23=Math.min(distances14+1, distances23);
}
if (validEdges113){
distances13=Math.min(distances13, distances23+1);
distances23=Math.min(distances13+1, distances23);
}
if (validEdges213){
distances13=Math.min(distances13, distances24+1);
distances24=Math.min(distances13+1, distances24);
}
if (validEdges313){
distances13=Math.min(distances13, distances14+1);
distances14=Math.min(distances13+1, distances14);
}
if (validEdges020){
distances21=Math.min(distances21, distances30+1);
distances30=Math.min(distances21+1, distances30);
}
if (validEdges120){
distances20=Math.min(distances20, distances30+1);
distances30=Math.min(distances20+1, distances30);
}
if (validEdges220){
distances20=Math.min(distances20, distances31+1);
distances31=Math.min(distances20+1, distances31);
}
if (validEdges320){
distances20=Math.min(distances20, distances21+1);
distances21=Math.min(distances20+1, distances21);
}
if (validEdges021){
distances22=Math.min(distances22, distances31+1);
distances31=Math.min(distances22+1, distances31);
}
if (validEdges121){
distances21=Math.min(distances21, distances31+1);
distances31=Math.min(distances21+1, distances31);
}
if (validEdges221){
distances21=Math.min(distances21, distances32+1);
distances32=Math.min(distances21+1, distances32);
}
if (validEdges321){
distances21=Math.min(distances21, distances22+1);
distances22=Math.min(distances21+1, distances22);
}
if (validEdges022){
distances23=Math.min(distances23, distances32+1);
distances32=Math.min(distances23+1, distances32);
}
if (validEdges122){
distances22=Math.min(distances22, distances32+1);
distances32=Math.min(distances22+1, distances32);
}
if (validEdges222){
distances22=Math.min(distances22, distances33+1);
distances33=Math.min(distances22+1, distances33);
}
if (validEdges322){
distances22=Math.min(distances22, distances23+1);
distances23=Math.min(distances22+1, distances23);
}
if (validEdges023){
distances24=Math.min(distances24, distances33+1);
distances33=Math.min(distances24+1, distances33);
}
if (validEdges123){
distances23=Math.min(distances23, distances33+1);
distances33=Math.min(distances23+1, distances33);
}
if (validEdges223){
distances23=Math.min(distances23, distances34+1);
distances34=Math.min(distances23+1, distances34);
}
if (validEdges323){
distances23=Math.min(distances23, distances24+1);
distances24=Math.min(distances23+1, distances24);
}
if (validEdges030){
distances31=Math.min(distances31, distances40+1);
distances40=Math.min(distances31+1, distances40);
}
if (validEdges130){
distances30=Math.min(distances30, distances40+1);
distances40=Math.min(distances30+1, distances40);
}
if (validEdges230){
distances30=Math.min(distances30, distances41+1);
distances41=Math.min(distances30+1, distances41);
}
if (validEdges330){
distances30=Math.min(distances30, distances31+1);
distances31=Math.min(distances30+1, distances31);
}
if (validEdges031){
distances32=Math.min(distances32, distances41+1);
distances41=Math.min(distances32+1, distances41);
}
if (validEdges131){
distances31=Math.min(distances31, distances41+1);
distances41=Math.min(distances31+1, distances41);
}
if (validEdges231){
distances31=Math.min(distances31, distances42+1);
distances42=Math.min(distances31+1, distances42);
}
if (validEdges331){
distances31=Math.min(distances31, distances32+1);
distances32=Math.min(distances31+1, distances32);
}
if (validEdges032){
distances33=Math.min(distances33, distances42+1);
distances42=Math.min(distances33+1, distances42);
}
if (validEdges132){
distances32=Math.min(distances32, distances42+1);
distances42=Math.min(distances32+1, distances42);
}
if (validEdges232){
distances32=Math.min(distances32, distances43+1);
distances43=Math.min(distances32+1, distances43);
}
if (validEdges332){
distances32=Math.min(distances32, distances33+1);
distances33=Math.min(distances32+1, distances33);
}
if (validEdges033){
distances34=Math.min(distances34, distances43+1);
distances43=Math.min(distances34+1, distances43);
}
if (validEdges133){
distances33=Math.min(distances33, distances43+1);
distances43=Math.min(distances33+1, distances43);
}
if (validEdges233){
distances33=Math.min(distances33, distances44+1);
distances44=Math.min(distances33+1, distances44);
}
if (validEdges333){
distances33=Math.min(distances33, distances34+1);
distances34=Math.min(distances33+1, distances34);
}
if (validEdges000){
distances01=Math.min(distances01, distances10+1);
distances10=Math.min(distances01+1, distances10);
}
if (validEdges100){
distances00=Math.min(distances00, distances10+1);
distances10=Math.min(distances00+1, distances10);
}
if (validEdges200){
distances00=Math.min(distances00, distances11+1);
distances11=Math.min(distances00+1, distances11);
}
if (validEdges300){
distances00=Math.min(distances00, distances01+1);
distances01=Math.min(distances00+1, distances01);
}
if (validEdges001){
distances02=Math.min(distances02, distances11+1);
distances11=Math.min(distances02+1, distances11);
}
if (validEdges101){
distances01=Math.min(distances01, distances11+1);
distances11=Math.min(distances01+1, distances11);
}
if (validEdges201){
distances01=Math.min(distances01, distances12+1);
distances12=Math.min(distances01+1, distances12);
}
if (validEdges301){
distances01=Math.min(distances01, distances02+1);
distances02=Math.min(distances01+1, distances02);
}
if (validEdges002){
distances03=Math.min(distances03, distances12+1);
distances12=Math.min(distances03+1, distances12);
}
if (validEdges102){
distances02=Math.min(distances02, distances12+1);
distances12=Math.min(distances02+1, distances12);
}
if (validEdges202){
distances02=Math.min(distances02, distances13+1);
distances13=Math.min(distances02+1, distances13);
}
if (validEdges302){
distances02=Math.min(distances02, distances03+1);
distances03=Math.min(distances02+1, distances03);
}
if (validEdges003){
distances04=Math.min(distances04, distances13+1);
distances13=Math.min(distances04+1, distances13);
}
if (validEdges103){
distances03=Math.min(distances03, distances13+1);
distances13=Math.min(distances03+1, distances13);
}
if (validEdges203){
distances03=Math.min(distances03, distances14+1);
distances14=Math.min(distances03+1, distances14);
}
if (validEdges303){
distances03=Math.min(distances03, distances04+1);
distances04=Math.min(distances03+1, distances04);
}
if (validEdges010){
distances11=Math.min(distances11, distances20+1);
distances20=Math.min(distances11+1, distances20);
}
if (validEdges110){
distances10=Math.min(distances10, distances20+1);
distances20=Math.min(distances10+1, distances20);
}
if (validEdges210){
distances10=Math.min(distances10, distances21+1);
distances21=Math.min(distances10+1, distances21);
}
if (validEdges310){
distances10=Math.min(distances10, distances11+1);
distances11=Math.min(distances10+1, distances11);
}
if (validEdges011){
distances12=Math.min(distances12, distances21+1);
distances21=Math.min(distances12+1, distances21);
}
if (validEdges111){
distances11=Math.min(distances11, distances21+1);
distances21=Math.min(distances11+1, distances21);
}
if (validEdges211){
distances11=Math.min(distances11, distances22+1);
distances22=Math.min(distances11+1, distances22);
}
if (validEdges311){
distances11=Math.min(distances11, distances12+1);
distances12=Math.min(distances11+1, distances12);
}
if (validEdges012){
distances13=Math.min(distances13, distances22+1);
distances22=Math.min(distances13+1, distances22);
}
if (validEdges112){
distances12=Math.min(distances12, distances22+1);
distances22=Math.min(distances12+1, distances22);
}
if (validEdges212){
distances12=Math.min(distances12, distances23+1);
distances23=Math.min(distances12+1, distances23);
}
if (validEdges312){
distances12=Math.min(distances12, distances13+1);
distances13=Math.min(distances12+1, distances13);
}
if (validEdges013){
distances14=Math.min(distances14, distances23+1);
distances23=Math.min(distances14+1, distances23);
}
if (validEdges113){
distances13=Math.min(distances13, distances23+1);
distances23=Math.min(distances13+1, distances23);
}
if (validEdges213){
distances13=Math.min(distances13, distances24+1);
distances24=Math.min(distances13+1, distances24);
}
if (validEdges313){
distances13=Math.min(distances13, distances14+1);
distances14=Math.min(distances13+1, distances14);
}
if (validEdges020){
distances21=Math.min(distances21, distances30+1);
distances30=Math.min(distances21+1, distances30);
}
if (validEdges120){
distances20=Math.min(distances20, distances30+1);
distances30=Math.min(distances20+1, distances30);
}
if (validEdges220){
distances20=Math.min(distances20, distances31+1);
distances31=Math.min(distances20+1, distances31);
}
if (validEdges320){
distances20=Math.min(distances20, distances21+1);
distances21=Math.min(distances20+1, distances21);
}
if (validEdges021){
distances22=Math.min(distances22, distances31+1);
distances31=Math.min(distances22+1, distances31);
}
if (validEdges121){
distances21=Math.min(distances21, distances31+1);
distances31=Math.min(distances21+1, distances31);
}
if (validEdges221){
distances21=Math.min(distances21, distances32+1);
distances32=Math.min(distances21+1, distances32);
}
if (validEdges321){
distances21=Math.min(distances21, distances22+1);
distances22=Math.min(distances21+1, distances22);
}
if (validEdges022){
distances23=Math.min(distances23, distances32+1);
distances32=Math.min(distances23+1, distances32);
}
if (validEdges122){
distances22=Math.min(distances22, distances32+1);
distances32=Math.min(distances22+1, distances32);
}
if (validEdges222){
distances22=Math.min(distances22, distances33+1);
distances33=Math.min(distances22+1, distances33);
}
if (validEdges322){
distances22=Math.min(distances22, distances23+1);
distances23=Math.min(distances22+1, distances23);
}
if (validEdges023){
distances24=Math.min(distances24, distances33+1);
distances33=Math.min(distances24+1, distances33);
}
if (validEdges123){
distances23=Math.min(distances23, distances33+1);
distances33=Math.min(distances23+1, distances33);
}
if (validEdges223){
distances23=Math.min(distances23, distances34+1);
distances34=Math.min(distances23+1, distances34);
}
if (validEdges323){
distances23=Math.min(distances23, distances24+1);
distances24=Math.min(distances23+1, distances24);
}
if (validEdges030){
distances31=Math.min(distances31, distances40+1);
distances40=Math.min(distances31+1, distances40);
}
if (validEdges130){
distances30=Math.min(distances30, distances40+1);
distances40=Math.min(distances30+1, distances40);
}
if (validEdges230){
distances30=Math.min(distances30, distances41+1);
distances41=Math.min(distances30+1, distances41);
}
if (validEdges330){
distances30=Math.min(distances30, distances31+1);
distances31=Math.min(distances30+1, distances31);
}
if (validEdges031){
distances32=Math.min(distances32, distances41+1);
distances41=Math.min(distances32+1, distances41);
}
if (validEdges131){
distances31=Math.min(distances31, distances41+1);
distances41=Math.min(distances31+1, distances41);
}
if (validEdges231){
distances31=Math.min(distances31, distances42+1);
distances42=Math.min(distances31+1, distances42);
}
if (validEdges331){
distances31=Math.min(distances31, distances32+1);
distances32=Math.min(distances31+1, distances32);
}
if (validEdges032){
distances33=Math.min(distances33, distances42+1);
distances42=Math.min(distances33+1, distances42);
}
if (validEdges132){
distances32=Math.min(distances32, distances42+1);
distances42=Math.min(distances32+1, distances42);
}
if (validEdges232){
distances32=Math.min(distances32, distances43+1);
distances43=Math.min(distances32+1, distances43);
}
if (validEdges332){
distances32=Math.min(distances32, distances33+1);
distances33=Math.min(distances32+1, distances33);
}
if (validEdges033){
distances34=Math.min(distances34, distances43+1);
distances43=Math.min(distances34+1, distances43);
}
if (validEdges133){
distances33=Math.min(distances33, distances43+1);
distances43=Math.min(distances33+1, distances43);
}
if (validEdges233){
distances33=Math.min(distances33, distances44+1);
distances44=Math.min(distances33+1, distances44);
}
if (validEdges333){
distances33=Math.min(distances33, distances34+1);
distances34=Math.min(distances33+1, distances34);
}
if (validEdges000){
distances01=Math.min(distances01, distances10+1);
distances10=Math.min(distances01+1, distances10);
}
if (validEdges100){
distances00=Math.min(distances00, distances10+1);
distances10=Math.min(distances00+1, distances10);
}
if (validEdges200){
distances00=Math.min(distances00, distances11+1);
distances11=Math.min(distances00+1, distances11);
}
if (validEdges300){
distances00=Math.min(distances00, distances01+1);
distances01=Math.min(distances00+1, distances01);
}
if (validEdges001){
distances02=Math.min(distances02, distances11+1);
distances11=Math.min(distances02+1, distances11);
}
if (validEdges101){
distances01=Math.min(distances01, distances11+1);
distances11=Math.min(distances01+1, distances11);
}
if (validEdges201){
distances01=Math.min(distances01, distances12+1);
distances12=Math.min(distances01+1, distances12);
}
if (validEdges301){
distances01=Math.min(distances01, distances02+1);
distances02=Math.min(distances01+1, distances02);
}
if (validEdges002){
distances03=Math.min(distances03, distances12+1);
distances12=Math.min(distances03+1, distances12);
}
if (validEdges102){
distances02=Math.min(distances02, distances12+1);
distances12=Math.min(distances02+1, distances12);
}
if (validEdges202){
distances02=Math.min(distances02, distances13+1);
distances13=Math.min(distances02+1, distances13);
}
if (validEdges302){
distances02=Math.min(distances02, distances03+1);
distances03=Math.min(distances02+1, distances03);
}
if (validEdges003){
distances04=Math.min(distances04, distances13+1);
distances13=Math.min(distances04+1, distances13);
}
if (validEdges103){
distances03=Math.min(distances03, distances13+1);
distances13=Math.min(distances03+1, distances13);
}
if (validEdges203){
distances03=Math.min(distances03, distances14+1);
distances14=Math.min(distances03+1, distances14);
}
if (validEdges303){
distances03=Math.min(distances03, distances04+1);
distances04=Math.min(distances03+1, distances04);
}
if (validEdges010){
distances11=Math.min(distances11, distances20+1);
distances20=Math.min(distances11+1, distances20);
}
if (validEdges110){
distances10=Math.min(distances10, distances20+1);
distances20=Math.min(distances10+1, distances20);
}
if (validEdges210){
distances10=Math.min(distances10, distances21+1);
distances21=Math.min(distances10+1, distances21);
}
if (validEdges310){
distances10=Math.min(distances10, distances11+1);
distances11=Math.min(distances10+1, distances11);
}
if (validEdges011){
distances12=Math.min(distances12, distances21+1);
distances21=Math.min(distances12+1, distances21);
}
if (validEdges111){
distances11=Math.min(distances11, distances21+1);
distances21=Math.min(distances11+1, distances21);
}
if (validEdges211){
distances11=Math.min(distances11, distances22+1);
distances22=Math.min(distances11+1, distances22);
}
if (validEdges311){
distances11=Math.min(distances11, distances12+1);
distances12=Math.min(distances11+1, distances12);
}
if (validEdges012){
distances13=Math.min(distances13, distances22+1);
distances22=Math.min(distances13+1, distances22);
}
if (validEdges112){
distances12=Math.min(distances12, distances22+1);
distances22=Math.min(distances12+1, distances22);
}
if (validEdges212){
distances12=Math.min(distances12, distances23+1);
distances23=Math.min(distances12+1, distances23);
}
if (validEdges312){
distances12=Math.min(distances12, distances13+1);
distances13=Math.min(distances12+1, distances13);
}
if (validEdges013){
distances14=Math.min(distances14, distances23+1);
distances23=Math.min(distances14+1, distances23);
}
if (validEdges113){
distances13=Math.min(distances13, distances23+1);
distances23=Math.min(distances13+1, distances23);
}
if (validEdges213){
distances13=Math.min(distances13, distances24+1);
distances24=Math.min(distances13+1, distances24);
}
if (validEdges313){
distances13=Math.min(distances13, distances14+1);
distances14=Math.min(distances13+1, distances14);
}
if (validEdges020){
distances21=Math.min(distances21, distances30+1);
distances30=Math.min(distances21+1, distances30);
}
if (validEdges120){
distances20=Math.min(distances20, distances30+1);
distances30=Math.min(distances20+1, distances30);
}
if (validEdges220){
distances20=Math.min(distances20, distances31+1);
distances31=Math.min(distances20+1, distances31);
}
if (validEdges320){
distances20=Math.min(distances20, distances21+1);
distances21=Math.min(distances20+1, distances21);
}
if (validEdges021){
distances22=Math.min(distances22, distances31+1);
distances31=Math.min(distances22+1, distances31);
}
if (validEdges121){
distances21=Math.min(distances21, distances31+1);
distances31=Math.min(distances21+1, distances31);
}
if (validEdges221){
distances21=Math.min(distances21, distances32+1);
distances32=Math.min(distances21+1, distances32);
}
if (validEdges321){
distances21=Math.min(distances21, distances22+1);
distances22=Math.min(distances21+1, distances22);
}
if (validEdges022){
distances23=Math.min(distances23, distances32+1);
distances32=Math.min(distances23+1, distances32);
}
if (validEdges122){
distances22=Math.min(distances22, distances32+1);
distances32=Math.min(distances22+1, distances32);
}
if (validEdges222){
distances22=Math.min(distances22, distances33+1);
distances33=Math.min(distances22+1, distances33);
}
if (validEdges322){
distances22=Math.min(distances22, distances23+1);
distances23=Math.min(distances22+1, distances23);
}
if (validEdges023){
distances24=Math.min(distances24, distances33+1);
distances33=Math.min(distances24+1, distances33);
}
if (validEdges123){
distances23=Math.min(distances23, distances33+1);
distances33=Math.min(distances23+1, distances33);
}
if (validEdges223){
distances23=Math.min(distances23, distances34+1);
distances34=Math.min(distances23+1, distances34);
}
if (validEdges323){
distances23=Math.min(distances23, distances24+1);
distances24=Math.min(distances23+1, distances24);
}
if (validEdges030){
distances31=Math.min(distances31, distances40+1);
distances40=Math.min(distances31+1, distances40);
}
if (validEdges130){
distances30=Math.min(distances30, distances40+1);
distances40=Math.min(distances30+1, distances40);
}
if (validEdges230){
distances30=Math.min(distances30, distances41+1);
distances41=Math.min(distances30+1, distances41);
}
if (validEdges330){
distances30=Math.min(distances30, distances31+1);
distances31=Math.min(distances30+1, distances31);
}
if (validEdges031){
distances32=Math.min(distances32, distances41+1);
distances41=Math.min(distances32+1, distances41);
}
if (validEdges131){
distances31=Math.min(distances31, distances41+1);
distances41=Math.min(distances31+1, distances41);
}
if (validEdges231){
distances31=Math.min(distances31, distances42+1);
distances42=Math.min(distances31+1, distances42);
}
if (validEdges331){
distances31=Math.min(distances31, distances32+1);
distances32=Math.min(distances31+1, distances32);
}
if (validEdges032){
distances33=Math.min(distances33, distances42+1);
distances42=Math.min(distances33+1, distances42);
}
if (validEdges132){
distances32=Math.min(distances32, distances42+1);
distances42=Math.min(distances32+1, distances42);
}
if (validEdges232){
distances32=Math.min(distances32, distances43+1);
distances43=Math.min(distances32+1, distances43);
}
if (validEdges332){
distances32=Math.min(distances32, distances33+1);
distances33=Math.min(distances32+1, distances33);
}
if (validEdges033){
distances34=Math.min(distances34, distances43+1);
distances43=Math.min(distances34+1, distances43);
}
if (validEdges133){
distances33=Math.min(distances33, distances43+1);
distances43=Math.min(distances33+1, distances43);
}
if (validEdges233){
distances33=Math.min(distances33, distances44+1);
distances44=Math.min(distances33+1, distances44);
}
if (validEdges333){
distances33=Math.min(distances33, distances34+1);
distances34=Math.min(distances33+1, distances34);
}
System.out.println("Bytecodes used in setup and iteration: " + (Clock.getBytecodeNum() - initialBytecodeCount));
int bestX = 2;
int bestY = 2;
int minDistance = Integer.MAX_VALUE;
if (validEdges012 && distances13 < minDistance) {
bestX = 1;
bestY = 3;
minDistance=distances13;
}
if (validEdges021 && distances31 < minDistance) {
bestX = 3;
bestY = 1;
minDistance=distances31;
}
if (validEdges112 && distances12 < minDistance) {
bestX = 1;
bestY = 2;
minDistance=distances12;
}
if (validEdges122 && distances32 < minDistance) {
bestX = 3;
bestY = 2;
minDistance=distances32;
}
if (validEdges211 && distances11 < minDistance) {
bestX = 1;
bestY = 1;
minDistance=distances11;
}
if (validEdges222 && distances33 < minDistance) {
bestX = 3;
bestY = 3;
minDistance=distances33;
}
if (validEdges321 && distances21 < minDistance) {
bestX = 2;
bestY = 1;
minDistance=distances21;
}
if (validEdges322 && distances23 < minDistance) {
bestX = 2;
bestY = 3;
minDistance=distances23;
}
if (minDistance == Integer.MAX_VALUE) return false;
Direction bestDir = myLoc.directionTo(new MapLocation(myLoc.x + (bestX - 2), myLoc.y + (bestY - 2)));
System.out.println("Total bytecodes used: " + (Clock.getBytecodeNum() - initialBytecodeCount));
return tryMove(bestDir);
}
}
