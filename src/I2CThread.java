import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class I2CThread extends Thread{

    public static final int INA3221_ADDR = 0x40;
    public static final byte READ_DATA_1_BYTE = (byte)0x04;

    public static final int INA3221_REG_CONFIG = 0x00;
    public static final byte INA3221_CONFIG_ENABLE_CHAN1 = (byte)0x4000;
    public static final byte INA3221_CONFIG_ENABLE_CHAN2 =  (byte)0x2000;
    public static final byte INA3221_CONFIG_ENABLE_CHAN3 =  (byte)0x1000;
    public static final byte INA3221_CONFIG_AVG2 =  (byte)0x0800;
    public static final byte INA3221_CONFIG_AVG1 =  (byte)0x0400;
    public static final byte INA3221_CONFIG_AVG0 =  (byte)0x0200;
    public static final byte INA3221_CONFIG_VBUS_CT2 = (byte) 0x0100;
    public static final byte INA3221_CONFIG_VBUS_CT1 =  (byte)0x0080;
    public static final byte INA3221_CONFIG_VBUS_CT0 = (byte) 0x0040;
    public static final byte INA3221_CONFIG_VSH_CT2 =  (byte)0x0020;
    public static final byte INA3221_CONFIG_VSH_CT1 = (byte) 0x0010;
    public static final byte INA3221_CONFIG_VSH_CT0 =  (byte)0x0008;
    public static final byte INA3221_CONFIG_MODE_2 =  (byte)0x0004;
    public static final byte INA3221_CONFIG_MODE_1 = (byte) 0x0002;
    public static final byte INA3221_CONFIG_MODE_0 = (byte) 0x0001;

    I2CBus i2c;
    I2CDevice device;

    private double voltage1;
    MainWindow mw;

    public I2CThread(MainWindow maw) throws IOException, I2CFactory.UnsupportedBusNumberException {
        i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        device = i2c.getDevice(INA3221_ADDR);


        device.write(0x00,(byte)0b01110001);
        device.write((byte)0b00100111);

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

        this.mw=maw;
    }

    public double GetVoltage1(){
        return 0;
    }

    @Override
    public void run() {
        while(true){
            try {
//                this.wait(10);
                Thread.sleep(500);

                device.write(0x80, (byte)0x02);
                Integer up=-1;
                up = device.read(0x02);
                up = up & 0xFFFF;
                int low = (up & 0xFF00)>>8;
                int high = (up & 0x00FF)<<8;
                String tmp = Integer.toString(low) + Integer.toString(high);
                double resoult = Double.parseDouble(tmp);
                voltage1 = resoult*0.001;
                mw.configPortLbl.setText(String.format("%.2f", voltage1));
            } catch (IOException e) {
                e.printStackTrace();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
