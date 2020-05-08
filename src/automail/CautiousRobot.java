package automail;

import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;

/**
 * The robot delivers mail!
 */
public class CautiousRobot extends Robot{

    public enum NextDelivery { ARMS, DELIVITEM}

    private BuildingSpecs specifications;
    private int unableToUnwrap = 0;
    public NextDelivery nextDelivery;
    
    private MailItem specialArm = null;


	/**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param behaviour governs selection of mail items for delivery and behaviour on priority arrivals
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public CautiousRobot(IMailDelivery delivery, IMailPool mailPool){
    	super(delivery, mailPool);
        this.specifications = new BuildingSpecs();
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    @Override
    public void step() throws ExcessiveDeliveryException { 
    	switch(current_state) {
    		/** This state is triggered when the robot is returning to the mailroom after a delivery */
    		case RETURNING:
    			/** If its current position is at the mailroom, then the robot should change state */
                if(current_floor == BuildingSpecs.MAILROOM_LOCATION){
                	if (tube != null) {
                		mailPool.addToPool(tube);
                        System.out.printf("T: %3d >  +addToPool [%s]%n", Clock.Time(), tube.toString());
                        tube = null;
                	}
        			/** Tell the sorter the robot is ready */
        			mailPool.registerWaiting(this);
                	changeState(RobotState.WAITING);
                } else {
                	/** If the robot is not at the mailroom floor yet, then move towards it! */
                    moveTowards(BuildingSpecs.MAILROOM_LOCATION);
                }
                break;
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                	if(specialArm!=null && !specialArm.isWrapped()) {
                		//Item is not yet wrapped
                		wrapTime++;
        				specialArm.wrap();
        				System.out.printf("T: %3d > %7s WRAPPING item%n", Clock.Time(), super.getIdTube());
        			}
                	else {
                		//robot has received an item to deliver
                		receivedDispatch = false;
                		deliveryCounter = 0; // reset delivery counter
                		setRoute();
                		changeState(RobotState.DELIVERING);
                	}
                }
                else {
                	//waiting robot has no item to deliver. Remain waiting and check the delivery room is not blocked
                	this.preventDeliveryRoomBlockage();
                }
                break;
    		case DELIVERING:
    			if(current_floor == destination_floor){ // If already here drop off either way
    				if (nextDelivery == NextDelivery.ARMS) { //needs fragile delivery
    					this.fragileProtocol();
    				}
    				else {
    					/** complete ordinary delivery */
    					statsTrackNormDeliv();
    					delivery.deliver(deliveryItem);
    			    	deliveryItem = null;
    					deliveryCounter++;
                    	if(deliveryCounter > 3){  // Implies a simulation bug
                    		throw new ExcessiveDeliveryException();
                    	}
                    	/** Check if want to return, i.e. if there are no remaining items in the robot*/
                    	if(this.isEmpty()){
                    		changeState(RobotState.RETURNING);
                    	}
                    	else{
                        	/** If there is another item, set the robot's route to the location to deliver the item */
                    		if(tube!=null) { //move item from tube to hand
                    			deliveryItem = tube;
                    			tube = null;
                    		}
                        	setRoute();
                        	changeState(RobotState.DELIVERING);
                    	}
    				}
    				
    			} else {
	        		/** The robot is not at the destination yet, move towards it! */
    				moveTowards(destination_floor);
    			}
                break;
    	}
    }
    

    /**
     * Sets the first destination for the robot
     */
    private void setRoute() {
    	if(specialArm!=null && deliveryItem!=null) {
    		//set the destination floor to the highest delivery floor needed among objects held
    		if(specialArm.getDestFloor()>deliveryItem.getDestFloor()) {
    			//special item has the highest floor delivery
    			destination_floor = specialArm.getDestFloor();
    			nextDelivery = NextDelivery.ARMS;
    		}
    		else {
    			//delivery item has the highest floor of delivery
    			destination_floor = deliveryItem.getDestFloor();
    			nextDelivery = NextDelivery.DELIVITEM;
    		}
    	}
    	else if (specialArm!=null) {
    		//only have item in special arm to deliver
    		destination_floor = specialArm.getDestFloor();
			nextDelivery = NextDelivery.ARMS;
    	}
    	else {
    		//next delivery will be item in hands
    		destination_floor = deliveryItem.getDestFloor();
    		nextDelivery = NextDelivery.DELIVITEM;
    	}
    }
    
    
    /**
     * Generic function that moves the robot towards the destination if the destination is allowable
     * @param destination the floor towards which the robot is moving
     */
    private void moveTowards(int destination) {
    	specifications.leavingFloor(current_floor); //maintain records of robots per floor
        if(current_floor < destination){
        	if(specifications.isAccessAllowed(current_floor+1)) {
        		current_floor++;
        	}
        } else {
        	if(specifications.isAccessAllowed(current_floor-1)) {
        		current_floor--;
        	}
        }
        specifications.enteringFloor(current_floor); //maintain records of robots per floor
        shouldFloorBeRestricted();   
    }


    /**
     * Executes a fragile item delivery
     * * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
    private void fragileProtocol() throws ExcessiveDeliveryException {
    	specifications.clearFloor(destination_floor);
    	if(this.specifications.floorIsEmpty(destination_floor)) {
    		unableToUnwrap=0;
    		if(!this.specialArm.isWrapped()) {
    			this.statsTrackFragDeliv();
    			delivery.deliver(specialArm);
            	specialArm = null;
            	specifications.allowAccess(destination_floor);
            	deliveryCounter++;
            	if(deliveryCounter > 3){  // Implies a simulation bug
            		throw new ExcessiveDeliveryException();
            	}
            	/** Check if want to return, i.e. if there is no item in the tube*/
            	if(this.isEmpty()){
            		changeState(RobotState.RETURNING);
            	}
            	else{
                	/** If there is another item, set the robot's route to the location to deliver the item */
            		nextDelivery = NextDelivery.DELIVITEM;
                	setRoute();
                	changeState(RobotState.DELIVERING);	
            	}
    		}
    		else {
    			wrapTime+=1;
    			specialArm.unwrap();
    			System.out.printf("T: %3d > %7s UNWRAPPING item%n", Clock.Time(), super.getIdTube());
    		}
    	}
    	else {
    		executeDeadlockAvoidance();
    	}
    	
    } 
    

    /**
     * Ensures a deadlock will not occur
     */
    private void executeDeadlockAvoidance() {
    	unableToUnwrap++;
    	if(unableToUnwrap>5) {
			moveTowards(current_floor-1);
			specifications.allowAccess(destination_floor);
		}
    }
    
    /**
     * Prevents future robots from moving to this floor if a fragile delivery is about to take place
     */
    private void shouldFloorBeRestricted() {
    	 //don't allow further access to floor if a fragile item needs to be delivered
        if(current_floor==destination_floor && nextDelivery==NextDelivery.ARMS && current_state==RobotState.DELIVERING) {
        	this.specifications.clearFloor(this.destination_floor);
        }
    }
    
    
    /**
     * Prevents case where fragile delivery needs to be made to the mailroom location 
     * and there is a waiting robot blocking this process
     */
    private void preventDeliveryRoomBlockage() {
    	if(!specifications.isAccessAllowed(BuildingSpecs.MAILROOM_LOCATION)) {
    		moveTowards(BuildingSpecs.MAILROOM_LOCATION+1);
    		changeState(RobotState.RETURNING);
    	}
    }
    
    
    /**
     * Records statistics concerning a fragile delivery
     */
    private void statsTrackFragDeliv() {
		fragileWeight += specialArm.getWeight();
		fragileTotal ++;
    }
    
    
    private String getIdSpecialArm() {
    	return String.format("%s(%1d)", id, (specialArm == null ? 0 : 1));
    }
    
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot will transition
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
    		if (nextDelivery==NextDelivery.DELIVITEM) {
    			System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), super.getIdTube(), current_state, nextState);
    		}
    		else {
    			System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdSpecialArm(), current_state, nextState);
    		}
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
    		if (nextDelivery==NextDelivery.DELIVITEM) {
    			System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), super.getIdTube(), deliveryItem.toString());
    		}
    		else {
    			System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdSpecialArm(), specialArm.toString());
    		}
    	}
    }
	
	public MailItem getArm() {
		return specialArm;
	}
	
	@Override
	public boolean isEmpty() {
		return (deliveryItem == null && tube == null && specialArm == null);
	}
	
	@Override
	public void addToSpecialArm(MailItem mailItem) throws ItemTooHeavyException{
		assert(specialArm == null);
		specialArm = mailItem;
		if (specialArm.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}
}
