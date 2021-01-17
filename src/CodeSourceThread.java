import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class CodeSourceThread extends Thread {

    //region VARIABLES
    //region Device config variables
    public static final int INA3221_ADDR = 0x40;
    public static final byte READ_DATA_1_BYTE = (byte) 0x04;
    public static final int INA3221_REG_CONFIG = 0x00;
    public static final byte INA3221_CONFIG_ENABLE_CHAN1 = (byte) 0x4000;
    public static final byte INA3221_CONFIG_ENABLE_CHAN2 = (byte) 0x2000;
    public static final byte INA3221_CONFIG_ENABLE_CHAN3 = (byte) 0x1000;
    public static final byte INA3221_CONFIG_AVG2 = (byte) 0x0800;
    public static final byte INA3221_CONFIG_AVG1 = (byte) 0x0400;
    public static final byte INA3221_CONFIG_AVG0 = (byte) 0x0200;
    public static final byte INA3221_CONFIG_VBUS_CT2 = (byte) 0x0100;
    public static final byte INA3221_CONFIG_VBUS_CT1 = (byte) 0x0080;
    public static final byte INA3221_CONFIG_VBUS_CT0 = (byte) 0x0040;
    public static final byte INA3221_CONFIG_VSH_CT2 = (byte) 0x0020;
    public static final byte INA3221_CONFIG_VSH_CT1 = (byte) 0x0010;
    public static final byte INA3221_CONFIG_VSH_CT0 = (byte) 0x0008;
    public static final byte INA3221_CONFIG_MODE_2 = (byte) 0x0004;
    public static final byte INA3221_CONFIG_MODE_1 = (byte) 0x0002;
    public static final byte INA3221_CONFIG_MODE_0 = (byte) 0x0001;
//endregion

    BaterryAndGeneratorInfo batChargInfo;

    I2CBus i2c;
    I2CDevice device;

    MainWindow mainWindow;
//endregion

    public CodeSourceThread(MainWindow mainWindow, BaterryAndGeneratorInfo baterryAndGeneratorInfo) throws IOException, I2CFactory.UnsupportedBusNumberException {
        i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        device = i2c.getDevice(INA3221_ADDR);


        device.write(0x00, (byte) 0b01110001);
        device.write((byte) 0b00100111);

        byte config = INA3221_CONFIG_ENABLE_CHAN1 |
                INA3221_CONFIG_ENABLE_CHAN2 |
                INA3221_CONFIG_ENABLE_CHAN3 |
                INA3221_CONFIG_AVG1 |
                INA3221_CONFIG_VBUS_CT2 |
                INA3221_CONFIG_VSH_CT2 |
                INA3221_CONFIG_MODE_2 |
                INA3221_CONFIG_MODE_1 |
                INA3221_CONFIG_MODE_0;
        device.write(INA3221_REG_CONFIG, config);

        this.mainWindow = mainWindow;
        this.batChargInfo = baterryAndGeneratorInfo;
    }


    private double ReadVoltage1() throws IOException {

        device.write(0x80, (byte) 0x02);
        Integer up = -1;
        up = device.read(0x02);
        up = up & 0xFFFF;
        int low = (up & 0xFF00) >> 8;
        int high = (up & 0x00FF) << 8;
        double voltage2 = (double)(low+high); //calibrate measurment
        String tmp = Double.toString(voltage2);
        return Double.parseDouble(tmp) * 0.001;
    }

    private double ReadVoltage2() throws IOException {

        device.write(0x80, (byte) 0x04);
        Integer up = -1;
        up = device.read(0x04);
        up = up & 0xFFFF;
        int low = (up & 0xFF00) >> 8;
        int high = (up & 0x00FF) << 8;
        double voltage1 = (double)(low+high); //calibrate measurment
        String tmp = Double.toString(voltage1);
        return Double.parseDouble(tmp) * 0.001;
    }

    private void BateryCharge() throws InterruptedException {
        if (batChargInfo.chargingMode) {
            mainWindow.lblChargingMode.setText("Work mode: Charging");
            mainWindow.setChargingExternal(false);
            mainWindow.setChargingGenerator(true);
            Thread.sleep(100);
            mainWindow.setChargingGenerator(false);
        } else {
            if (batChargInfo.externalChargingMode) {
                mainWindow.lblChargingMode.setText("Work mode: External charging");
                mainWindow.setChargingGenerator(false);
                mainWindow.setChargingExternal(true);
                Thread.sleep(100);
                mainWindow.setChargingExternal(false);
            } else mainWindow.lblChargingMode.setText("Work mode: Discarging");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                batChargInfo.baterryCurrentVoltage = ReadVoltage1();
                batChargInfo.generatorCurrentVoltage = ReadVoltage2();

                if (batChargInfo.baterryCurrentVoltage > batChargInfo.bateryCriticalMinimumVoltage) {
                    batChargInfo.externalChargingMode = false;
                    if (batChargInfo.baterryCurrentVoltage <= batChargInfo.bateryMaximumVoltage && batChargInfo.generatorCurrentVoltage > batChargInfo.baterryCurrentVoltage && mainWindow.ChargeWithGenerator)
                        batChargInfo.chargingMode = true;
                    else batChargInfo.chargingMode = false;
                } else {
                    batChargInfo.chargingMode = false;
                    batChargInfo.externalChargingMode = true;
                }


                if (batChargInfo.chargingMode || batChargInfo.externalChargingMode)
                    mainWindow.setBateryOutputState(false);
                else mainWindow.setBateryOutputState(true);

                BateryCharge();


                mainWindow.SetVoltageLabel1(batChargInfo.baterryCurrentVoltage);
                mainWindow.SetVoltageLabel2(batChargInfo.generatorCurrentVoltage);


                Thread.sleep(10);
            } catch (InterruptedException | IOException e) {
                //e.printStackTrace();
                return;
            }
        }
    }
}
