package de.swa.ui;

import javax.swing.*;
import java.awt.*;

/**
 * progress frame indicating the processing status at startup of the GMAF
 **/
public class ProgressFrame extends JFrame implements ProgressListener {
	private static ProgressFrame instance;
	private JProgressBar progress = new JProgressBar();
	private JLabel message = new JLabel("starting GMAF application");

	public ProgressFrame() {
		instance = this;

		setSize(500, 250);
		setUndecorated(true);
		progress.setBackground(Color.white);
		progress.setForeground(new Color(200, 200, 200));
		Dimension d = new Dimension();
		d.height = 20;
		progress.setPreferredSize(d);

		progress.setSize(new Dimension(500,100));
		progress.setValue(0);
		message.setText("GMAF Framework loading...");

		setTitle("GMAF Framework loading...");
		setLayout(new BorderLayout());
		add(message, "South");
		add(new JLabel(new ImageIcon("resources/splash.png")), "North");
		add(progress, "Center");
		pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - 500) / 2);
		int y = (int) ((dimension.getHeight() - 250) / 2);
		setLocation(x, y);



		MMFGCollection.getInstance().addProgressListener(this);
		setVisible(true);
		

	}

	public static ProgressFrame getInstance() {
		if (instance == null) {
			new ProgressFrame();
		}
		return instance;
	}

	public void log(int progress, String s) {
		setProgress(progress, s);
	}

	public void setProgress(int value, String s) {
		progress.setValue(value);
		message.setText(s);
	}

	public void close() {
		setVisible(false);
	}
}
