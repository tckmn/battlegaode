# initialize tunable parameters
radius = 2
iterations = 3

debug = True

# other parameter computation
n = 2 * radius + 1

# print class headers
print("package attacc;")
print('import battlecode.common.*;')
print('public class UnrolledNavigation extends Navigation {')
print('public UnrolledNavigation(RobotController r) {super(r);}')

# initialize valid locations
print("boolean navTo (MapLocation destination) throws GameActionException {")
if (debug): print("int initialBytecodeCount = Clock.getBytecodeNum();")
if (debug): print('System.out.println("Intelligent navigation to " + destination);')
print("MapLocation myLoc = rc.getLocation();")
for x in range(n):
  for y in range(n):
    print("int elevations" + str(x) + str(y) + ";")
    strLoc = "loc" + str(x) + str(y)
    print("MapLocation " + strLoc + " = new MapLocation(myLoc.x + " + str(x - radius) + ", myLoc.y + " + str(y - radius) + ");")
    print("boolean validLocs" + str(x) + str(y) + " = rc.canSenseLocation(" + strLoc + ") && !rc.senseFlooding(" + strLoc + ") && !rc.isLocationOccupied(" + strLoc + ");")
    print("if (validLocs" + str(x) + str(y) + ") elevations" + str(x) + str(y) + " = rc.senseElevation(" + strLoc + "); else elevations" + str(x) + str(y) + " = 0;")

print("validLocs" + str(radius) + str(radius) + " = true;")
print("elevations" + str(radius) + str(radius) + " = rc.senseElevation(loc" + str(radius) + str(radius) + ");")
if (debug): print('System.out.println("Bytecodes used in checking location validity: " + (Clock.getBytecodeNum() - initialBytecodeCount));')

# figure out which edges are valid
# TODO: actually do this correctly (with boundary conditions) here
for x in range(n-1):
  for y in range(n-1):
    print("boolean validEdges0" + str(x) + str(y) + " = (validLocs"+str(x)+""+str(y+1)+" && validLocs"+str(x+1)+""+str(y)+" && Math.abs(elevations"+str(x)+""+str(y+1)+" - elevations"+str(x+1)+""+str(y)+") <= 3);")
    print("boolean validEdges1" + str(x) + str(y) + " = (validLocs"+str(x)+""+str(y)+" && validLocs"+str(x+1)+""+str(y)+" && Math.abs(elevations"+str(x)+""+str(y)+" - elevations"+str(x+1)+""+str(y)+") <= 3);")
    print("boolean validEdges2" + str(x) + str(y) + " = (validLocs"+str(x)+""+str(y)+" && validLocs"+str(x+1)+""+str(y+1)+" && Math.abs(elevations"+str(x)+""+str(y)+" - elevations"+str(x+1)+""+str(y+1)+") <= 3);")
    print("boolean validEdges3" + str(x) + str(y) + " = (validLocs"+str(x)+""+str(y)+" && validLocs"+str(x)+""+str(y+1)+" && Math.abs(elevations"+str(x)+""+str(y)+" - elevations"+str(x)+""+str(y+1)+") <= 3);")

if (debug): print('System.out.println("Bytecodes used in setup: " + (Clock.getBytecodeNum() - initialBytecodeCount));')

print('int shiftedX = myLoc.x - destination.x - ' + str(radius) + ';')
print('int shiftedY = myLoc.y - destination.y - ' + str(radius) + ';')
for x in range(n):
  for y in range(n):
    print('int distances' + str(x) + str(y) + ' = (int)(Math.sqrt(Math.pow(shiftedX + ' + str(x) + ', 2) + Math.pow(shiftedY + ' + str(y) + ', 2)) * 100 + 0.5);')

if (debug): print('System.out.println("Bytecodes used in setup and distance initialization: " + (Clock.getBytecodeNum() - initialBytecodeCount));')

for counter in range(iterations):
  for x in range(n-1):
    for y in range(n-1):
      print('if (validEdges0'+str(x)+str(y)+'){')
      print('distances'+str(x)+str(y+1)+'=Math.min(distances'+str(x)+str(y+1)+', distances'+str(x+1)+str(y)+'+1);')
      print('distances'+str(x+1)+str(y)+'=Math.min(distances'+str(x)+str(y+1)+'+1, distances'+str(x+1)+str(y)+');')
      print('}')
      print('if (validEdges1'+str(x)+str(y)+'){')
      print('distances'+str(x)+str(y)+'=Math.min(distances'+str(x)+str(y)+', distances'+str(x+1)+str(y)+'+1);')
      print('distances'+str(x+1)+str(y)+'=Math.min(distances'+str(x)+str(y)+'+1, distances'+str(x+1)+str(y)+');')
      print('}')
      print('if (validEdges2'+str(x)+str(y)+'){')
      print('distances'+str(x)+str(y)+'=Math.min(distances'+str(x)+str(y)+', distances'+str(x+1)+str(y+1)+'+1);')
      print('distances'+str(x+1)+str(y+1)+'=Math.min(distances'+str(x)+str(y)+'+1, distances'+str(x+1)+str(y+1)+');')
      print('}')
      print('if (validEdges3'+str(x)+str(y)+'){')
      print('distances'+str(x)+str(y)+'=Math.min(distances'+str(x)+str(y)+', distances'+str(x)+str(y+1)+'+1);')
      print('distances'+str(x)+str(y+1)+'=Math.min(distances'+str(x)+str(y)+'+1, distances'+str(x)+str(y+1)+');')
      print('}')

if (debug): print('System.out.println("Bytecodes used in setup and iteration: " + (Clock.getBytecodeNum() - initialBytecodeCount));')

print('int bestX = '+str(radius)+';')
print('int bestY = '+str(radius)+';')
print('int minDistance = distances' + str(radius) + str(radius) +';')

print('if (validEdges0'+str(radius-1)+str(radius)+' && distances'+str(radius-1)+str(radius+1)+' < minDistance) {')
print('bestX = '+str(radius-1)+';')
print('bestY = '+str(radius+1)+';')
print('minDistance=distances'+str(radius-1)+str(radius+1)+';')
print('}')
print('if (validEdges0'+str(radius)+str(radius-1)+' && distances'+str(radius+1)+str(radius-1)+' < minDistance) {')
print('bestX = '+str(radius+1)+';')
print('bestY = '+str(radius-1)+';')
print('minDistance=distances'+str(radius+1)+str(radius-1)+';')
print('}')

print('if (validEdges1'+str(radius-1)+str(radius)+' && distances'+str(radius-1)+str(radius)+' < minDistance) {')
print('bestX = '+str(radius-1)+';')
print('bestY = '+str(radius)+';')
print('minDistance=distances'+str(radius-1)+str(radius)+';')
print('}')
print('if (validEdges1'+str(radius)+str(radius)+' && distances'+str(radius+1)+str(radius)+' < minDistance) {')
print('bestX = '+str(radius+1)+';')
print('bestY = '+str(radius)+';')
print('minDistance=distances'+str(radius+1)+str(radius)+';')
print('}')

print('if (validEdges2'+str(radius-1)+str(radius-1)+' && distances'+str(radius-1)+str(radius-1)+' < minDistance) {')
print('bestX = '+str(radius-1)+';')
print('bestY = '+str(radius-1)+';')
print('minDistance=distances'+str(radius-1)+str(radius-1)+';')
print('}')
print('if (validEdges2'+str(radius)+str(radius)+' && distances'+str(radius+1)+str(radius+1)+' < minDistance) {')
print('bestX = '+str(radius+1)+';')
print('bestY = '+str(radius+1)+';')
print('minDistance=distances'+str(radius+1)+str(radius+1)+';')
print('}')

print('if (validEdges3'+str(radius)+str(radius-1)+' && distances'+str(radius)+str(radius-1)+' < minDistance) {')
print('bestX = '+str(radius)+';')
print('bestY = '+str(radius-1)+';')
print('minDistance=distances'+str(radius)+str(radius-1)+';')
print('}')
print('if (validEdges3'+str(radius)+str(radius)+' && distances'+str(radius)+str(radius+1)+' < minDistance) {')
print('bestX = '+str(radius)+';')
print('bestY = '+str(radius+1)+';')
print('minDistance=distances'+str(radius)+str(radius+1)+';')
print('}')

print('if (minDistance == distances' + str(radius) + str(radius) + ') return false;')
print('Direction bestDir = myLoc.directionTo(new MapLocation(myLoc.x + (bestX - '+str(radius)+'), myLoc.y + (bestY - '+str(radius)+')));')
if(debug): print('System.out.println("Total bytecodes used: " + (Clock.getBytecodeNum() - initialBytecodeCount));')
print('return tryMove(bestDir);')
print('}')

print('}')
