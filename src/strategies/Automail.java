package strategies;

import automail.CautiousRobot;
import automail.IMailDelivery;
import automail.Robot;

public class Automail {
	      
    public Robot[] robots;
    public IMailPool mailPool;
    
    public Automail(IMailPool mailPool, IMailDelivery delivery, int numRobots, boolean caution_enabled) {
    	// Swap between simple provided strategies and your strategies here
    	    	
    	/** Initialize the MailPool */
    	
    	this.mailPool = mailPool;
    	robots = new Robot[numRobots];
    	
    	/** Initialize robots */
    	if(caution_enabled) {
    		for (int i = 0; i < numRobots; i++) robots[i] = new CautiousRobot(delivery, mailPool);
    	}
    	else {
    		for (int i = 0; i < numRobots; i++) robots[i] = new Robot(delivery, mailPool);
    	}
    }
    
}
