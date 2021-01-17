import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.impl.I2CDeviceImpl;
import com.pi4j.wiringpi.I2C;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Arrays;

public class MainWindow  extends JFrame implements ActionListener {

    //region VARIABLES
    CodeSourceThread codeSourceThread = new CodeSourceThread(this, this.baterryAndGeneratorInfo);
    //region Window Controlls
    //region Buttons
    JButton btnStartProgram;
    JButton btnStopProgram;
    JButton btnOutputMode;
    JButton btnCloseProgram;
    //endregion
    //region Lables
    JLabel lblSTBaterry;
    JLabel lblChargingMode;
    JLabel lblCurrentVoltage1;
    JLabel lblCurrentVoltage2;
    //endregion
    //region Textbox
    //endregion
    //region ProgressBar
    JProgressBar pbarVoltageInLimit;
    //endregion
    //endregion

    boolean customGui = true;
    boolean ChargeWithGenerator = false;
    BaterryAndGeneratorInfo baterryAndGeneratorInfo = new BaterryAndGeneratorInfo();

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput chargingGenerator = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "chargingGenerator", PinState.HIGH);
    final GpioPinDigitalOutput chargingExternal = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "chargingExternal", PinState.HIGH);
    final GpioPinDigitalOutput bateryOutput = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "bateryOutput", PinState.HIGH);

    //endregion

public MainWindow() throws IOException, I2CFactory.UnsupportedBusNumberException {

   //region batery info initialization
    baterryAndGeneratorInfo.bateryMinimumVoltage=10;
    baterryAndGeneratorInfo.bateryMaximumVoltage=12;
    baterryAndGeneratorInfo.bateryCriticalMinimumVoltage=6;
    baterryAndGeneratorInfo.baterryCurrentVoltage=-1;
    baterryAndGeneratorInfo.generatorCurrentVoltage=-1;
    baterryAndGeneratorInfo.chargingMode = false;
    //endregion

    chargingGenerator.setShutdownOptions(false, PinState.LOW);
    chargingExternal.setShutdownOptions(false, PinState.LOW);
    bateryOutput.setShutdownOptions(false, PinState.LOW);
    chargingGenerator.setState(false);
    chargingExternal.setState(false);
    bateryOutput.setState(false);

    //region Window
    int windowWidth = 640;
    int windowHeight = 480;
    setSize(windowWidth, windowHeight);
    setTitle("ROWEREK");
    if(customGui) getContentPane().setBackground(Color.darkGray);
    setLayout(null);
    //endregion

    //region Buttons
    btnStartProgram = new JButton("On");
    btnStartProgram.setBounds(10,500,200,30);
    if(customGui) btnStartProgram.setBackground(Color.darkGray);
    if(customGui) btnStartProgram.setForeground(Color.lightGray);
    if(customGui) btnStartProgram.setBorder(BorderFactory.createMatteBorder(2,6,2,6,Color.lightGray));
    if(customGui) btnStartProgram.setFocusPainted(false);
    btnStartProgram.addActionListener(this);
    add(btnStartProgram);

    btnStopProgram = new JButton("Off");
    btnStopProgram.setBounds(220,500,200,30);
    if(customGui) btnStopProgram.setBackground(Color.darkGray);
    if(customGui) btnStopProgram.setForeground(Color.lightGray);
    if(customGui) btnStopProgram.setBorder(BorderFactory.createMatteBorder(2,6,2,6,Color.lightGray));
    if(customGui) btnStopProgram.setFocusPainted(false);
    btnStopProgram.addActionListener(this);
    add(btnStopProgram);

    btnOutputMode = new JButton("Output power ON");
    btnOutputMode.setBounds(10,460,420,30);
    if(customGui) btnOutputMode.setBackground(Color.darkGray);
    if(customGui) btnOutputMode.setForeground(Color.lightGray);
    if(customGui) btnOutputMode.setBorder(BorderFactory.createMatteBorder(2,6,2,6,Color.lightGray));
    if(customGui) btnOutputMode.setFocusPainted(false);
    btnOutputMode.addActionListener(this);
    add(btnOutputMode);

    btnCloseProgram = new JButton("Close");
    btnCloseProgram.setBounds(10,700,100,30);
    if(customGui) btnCloseProgram.setBackground(Color.darkGray);
    if(customGui) btnCloseProgram.setForeground(Color.lightGray);
    if(customGui) btnCloseProgram.setBorder(BorderFactory.createMatteBorder(2,6,2,6,Color.lightGray));
    if(customGui) btnCloseProgram.setFocusPainted(false);
    btnCloseProgram.addActionListener(this);
    add(btnCloseProgram);
    //endregion

    //region Lables
    lblSTBaterry = new JLabel("Batery");
    lblSTBaterry.setBounds(10,10,100,30);
    if(customGui) lblSTBaterry.setForeground(Color.lightGray);
    add(lblSTBaterry);

    lblChargingMode = new JLabel("Work mode: ---");
    lblChargingMode.setBounds(10,130,300,30);
    if(customGui) lblChargingMode.setForeground(Color.lightGray);
    add(lblChargingMode);

    lblCurrentVoltage1 = new JLabel("Batery: -.--V");
    lblCurrentVoltage1.setBounds(100,40,200,30);
    if(customGui) lblCurrentVoltage1.setForeground(Color.lightGray);
    add(lblCurrentVoltage1);

    lblCurrentVoltage2 = new JLabel("Generator: -.--V");
    lblCurrentVoltage2.setBounds(10,90,200,30);
    if(customGui) lblCurrentVoltage2.setForeground(Color.lightGray);
    add(lblCurrentVoltage2);
    //endregion

    //region Textbox
    //endregion

    //region ProgressBar
    pbarVoltageInLimit = new JProgressBar();
    pbarVoltageInLimit.setBounds(90,10,500,30);
    pbarVoltageInLimit.setMinimum((int)baterryAndGeneratorInfo.bateryCriticalMinimumVoltage*100);
    pbarVoltageInLimit.setMaximum((int)baterryAndGeneratorInfo.bateryMaximumVoltage*100);
    pbarVoltageInLimit.setValue(0);
    if(customGui) pbarVoltageInLimit.setBackground(Color.darkGray);
    if(customGui) pbarVoltageInLimit.setForeground(Color.gray);
    if(customGui) pbarVoltageInLimit.setBorder(BorderFactory.createMatteBorder(6,1,6,1,Color.lightGray));
    add(pbarVoltageInLimit);
    //endregion

}


    public void SetVoltageLabel1(double voltage){
    lblCurrentVoltage1.setText("Batery: " + String.format("%.2f", voltage) + "V");
    if(voltage > baterryAndGeneratorInfo.bateryMaximumVoltage || voltage <0){
        if(voltage>baterryAndGeneratorInfo.bateryMaximumVoltage) {
            pbarVoltageInLimit.setValue((int)baterryAndGeneratorInfo.bateryMaximumVoltage*100);
        }
        else{
            pbarVoltageInLimit.setValue((int)baterryAndGeneratorInfo.bateryCriticalMinimumVoltage*100);
        }
    }
    else{
        pbarVoltageInLimit.setValue((int)(voltage*100));
    }
    }

    public void SetVoltageLabel2(double voltage){
        lblCurrentVoltage2.setText("Generator: " + String.format("%.2f", voltage) + "V");
    }

    public void setChargingExternal(boolean state){
    if(state) chargingExternal.high();
    else chargingExternal.low();
    }

    public void setChargingGenerator(boolean state){
        if(state) chargingGenerator.high();
        else chargingGenerator.low();
    }

    public void setBateryOutputState(boolean state){
        if(state) bateryOutput.high();
        else bateryOutput.low();
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource()==btnStartProgram) {
            if(!codeSourceThread.isAlive()) {
                try {
                    codeSourceThread = new CodeSourceThread(this, this.baterryAndGeneratorInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (I2CFactory.UnsupportedBusNumberException e) {
                    e.printStackTrace();
                }
                codeSourceThread.start();
            }
        }
        else if(actionEvent.getSource()==btnStopProgram){
            codeSourceThread.interrupt();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chargingGenerator.setState(false);
            chargingExternal.setState(false);
            bateryOutput.setState(false);
            lblChargingMode.setText("Work mode: ---");
            lblCurrentVoltage1.setText("Batery: -.--V");
            lblCurrentVoltage2.setText("Generator: -.--V");
            pbarVoltageInLimit.setValue((int)baterryAndGeneratorInfo.bateryCriticalMinimumVoltage);
        }
        else if(actionEvent.getSource()==btnOutputMode){
          if(ChargeWithGenerator){
              btnOutputMode.setBackground(Color.darkGray);
              ChargeWithGenerator = false;
          }
          else{
              btnOutputMode.setBackground(Color.black);
              ChargeWithGenerator = true;
          }
        }
        else if(actionEvent.getSource()==btnCloseProgram){
            codeSourceThread.interrupt();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chargingGenerator.setState(false);
            chargingExternal.setState(false);
            bateryOutput.setState(false);
            this.dispose();
        }
    }
}
