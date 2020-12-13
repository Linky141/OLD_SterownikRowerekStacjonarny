import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.impl.I2CDeviceImpl;
import com.pi4j.wiringpi.I2C;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

public class MainWindow  extends JFrame implements ActionListener {

    //region VARIABLES

    //region Window Controlls
    //region Buttons
        JButton btnReload;
    //endregion
    //region Lables
        public JLabel configPortLbl;
    //endregion
    //region Textbox
        JTextArea tbxTmp;
    //endregion
    //endregion

    //endregion

    I2CThread i2cThread = new I2CThread(this);


public MainWindow() throws IOException, I2CFactory.UnsupportedBusNumberException {
    //region Window
    setSize(640,480);
    setTitle("NewWindow");
    setLayout(null);
    //endregion

    //region Buttons
    btnReload = new JButton("REFRESH");
    btnReload.setBounds(10,50,50,30);
    btnReload.addActionListener(this);
    add(btnReload);
    //endregion

    //region Lables
    configPortLbl = new JLabel("---");
    configPortLbl.setBounds(10,200,200,30);
    add(configPortLbl);
    //endregion

    //region Textbox
    tbxTmp = new JTextArea();
    tbxTmp.setBounds(10,10,500,30);
    add(tbxTmp);
    //endregion

}



    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource()==btnReload) {
          i2cThread.start();
        }
    }
}
