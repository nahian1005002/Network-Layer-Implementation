

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class StartingClass {
	JFrame window;
	MainPanel mp;
	public StartingClass() {
		window = new JFrame("Assignment 3");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
//				/ 2 - this.getSize().height / 2);
		window.setSize(dim);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.add(new MainPanel());
		window.setVisible(true);
	}
	public static void main(String[] args) {
		new StartingClass();
	}
}
