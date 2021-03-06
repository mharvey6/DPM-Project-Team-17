package labPackage;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**Class for navigating close to the ball rack.
 * Robot to relocalize before ball loading. to use, use setupThreads(), runThreads() then killThreads() successively. three threads
 * incorporating obstacle avoidance and simple navigation
 * 
 * @author Ivan
 *
 */
public class GoCloseToBalls {
	Navigation nav;
	int lane;
	Odometer odo;
	Thread thread1;
	Thread thread2;
	int corner; //starting corner
	int lowerLeftX; //of ball rack
	int lowerLeftY;
	private SampleProvider usSensor1;
	private float[] usData1;
	boolean noObstacle = true;
	boolean travellingInX = true;
	boolean doneGoingNearBalls = false;
	int travelFactor = 1;
	
	/**constructor. reassigns parameters to same local names
	 * 
	 * @param nav
	 * @param odo
	 * @param lowerLeftX
	 * @param lowerLeftY
	 * @param usSensor1
	 * @param usData1
	 */
	public GoCloseToBalls(Navigation nav,Odometer odo,int lowerLeftX, int lowerLeftY, SampleProvider usSensor1, float[] usData1){
		this.nav = nav;
		this.lane = lane;
		this.odo = odo;
		this.lowerLeftX = lowerLeftX;
		this.lowerLeftY = lowerLeftY;
		this.usSensor1 = usSensor1;
		this.usData1 = usData1;
		setUpThreads();
		if(lowerLeftX < 6){
			travelFactor = -1;
		}
	}
	
	/**start threads. Begin navigating close to ball rack.
	 */
	public void runThreads(){
		thread1.start();
		thread2.start();
	}
	
	/**Not immediate. kills threads when navigation is done.
	 * wait until robot is within 7 cm square centered at intersection close to ball rack, then sets local variable doneGoingNearBalls to true.
	 * threads will then individually run out.
	 * 
	 * @author Ivan
	 */
	public void killThreads(){
		boolean stop = false;
		while(!stop){
			double xValue = odo.getX();
			double yValue = odo.getY();
			
			//exit condition
			if((xValue < (lowerLeftX-1)*30.96+7 && xValue > (lowerLeftX-1)*30.96-7)  &&  (yValue < (lowerLeftY-1)*30.96+7 && yValue > (lowerLeftY-1)*30.96-7)){
				stop = true;
				doneGoingNearBalls = true;
			}
			try {
				Thread.sleep(500); //check 2 times per second
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
		}
	}
	
	/**Necessary before running navigation.
	 * initializes and defines run function for each thread.
	 * thread1 repeatedly updates travel destination
	 * thread2 handles obstacle avoidance
	 * 
	 */
	public void setUpThreads(){
		thread1 = new Thread(){
			public void run(){
				while(!doneGoingNearBalls){
					while(noObstacle){
						nav.travelToBallsX((lowerLeftX-1)*30.96, odo.getY(), travelFactor);
						if(noObstacle){
							nav.turnToSimple(90, -1, 1);
							nav.travelToBallsY(odo.getX(), (lowerLeftY-1)*30.96);
						}
						Sound.beepSequenceUp();
					}
				}
			}
		};
		thread2 = new Thread(){
			public void run(){
				while(!doneGoingNearBalls){
					if(getFilteredData1() < 19){
						noObstacle = false;
						nav.setKeepTravelling(false);
						double xValue = odo.getX();
						nav.setSpeeds(0, 0);
						if (xValue < ((lowerLeftX-1)*30.96 - 5) || xValue > ((lowerLeftX-1)*30.96 +5)){ //hardcode left jog 
							nav.turnToSimple(90, -1, 1); //turn left by 90 deg
							nav.goForward(30.96);
							nav.turnToSimple(90,1, -1);    //remember this 0 changes depending where the ball rack is
						}
						else{
							nav.turnToSimple(90, -1, 1);
							nav.goForward(30.96);
							nav.turnToSimple(90,1,-1);
						}
					}
					nav.setKeepTravelling(true);
					noObstacle = true;
				}
			}
		};
	}
	
	/**Truncates value of US Sensor to be 60 cm or below
	 * 
	 * @return float. truncated value of US sensor, 60 cm or less
	 */
	private float getFilteredData1() {
		usSensor1.fetchSample(usData1, 0);
		float distance = usData1[0]*100;
		if (distance > 60)														
			distance = 60;																				
		return distance;
	}
}