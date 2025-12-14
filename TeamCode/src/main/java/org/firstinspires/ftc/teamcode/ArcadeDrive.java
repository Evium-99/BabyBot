package org. firstinspires.ftc. teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode. TeleOp;
import com.qualcomm.robotcore. hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Arcade Drive", group = "TeleOp")
public class ArcadeDrive extends OpMode {

    // Declare motors
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    // Slow mode variables
    private boolean slowMode = false;
    private boolean lastBumperState = false;

    @Override
    public void init() {
        // Initialize motors from hardware map
        frontLeft = hardwareMap.get(DcMotor.class, "fl");  // Front Left   | 0
        frontRight = hardwareMap.get(DcMotor.class, "fr");   // Front Right | 1
        backLeft = hardwareMap.get(DcMotor.class, "bl");  // Back Left     | 2
        backRight = hardwareMap.get(DcMotor.class, "br");  // Back Right   | 3

        // Set motor directions
        // Reverse left side motors so robot moves forward when positive power applied
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple. Direction.FORWARD);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple. Direction.FORWARD);

        // Set zero power behavior to brake
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior. BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Set motors to run without encoders (or use RUN_USING_ENCODER for more control)
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Handle slow mode toggle with left bumper
        boolean currentBumperState = gamepad1.left_bumper;
        if (currentBumperState && !lastBumperState) {
            // Button was just pressed (rising edge detection)
            slowMode = ! slowMode;
        }
        lastBumperState = currentBumperState;

        // Get joystick values for arcade drive
        // Left stick Y-axis controls forward/backward (drive)
        // Right stick X-axis controls turning
        double drive = -gamepad1.left_stick_y;  // Negative because stick is reversed
        double turn = gamepad1.right_stick_x;   // Positive values turn right

        // Calculate left and right motor powers using arcade drive formula
        double leftPower = drive + turn;
        double rightPower = drive - turn;

        // Normalize powers if they exceed 1.0
        double maxPower = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (maxPower > 1.0) {
            leftPower /= maxPower;
            rightPower /= maxPower;
        }

        // Apply slow mode if enabled (50% speed)
        if (slowMode) {
            leftPower *= 0.5;
            rightPower *= 0.5;
        }

        // Apply trigger speed reduction
        // Right trigger ranges from 0.0 to 1.0
        // When fully pressed (1.0), we want minimum speed
        // Speed multiplier = 1.0 - (trigger * reduction factor)
        double triggerReduction = gamepad1.right_trigger * 0.7; // Reduces up to 70% when fully pressed
        double speedMultiplier = 1.0 - triggerReduction;

        leftPower *= -speedMultiplier;
        rightPower *= -speedMultiplier;

        // Set motor powers
        frontLeft.setPower(leftPower);
        backLeft.setPower(leftPower);
        frontRight.setPower(rightPower);
        backRight.setPower(rightPower);

        // Display telemetry
        telemetry. addData("Status", "Running");
        telemetry.addData("Drive Mode", "ARCADE");
        telemetry. addData("Slow Mode", slowMode ? "ENABLED (50%)" : "DISABLED");
        telemetry.addData("Trigger Reduction", "%.1f%%", triggerReduction * 100);
        telemetry.addData("Effective Speed", "%.1f%%", speedMultiplier * (slowMode ? 50 : 100));
        telemetry.addData("Drive", "%.2f", drive);
        telemetry.addData("Turn", "%.2f", turn);
        telemetry.addData("Left Power", "%.2f", leftPower);
        telemetry.addData("Right Power", "%.2f", rightPower);
        telemetry.update();
    }

    @Override
    public void stop() {
        // Stop all motors when OpMode is stopped
        frontLeft. setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}