package weigl.modelutil.test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import weigl.modelutil.BindField;
import weigl.modelutil.TransferMaster;

public class TestFrame extends JFrame implements ActionListener {
    
    @BindField("name")
    public JTextField txtName = new JTextField();
    
    @BindField("phone")    
    public JTextField txtPhone = new JTextField();
    
    JButton btn = new JButton("Set!");

    public TestFrame() {
	setLayout(new GridLayout(0, 2));
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	addC("Name", txtName);
	addC("Telefon", txtPhone);

	addC("", btn);

	btn.addActionListener(this);
    }

    private void addC(String string, JComponent txt) {
	JLabel lbl = new JLabel(string);
	add(lbl);
	lbl.setLabelFor(txt);
	add(txt);
    }

    public static void main(String[] args) throws IntrospectionException {
	TransferMaster.registerType(JTextField.class, "text");
	
	TestFrame tf = new TestFrame();
	tf.pack();
	tf.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	System.out.println("TestFrame.actionPerformed()");
	
	ModelBean mb = new ModelBean();
	TransferMaster.set(this, mb);
	System.out.println(mb);
    }
}
