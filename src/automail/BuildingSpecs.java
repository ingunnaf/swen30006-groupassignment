package automail;

import exceptions.ExcessiveDeliveryException;

public class BuildingSpecs {
	
	
    /** The number of floors in the building **/
    public static int FLOORS;
    
    /** Represents the ground floor location */
    public static final int LOWEST_FLOOR = 1;
    
    /** Represents the mailroom location */
    public static final int MAILROOM_LOCATION = 1;
    
    private static boolean floorClearCalled[];
    private static int robotsOnFloor[];
    

    /**
     * Constructor for building specifications
     * Building specifications records which floors contain robots 
     * and which floors must be emptied for a fragile delivery
     */
    public BuildingSpecs() {
    	floorClearCalled = new boolean[FLOORS+1];
    	robotsOnFloor = new int[FLOORS+1];
    	for(int floor=0; floor<=FLOORS; floor++) {
    		floorClearCalled[floor] = false;
    		robotsOnFloor[floor] = 0;
    	}
    }
    
    /**
     * Records which floors are for use use by a fragile item delivery
     * @param floor indicates to which floor access needs to be prevented
     */
    public void clearFloor(int floor) {
    	floorClearCalled[floor]=true;
    }
    
    /**
     * Allows access to floor following a fragile item delivery
     * @param floor indicates floor to which access is being allowed
     */
    public void allowAccess(int floor) {
    	floorClearCalled[floor]=false;
    }
    
    /**
     * @returns if access to a floor is allowed
     */
    public boolean isAccessAllowed(int floor) {
    	return !floorClearCalled[floor];
    }
    
    /**
     * Used to maintain record of robot quantity per floor
     */
    public void enteringFloor(int floor) {
    	robotsOnFloor[floor]++;
    }
    
    /**
     * Used to maintain record of robot quantity per floor
     */
    public void leavingFloor(int floor) {
    	robotsOnFloor[floor]--;
    }
    
    /**
     * Used to determine if a floor is empty
     * @returns if floor is empty
     */
    public boolean floorIsEmpty(int floor) {
    	if (robotsOnFloor[floor]<=1) {
    		return true;
    	}
    	return false;
    }

}
