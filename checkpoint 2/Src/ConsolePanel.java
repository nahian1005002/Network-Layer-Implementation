import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class ConsolePanel extends JPanel{
	JLabel jlName = new JLabel("Name");
	JTextArea jtSend = new JTextArea("Send massage: ");
	public static JTextArea jtReceived = new JTextArea("Received: ");
	public static JTextArea jtDebug = new JTextArea("Debug: ");
	public static JTextArea jtError = new JTextArea("Error: ");
	JButton jbSend = new JButton("Send");
	public ConsolePanel(String name) {
		setLayout(new GridLayout(3,2));
		jlName.setText(name);
		jbSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = jtSend.getText().toString();
				Res.MSG_TO_SEND = msg;
				Res.haveSomethingToRead = true;
				jtSend.setText("");
			}
		});
		
		add(jlName);
		add(jtReceived);
		add(jtSend);
		add(jbSend);
		add(jtDebug);
		add(jtError);
	}
}
