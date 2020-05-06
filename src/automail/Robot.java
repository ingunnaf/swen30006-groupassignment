package automail;

import exceptions.BreakingFragileItemException;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import strategies.IMailPool;
import java.util.Map;
import java.util.TreeMap;

/**
 * The robot delivers mail!
 */
public class Robot {
	
    static public final int INDIVIDUAL_MAX_WEIGHT = 2000;

    IMailDelivery delivery;
    protected final String id;
    /** Possible states the robot can be in */
    public enum RobotState { DELIVERING, WAITING, RETURNING }
    public enum NextDelivery { ARMS, DELIVITEM}
    public RobotState current_state;
    public NextDelivery nextDelivery;
    private int current_floor;
    private int destination_floor;
    private IMailPool mailPool;
    private boolean receivedDispatch;
    private BuildingSpecs specifications;
    
    private MailItem deliveryItem = null;
    private MailItem tube = null;
    private MailItem specialArm = null;
    
    private int deliveryCounter;
    

    /**
     * Initiates the robot's location at the start to be at the mailroom
     * also set it to be waiting for mail.
     * @param behaviour governs selection of mail items for delivery and behaviour on priority arrivals
     * @param delivery governs the final delivery
     * @param mailPool is the source of mail items
     */
    public Robot(IMailDelivery delivery, IMailPool mailPool){
    	id = "R" + hashCode();
        // current_state = RobotState.WAITING;
    	current_state = RobotState.RETURNING;
        current_floor = BuildingSpecs.MAILROOM_LOCATION;
        this.delivery = delivery;
        this.mailPool = mailPool;
        this.receivedDispatch = false;
        this.deliveryCounter = 0;
        this.specifications = new BuildingSpecs();
    }
    
    public void dispatch() {
    	receivedDispatch = true;
    }

    /**
     * This is called on every time step
     * @throws ExcessiveDeliveryException if robot delivers more than the capacity of the tube without refilling
     */
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
                	break;
                }
    		case WAITING:
                /** If the StorageTube is ready and the Robot is waiting in the mailroom then start the delivery */
                if(!isEmpty() && receivedDispatch){
                	receivedDispatch = false;
                	deliveryCounter = 0; // reset delivery counter
        			setRoute();
                	changeState(RobotState.DELIVERING);
                }
                break;
    		case DELIVERING:
    			boolean flagWrap = true;
    			if(specialArm!=null) {
    				if (!specialArm.isWrapped()) {
    					specialArm.wrap();
    					flagWrap = false;
    				}
    			}
    			
    			
    			
    			if(current_floor == destination_floor && flagWrap){ // If already here drop off either way
                    /** Delivery complete, report this to the simulator! */
    				if (nextDelivery == NextDelivery.ARMS) {
    					this.fragileProtocol();
    				}
    				else {
    					delivery.deliver(deliveryItem);
                    	deliveryItem = null;
                    	deliveryCounter++;
                    	if(deliveryCounter > 3){  // Implies a simulation bug
                    		throw new ExcessiveDeliveryException();
                    	}
                    	/** Check if want to return, i.e. if there is no item in the tube*/
                    	if(tube == null){
                    		changeState(RobotState.RETURNING);
                    	}
                    	else{
                        	/** If there is another item, set the robot's route to the location to deliver the item */
                        	deliveryItem = tube;
                        	tube = null;
                        	setRoute();
                        	changeState(RobotState.DELIVERING);
                    	}
    				}
    				
    			} else if (flagWrap) {
	        		/** The robot is not at the destination yet, move towards it! */
    				moveTowards(destination_floor);
    			}
                break;
    	}
    }

    /**
     * Sets the route for the robot
     */
    private void setRoute() {
        /** Set the destination floor */
    	if(specialArm!=null && deliveryItem!=null) {
    		if(specialArm.getDestFloor()>deliveryItem.getDestFloor()) {
    			destination_floor = specialArm.getDestFloor();
    			nextDelivery = NextDelivery.ARMS;
    		}
    		else {
    			destination_floor = deliveryItem.getDestFloor();
    			nextDelivery = NextDelivery.DELIVITEM;
    		}
    	}
    	else if (specialArm!=null) {
    		destination_floor = specialArm.getDestFloor();
			nextDelivery = NextDelivery.ARMS;
    	}
    	else {
    		destination_floor = deliveryItem.getDestFloor();
    		nextDelivery = NextDelivery.DELIVITEM;
    	}
    }

    
    /**
     * Generic function that moves the robot towards the destination if the destination is allowable
     * @param destination the floor towards which the robot is moving
     */
    private void moveTowards(int destination) {
    	specifications.leavingFloor(current_floor);
        if(current_floor < destination){
        	if(specifications.isAccessAllowed(current_floor+1)) {
        		current_floor++;
        	}
        } else {
        	if(specifications.isAccessAllowed(current_floor-1)) {
        		current_floor--;
        	}
        }
        specifications.enteringFloor(current_floor);
    }
    
    
    
    private void fragileProtocol() throws ExcessiveDeliveryException {
    	/*
		if fragile item, call clear floor check floor is clear every turn and then unwrap and deliver
		else deliver normal item
		*/
    	this.specifications.clearFloor(this.destination_floor);
    	
    	if(this.specifications.floorIsEmpty(destination_floor)) {
    		if(this.specialArm.isWrapped()) {
    			delivery.deliver(this.specialArm);
            	this.specialArm = null;
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
    			specialArm.unwrap();
    		}
    	}
    	
    }
    	
    
    private String getIdTube() {
    	return String.format("%s(%1d)", id, (tube == null ? 0 : 1));
    }
    
    private String getIdSpecialArm() {
    	return String.format("%s(%1d)", id, (specialArm == null ? 0 : 1));
    }
    
    private String getIdDeliveryItem() {
    	return String.format("%s(%1d)", id, (deliveryItem == null ? 0 : 1));
    }
    
    
    /**
     * Prints out the change in state
     * @param nextState the state to which the robot is transitioning
     */
    private void changeState(RobotState nextState){
    	assert(!(deliveryItem == null && tube != null));
    	if (current_state != nextState) {
    		if (nextDelivery==NextDelivery.DELIVITEM) {
    			System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdTube(), current_state, nextState);
    		}
    		else {
    			System.out.printf("T: %3d > %7s changed from %s to %s%n", Clock.Time(), getIdSpecialArm(), current_state, nextState);
    		}
    	}
    	current_state = nextState;
    	if(nextState == RobotState.DELIVERING){
    		if (nextDelivery==NextDelivery.DELIVITEM) {
    			System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdTube(), deliveryItem.toString());
    		}
    		else {
    			System.out.printf("T: %3d > %9s-> [%s]%n", Clock.Time(), getIdSpecialArm(), specialArm.toString());
    		}
    	}
    }
    

	public MailItem getTube() {
		return tube;
	}
	
	public MailItem getHand() {
		return deliveryItem;
	}
	
	public MailItem getArm() {
		return specialArm;
	}
    
	static private int count = 0;
	static private Map<Integer, Integer> hashMap = new TreeMap<Integer, Integer>();

	@Override
	public int hashCode() {
		Integer hash0 = super.hashCode();
		Integer hash = hashMap.get(hash0);
		if (hash == null) { hash = count++; hashMap.put(hash0, hash); }
		return hash;
	}

	public boolean isEmpty() {
		return (deliveryItem == null && tube == null && specialArm == null);
	}

	public void addToHand(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(deliveryItem == null);
		if(mailItem.fragile) throw new BreakingFragileItemException();
		deliveryItem = mailItem;
		if (deliveryItem.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}

	public void addToTube(MailItem mailItem) throws ItemTooHeavyException, BreakingFragileItemException {
		assert(tube == null);
		if(mailItem.fragile) throw new BreakingFragileItemException();
		tube = mailItem;
		if (tube.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}
	
	public void addToSpecialArm(MailItem mailItem) throws ItemTooHeavyException{
		assert(specialArm == null);
		specialArm = mailItem;
		if (specialArm.weight > INDIVIDUAL_MAX_WEIGHT) throw new ItemTooHeavyException();
	}


}
