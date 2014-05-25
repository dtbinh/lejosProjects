package balancer;

import lejos.hardware.Sound; 
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.HiTechnicGyro;
import lejos.robotics.EncoderMotor;

/**
 * <p>This class balances a two-wheeled Segway-like robot. It works with almost any construction 
 * (tall or short) such as the <a href="http://www.laurensvalk.com/nxt-2_0-only/anyway">Anyway</a> or
 * the <a href="http://www.hitechnic.com/blog/gyro-sensor/htway/">HTWay</a>. Wheel diameter is the most
 * important construction variable, which is specified in the constructor.</p> 
 * 
 * <p>To start the robot balancing:
 * <li>1. Run the program. You will be prompted to lay it down.
 * <li>2. Lay it down (orientation doesn't matter). When it detects it is not moving it will automatically calibrate the gyro sensor.
 * <li>3. When the beeping begins, stand it up so it is vertically balanced.
 * <li>4. When the beeping stops, let go and it will begin balancing on its own.</p>
 * 
 * <p>Alternately you can lean the robot against a wall and run the program. After the gyro 
 * calibration, the robot backs up against the wall until it falls forward. When it detects the
 * forward fall, it start the balance loop.</p>
 * 
 * <p>NOTE: In order to make the robot move and navigate, use the SegowayPilot class.</p> 
 * <p><i>This code is based on the <a href="http://www.hitechnic.com/blog/gyro-sensor/htway/">HTWay</a> by HiTechnic.</i></p>
 * 
 * @author Brian Bagnall
 * @author Barry Becker
 */
public class EV3Segoway implements FallListener {
  
	protected EncoderMotor left_motor;   // used to be EncoderMotor
    protected EncoderMotor right_motor;
   
    private final BalancingThread balancingThread;

    /**
     * Creates an instance of the Segoway, prompts the user to lay Segoway flat for gyro calibration,
     * then begins self-balancing thread. Wheel diameter is used in balancing equations.
     *  
     *  <li>NXT 1.0 wheels = 5.6 cm
     *  <li>NXT 2.0 wheels = 4.32 cm
     *  <li>RCX "motorcycle" wheels = 8.16 cm
     * 
     * @param left The left motor. An unregulated motor.
     * @param right The right motor. An unregulated motor.
     * @param gyro A HiTechnic gyro sensor
     * @param wheelDiameter diameter of wheel, preferably use cm (printed on side of LEGO tires in mm)
     */
    public EV3Segoway(EncoderMotor left, EncoderMotor right, HiTechnicGyro gyro, double wheelDiameter) {
    
    	this.left_motor = left;
        this.right_motor = right;
        float gyroBaseline = getGyroBaseline(gyro);

        // Play warning beep sequence before balance starts
        startBeeps();
        
        balancingThread = new BalancingThread(left, right, gyro, gyroBaseline, wheelDiameter, this);
        balancingThread.start();
    }
    
    /** steer the robot */
    public void wheelDriver(int left_wheel, int right_wheel) {
    	balancingThread.wheelDriver(left_wheel, right_wheel);
    }
    
    public void hasFallen(double elapsedTime) {
    	Sound.beepSequenceUp();
        System.out.println("Oops... I fell");
        System.out.println("Elapsed time: ");
        System.out.println((int)(elapsedTime*1000) + " seconds");
    }
    
    public void stop() {
        balancingThread.terminate();
    }
    
    /**
     * This function returns a suitable initial gyro offset.  It takes
     * N gyro samples over a time of 1/2 second and averages them to
     * get the offset.  It also check the max and min during that time
     * and if the difference is larger than one it rejects the data and
     * gets another set of samples.
     */
    private float getGyroBaseline(HiTechnicGyro gyro) {
    
        System.out.println("Lay robot down");
        System.out.println("to calibrate");
        System.out.println("the gyro");
        System.out.println();
        
        // read a few values and take the average
        float total = 0;
        float num = 100;
        try {
        	Thread.sleep(500);
	        for (int i=0; i<num; i++) {
	            total += getAngularVelocity(gyro);
	            Thread.sleep(10);
	        }
        }
        catch (InterruptedException e) {}
        float gyroBaseline = total / num;
        System.out.println("Gyro Baseline: " + gyroBaseline);
        return gyroBaseline;
    }
    
    /**
     * @return the calibrated angular velocity from the gyroscope sensor.
     */
    private float getAngularVelocity(HiTechnicGyro gyro) {
        float[] rate = new float[1];
        gyro.fetchSample(rate, 0);
        float rotSpeed = rate[0];
        return rotSpeed; 
    }

    /**
     * Warn user the Segoway is about to start balancing. 
     */
    private void startBeeps() {
        
    	System.out.println();
        System.out.println("Balance in");

        // Play warning beep sequence to indicate balance about to start
        for (int c=5; c>0;c--) {
            System.out.print(c + " ");
            Sound.playTone(440, 100);
            try { Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        System.out.println("GO!");
        System.out.println();
    }        
}