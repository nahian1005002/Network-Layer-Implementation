import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel implements Runnable {
	JPanel upper, lower, middle;
	// upper
	JLabel jlDropRate = new JLabel("Drop Rate");
	JLabel jlErrorRate = new JLabel("Error Rate");
	JLabel jlHosts = new JLabel("Hosts");
	JLabel jlRouters = new JLabel("Routers");
	JLabel jlSwitches = new JLabel("Switches");
	JTextArea jtDropRate = new JTextArea(1, 5);
	JTextArea jtErrorRate = new JTextArea(1, 5);
	JTextArea jtHosts = new JTextArea(1, 15);
	JTextArea jtRouters = new JTextArea(1, 15);
	JTextArea jtSwitches = new JTextArea(1, 15);

	JButton jbStartConnectionDeamon = new JButton("Start Connection Daemon");
	JButton jbClearConnectionDaemonOutput = new JButton("Clear");
	
	ArrayList<ConsolePanel> cp = new ArrayList<ConsolePanel>();
	ArrayList<Out> ot = new ArrayList<Out>();
	// lower
	public static JTextArea jlConnectionDaemonOutput = new JTextArea(
			"Please start Connection Daemon");

	public MainPanel() {
		setLayout(new BorderLayout(10, 10));
		upper = new JPanel(new FlowLayout());
		middle = new JPanel(new FlowLayout());
		lower = new JPanel(new FlowLayout());

		// upper
		decorateLowerPanel();
		decorateUpperPanel();

		// add panels to main panel
		add(upper, BorderLayout.NORTH);

	}

	private void decorateUpperPanel() {
		upper.add(jlDropRate);
		upper.add(jtDropRate);
		upper.add(jlErrorRate);
		upper.add(jtErrorRate);
		upper.add(jlHosts);
		upper.add(jtHosts);
		upper.add(jlRouters);
		upper.add(jtRouters);
		upper.add(jlSwitches);
		upper.add(jtSwitches);
		jbStartConnectionDeamon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isValid = true;
				Double dropRate = 0.0, errorRate = 0.0;
				String dr = jtDropRate.getText().toString();
				String er = jtErrorRate.getText().toString();
				String inputedHosts = jtHosts.getText().toString();
				String inputedRouters = jtRouters.getText().toString();
				String inputedSwitches = jtSwitches.getText().toString();
				if (dr.equals("")) {
					dropRate = 0.0;
				} else {
					try {
						dropRate = Double.parseDouble(dr);
						if (dropRate >= 1.0) {
							JOptionPane.showMessageDialog(upper,
									"Invalid drop rate");
							jtDropRate.setText("");
							isValid = false;
						}
					} catch (NumberFormatException w) {
						w.printStackTrace();
						JOptionPane.showMessageDialog(upper, "Invalid number");
						jtDropRate.setText("");
						isValid = false;
					}
				}
				if (er.equals("")) {
					errorRate = 0.0;
				} else {
					try {
						errorRate = Double.parseDouble(er);
						if (errorRate >= 1.0) {
							JOptionPane.showMessageDialog(upper,
									"Invalid error rate");
							jtErrorRate.setText("");
							isValid = false;
						}
					} catch (NumberFormatException w) {
						w.printStackTrace();
						JOptionPane.showMessageDialog(upper, "Invalid number");
						jtErrorRate.setText("");
						isValid = false;
					}
				}
				if (isValid) {
					new ConnectionDaemon(dropRate, errorRate);
					jbStartConnectionDeamon.setEnabled(false);
					add(lower, BorderLayout.SOUTH);
					add(middle,BorderLayout.CENTER);

					if (inputedHosts.equals("")) {
						// isValid = false;
					} else {
						String[] hosts = inputedHosts.split(",");
						for (int i = 0; i < hosts.length; i++) {
							if (hosts[i].equals("")) {

							} else {
								ConsolePanel aCp = new ConsolePanel(hosts[i]);
								Out aOut = new Out();
								cp.add(aCp);
								ot.add(aOut);
								SimHost aSh = new SimHost(hosts[i]);
								//aCp.jlName.setText("test");
								
							}
						}
					}
					if (inputedRouters.equals("")) {
						// isValid = false;
					} else {
						String[] routers = inputedRouters.split(",");
						for (int i = 0; i < routers.length; i++) {
							if (routers[i].equals("")) {

							} else {
								new SimRouter(routers[i]);
								cp.add(new ConsolePanel(routers[i]));
								ot.add(new Out());
							}
						}
					}
					if (inputedSwitches.equals("")) {
						// isValid = false;
					} else {
						String[] switches = inputedSwitches.split(",");
						for (int i = 0; i < switches.length; i++) {
							if (switches[i].equals("")) {

							} else {
								new SimSwitch(switches[i]);
								cp.add(new ConsolePanel(switches[i]));
								ot.add(new Out());
							}
						}
					}
					for(int i = 0 ; i < cp.size();i++)
					{
						middle.add(cp.get(i));
					}
					revalidate();
					repaint();
				} else {
					add(lower, BorderLayout.SOUTH);
					revalidate();
					repaint();
				}
			}
		});
		upper.add(jbStartConnectionDeamon);
	}

	private void decorateLowerPanel() {
		jlConnectionDaemonOutput.setEditable(false);
		jlConnectionDaemonOutput.setColumns(100);
		lower.add(jlConnectionDaemonOutput);
		jbClearConnectionDaemonOutput.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Res.CD_OUT = "Cleared";
				MainPanel.jlConnectionDaemonOutput.setText(Res.CD_OUT);
			}
		});
		lower.add(jbClearConnectionDaemonOutput);
	}

	@Override
	public void run() {
		while (true) {
			jlConnectionDaemonOutput.setText(Res.CD_OUT);
			for(int i = 0 ; i < cp.size() ; i++)
			{
				cp.get(i).jtSend.setText(ot.get(i).SEND);
				cp.get(i).jtReceived.setText(ot.get(i).RECEIVED);
				cp.get(i).jtError.setText(ot.get(i).ERROR);
				cp.get(i).jtDebug.setText(ot.get(i).DEBUG);
			}
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					lower.revalidate();
					lower.repaint();
					middle.revalidate();
					middle.repaint();

				}
			});
		}
	}
}
