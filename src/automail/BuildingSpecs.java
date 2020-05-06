package automail;

public class BuildingSpecs {
	
	
    /** The number of floors in the building **/
    public static int FLOORS;
    
    /** Represents the ground floor location */
    public static final int LOWEST_FLOOR = 1;
    
    /** Represents the mailroom location */
    public static final int MAILROOM_LOCATION = 1;
    
    private static boolean floorClearCalled[];
    private static int robotsOnFloor[];
    
    public BuildingSpecs() {
    	floorClearCalled = new boolean[FLOORS+1];
    	robotsOnFloor = new int[FLOORS+1];
    	for(int floor=0; floor<=FLOORS; floor++) {
    		floorClearCalled[floor] = false;
    		robotsOnFloor[floor] = 0;
    	}
    }
    
    public void clearFloor(int floor) {
    	floorClearCalled[floor]=true;
    }
    
    public void allowAccess(int floor) {
    	floorClearCalled[floor]=false;
    }
    
    public boolean isAccessAllowed(int floor) {
    	return !floorClearCalled[floor];
    }
    
    public void enteringFloor(int floor) {
    	robotsOnFloor[floor]++;
    }
    
    public void leavingFloor(int floor) {
    	robotsOnFloor[floor]--;
    }
    
    public boolean floorIsEmpty(int floor) {
    	System.out.printf("%d", robotsOnFloor[floor]);
    	if (robotsOnFloor[floor]==0 || robotsOnFloor[floor]==1) {
    		return true;
    	}
    	return false;
    }

}
