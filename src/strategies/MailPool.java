package strategies;

import java.util.LinkedList;
import java.util.Comparator;
import java.util.ListIterator;

import automail.MailItem;
import automail.Robot;
import exceptions.BreakingFragileItemException;
import exceptions.ItemTooHeavyException;

public class MailPool implements IMailPool {

	private class Item {
		int destination;
		MailItem mailItem;
		
		public Item(MailItem mailItem) {
			destination = mailItem.getDestFloor();
			this.mailItem = mailItem;
		}
	}
	
	public class ItemComparator implements Comparator<Item> {
		@Override
		public int compare(Item i1, Item i2) {
			int order = 0;
			if (i1.destination > i2.destination) {  // Further before closer
				order = 1;
			} else if (i1.destination < i2.destination) {
				order = -1;
			}
			return order;
		}
	}
	
	
	private LinkedList<Item> pool;
	private LinkedList<Item> fragilePool;
	private LinkedList<Robot> robots;

	public MailPool(int nrobots){
		// Start empty
		pool = new LinkedList<Item>();
		fragilePool = new LinkedList<Item>();
		robots = new LinkedList<Robot>();
	}

	public void addToPool(MailItem mailItem) {
		Item item = new Item(mailItem);
		if(mailItem.isFragile()) {
			fragilePool.add(item);
			fragilePool.sort(new ItemComparator());
		}
		else {
			pool.add(item);
			pool.sort(new ItemComparator());
		}
	}
	
	@Override
	public void step() throws ItemTooHeavyException, BreakingFragileItemException {
		try{
			ListIterator<Robot> i = robots.listIterator();
			while (i.hasNext()) loadRobot(i);
		} catch (Exception e) { 
            throw e; 
        } 
	}
	
	private void loadRobot(ListIterator<Robot> i) throws ItemTooHeavyException, BreakingFragileItemException {
		Robot robot = i.next();
		assert(robot.isEmpty());
		// System.out.printf("P: %3d%n", pool.size());
		ListIterator<Item> j = pool.listIterator();
		ListIterator<Item> k = fragilePool.listIterator();
		//System.out.println("robot");
		if (fragilePool.size() > 0){
			try {
				robot.addToSpecialArm(k.next().mailItem);
				System.out.printf("fragile add %s", robot.getArm().toString());
				System.out.println(" ");
				robot.dispatch(); // send the robot off if it has any items to deliver
				k.remove();
				if(pool.size()<=0) {
					i.remove();       // remove from mailPool queue
				}
			} catch (Exception e) {
				throw e;
			}
		}
		if (pool.size() > 0) {
			try {
			robot.addToHand(j.next().mailItem); // hand first as we want higher priority delivered first
			System.out.printf("normal add %s", robot.getHand().toString());
			System.out.println(" ");
			j.remove();
			if (pool.size() > 0) {
				robot.addToTube(j.next().mailItem);
				System.out.printf("tube add %s", robot.getTube().toString());
				System.out.println(" ");
				j.remove();
			}
			robot.dispatch(); // send the robot off if it has any items to deliver
			i.remove();       // remove from mailPool queue
			} catch (Exception e) { 
	            throw e; 
	        } 
		}
	}

	@Override
	public void registerWaiting(Robot robot) { // assumes won't be there already
		robots.add(robot);
	}

}
