package labPackage;

import java.io.IOException;  
import java.rmi.NotBoundException;










import navigation.Driver;
import navigation.odometryCorrection;
import lejos.hardware.*;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.remote.ev3.RemoteEV3;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Main {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2



	public static void main(String[] args)  {
		
		//ImportedData
		int llx = 2; //lower left x coordinate of ball rack, tiles
		int lly = 2; //lower left y coordinate of ball rack, tiles
		int urx = 6; //upper right x coordinate of ball rack, tiles
		int ury = 6; //upper right y coordinate of ball rack, tiles
		double angle = 0; //
		
		//Brick brick1 = null;
		Brick bricks1 = null; //named "EV3"
		RemoteRequestEV3 bricks2 = null; //named "EV1"
		//RegulatedMotor leftMotor;

		try {
			bricks1 = new RemoteEV3(BrickFinder.find("EV1")[0].getIPAddress()); //local
			bricks2 = new RemoteRequestEV3(BrickFinder.find("EV3")[0].getIPAddress()); //remote


			//RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("D"));
			//usPort1 = bricks1.getPort("S1");		
			//usPort2 = bricks1.getPort("S2");	

		} catch (IOException | NotBoundException e) {
			System.out.println("An error occured\nwhile assigning the\nbricks to the motors.");
			e.printStackTrace();
		}

		//bricks1.getAudio().playTone(Sound.BEEP, 100);

		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("A"));
		EV3LargeRegulatedMotor lowerArmMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("B"));
		EV3LargeRegulatedMotor higherArmMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("C"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(BrickFinder.getLocal().getPort("D"));
		RegulatedMotor usMotor = bricks2.createRegulatedMotor("B", 'M');
		RegulatedMotor shooter = bricks2.createRegulatedMotor("A", 'L');
		
		//Setup ultrasonic sensor

		Port uvPort1 = BrickFinder.getLocal().getPort("S1");
		Port uvPort2 = BrickFinder.getLocal().getPort("S2");
		Port colorPort1 =  BrickFinder.getLocal().getPort("S3");
		
		SensorModes usSensor1 = new EV3UltrasonicSensor(uvPort1);
		SensorModes usSensor2 = new EV3UltrasonicSensor(uvPort2);
		
		SampleProvider sampleProvider1 = usSensor1.getMode("Distance"); 
		SampleProvider sampleProvider2 = usSensor2.getMode("Distance"); 
		
		float[] usData1 = new float[sampleProvider1.sampleSize()];				// colorData is the buffer in which data are returned
		float[] usData2 = new float[sampleProvider2.sampleSize()];				// colorData is the buffer in which data are returned
		
		EV3ColorSensor colorSensor1 = new EV3ColorSensor(colorPort1);
		
		final SampleProvider colorValue1 = colorSensor1.getMode("Red");		
		final float[] colorData1 = new float[colorValue1.sampleSize()];	
		
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		LCDInfo lcd = new LCDInfo(odo, usSensor1, usData1, usSensor2, usData2);
		Navigation nav = new Navigation(odo);
		ShooterMilestone sho = new ShooterMilestone(lowerArmMotor, higherArmMotor, shooter, nav, colorValue1, colorData1, colorSensor1, angle);
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer( odo, sampleProvider1, usData1,sampleProvider2, usData2, USLocalizer.LocalizationType.FALLING_EDGE, 1);
		LightLocalizer lsl = new LightLocalizer(leftMotor, rightMotor, nav, odo, colorValue1, colorData1, colorSensor1);
		odometryCorrection cor = new odometryCorrection(nav, usMotor, 5, 5, odo, colorValue1, colorData1, colorSensor1);
		Driver driver = new Driver(nav, usMotor, 5, 5, odo, llx, lly, sampleProvider1, usData1);

		usl.doLocalization();
		lsl.doLocalization();
		
		//nav.travelTo(15,15);
		//nav.turnToNoError(0, true);
		

		//Button.waitForAnyPress();
		
		//nav.travelTo(125, 173);
		//nav.turnToNoError(90, true);
		
		driver.doDriver();
		
		sho.doShooter();
		//cor.run();
		//wallFollower.run();

		//perform the light sensor localization
		//LightLocalizer lsl = new LightLocalizer(leftMotor, rightMotor, nav, odo, colorValue1, colorData1, colorSensor1);
		//nav.travelTo(11.0,11.0);											// travel to where it's possible to do light localization
		//lsl.doLocalization();											// perform the light sensor localization
		//nav.travelTo(0,0);											// travel to origin
		//nav.turnTo(0, true);	
		//		
		Sound.beep();

		//leftMotor.close();
		//rightMotor.close();
		//bricks2.disConnect();       		//i think this is fucking things up
		//colorSensor1.close();
		//colorSensor2.close();
		//(Port)colorPort1.getClose();
		//ev3UltrasonicSensor1.close();
		//		ev3UltrasonicSensor2.close();
		//		colorSensor1.close();
		//		colorSensor2.close();

		
		try {
			Thread.sleep(12000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(0);	

	}


	
	
//	SampleProvider usValue1 = bricks2.createSampleProvider("S1", "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");;			// colorValue provides samples from this instance
	//		SampleProvider usValue2 = bricks2.createSampleProvider("S2", "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");;			// colorValue provides samples from this instance	
	//		float[] usData1 = new float[usValue1.sampleSize()];				// colorData is the buffer in which data are returned
	//		float[] usData2 = new float[usValue2.sampleSize()];				// colorData is the buffer in which data are returned



}