package gsxconfig;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings({"serial"})
public class GSXConfigUI extends JFrame {
	private final JLabel macLabel = new JLabel();
	private final JTextField ipTextField = new JTextField();
	private final JTextField ssidTextField = new JTextField();
	private JTextField pwTextField = new JPasswordField();
	private final JCheckBox hideCheckBox = new JCheckBox("Hide", true);
	private final JCheckBox credentialsOnlyCheckBox = new JCheckBox("Download wi-fi credentials only", true);
	private JButton configure;
	private final JProgressBar pb = new JProgressBar();
	private final JButton cancel = new JButton("Cancel");
	private final JCheckBox[] senseCBs = new JCheckBox[4];
	
	private static final String[] SECURITY_CHOICES = {
		"Open (Default)", // comes back as 'OPEN'
		"WEP-128", // comes back as 'WEP'
		"WPA1", // comes back as 'WPA1'
		"Mixed WPA1 & WPA2-PSK", // comes back as 'MIXED'
		"WPA2-PSK", // comes back as 'WPA2'
		"Not Used", // comes back as 'AUTO'
		"Adhoc, Join any Adhoc network" // comes back as 'ADHOC'
	};
	private final JComboBox securityComboBox = new JComboBox(SECURITY_CHOICES);
	
	public GSXConfigUI(ConfigureI config) {
		super("Device Configuration");
		init(config);
	}
	
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final int UI_WIDTH = 400;
	private static final int FIELD_HEIGHT = 30;
	
	public void init(final ConfigureI config) {
		//o("L&F: "+UIManager.getLookAndFeel());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container p = this.getContentPane();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		JPanel ipPanel = new JPanel();	
		ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.X_AXIS));
		ipPanel.add(new JLabel("IP:       "));
		ipTextField.setText(config.getIP());
		ipTextField.setMaximumSize(new Dimension(UI_WIDTH, FIELD_HEIGHT));
		ipPanel.add(ipTextField);
		final Container parent = this;
		
		final JobRunner uploadRunner = new JobRunner(null);

		final JDialog uploadDialog = new JDialog(this, "Waiting for Upload.", true);
		Container d = uploadDialog.getContentPane();
		d.setLayout(new BoxLayout(d, BoxLayout.Y_AXIS));
		
		JLabel waitLabel = new JLabel("Waiting for Upload...");
		waitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		d.add(waitLabel);
		JButton uploadCancel = new JButton("Cancel");
		uploadCancel.setAlignmentX(Component.CENTER_ALIGNMENT);

		d.add(uploadCancel);
		uploadCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadRunner.setCancelled(true);
			}
		});
		uploadDialog.setSize(UI_WIDTH/2, 100);
		
		final Executable uploadJob = new Executable() {
			public String execute(Progress p) {
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				return connectAndUpload(config, parent, p);
			}
		};
		
		final Callback uploadFinished = new Callback() {
			public void call(String msg) {
				uploadDialog.setVisible(false);
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				if (msg!=null) JOptionPane.showMessageDialog(parent, msg);
			}
		};
	
		final JButton connect = new JButton("Upload");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//connectAndUpload(config, parent);
				uploadRunner.execute(uploadJob, uploadFinished);	   
				int w = uploadDialog.getSize().width;
			    int h = uploadDialog.getSize().height;
			    int x = parent.getLocation().x+(parent.getWidth()-w)/2;
			    int y = parent.getLocation().y+(parent.getHeight()-h)/2;
			    // Move the window
			    uploadDialog.setLocation(x, y);
				uploadDialog.setVisible(true);
			}
		});
		ipPanel.add(connect);
		p.add(ipPanel);
		
		JPanel macPanel = new JPanel();	
		macPanel.setLayout(new BoxLayout(macPanel, BoxLayout.X_AXIS));
		macPanel.add(new JLabel("Device ID "));
		macLabel.setText("");
		macLabel.setMaximumSize(new Dimension(UI_WIDTH, FIELD_HEIGHT));

		macPanel.add(macLabel);
		p.add(macPanel);
		
		JPanel ssidPanel = new JPanel();	
		ssidPanel.setLayout(new BoxLayout(ssidPanel, BoxLayout.X_AXIS));
		ssidPanel.add(new JLabel("SSID:     "));
		ssidTextField.setText("");
		ssidTextField.setMaximumSize(new Dimension(UI_WIDTH, FIELD_HEIGHT));
		ssidPanel.add(ssidTextField);
		p.add(ssidPanel);
		
		JPanel securityPanel = new JPanel();
		securityPanel.setLayout(new BoxLayout(securityPanel, BoxLayout.X_AXIS));
		securityPanel.add(new JLabel("Security: "));
		securityPanel.add(securityComboBox);
		securityComboBox.setMaximumSize(new Dimension(UI_WIDTH, FIELD_HEIGHT));
		securityComboBox.setSelectedIndex(0);
		p.add(securityPanel);
		
		final JPanel pwPanel = new JPanel();	
		pwPanel.setLayout(new BoxLayout(pwPanel, BoxLayout.X_AXIS));
		pwPanel.add(new JLabel("PW:       "));
		pwTextField.setText("");
		pwTextField.setMaximumSize(new Dimension(UI_WIDTH, FIELD_HEIGHT));
		pwPanel.add(pwTextField);
		hideCheckBox.addChangeListener(new ChangeListener() {
			public  void stateChanged(ChangeEvent e) {
					String text = "";
					if (pwTextField!=null) text = pwTextField.getText();
					pwPanel.remove(pwTextField);
					pwTextField = hideCheckBox.isSelected() ? new JPasswordField() : new JTextField();
					pwTextField.setText(text);
					pwPanel.add(pwTextField, 1);
					pwPanel.validate();
			}
			 
		});
		pwPanel.add(hideCheckBox);
		p.add(pwPanel);
		
		final JPanel sensePanel = new JPanel();
		sensePanel.setLayout(new BoxLayout(sensePanel, BoxLayout.X_AXIS));
		sensePanel.add(new JLabel("Trigger: "));
		for (int i=0; i<4; i++) {
			senseCBs[i] = new JCheckBox("Sense"+i);
			sensePanel.add(senseCBs[i]);
		}
		p.add(sensePanel);
		p.add(credentialsOnlyCheckBox);
		
//		JPanel buttonPanel = new JPanel();
//		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		//buttonPanel.add(connect);
		
		configure = new JButton("Download");
		final Executable downloadJob = new Executable() {
				public String execute(Progress p) {
					return config.configure(p, credentialsOnlyCheckBox.isSelected());
				}
		};
		
		final Callback downloadFinished = new Callback() {
			public void call(String msg) {
				if (msg==null) msg = "Download complete.";
				JOptionPane.showMessageDialog(parent, msg);
				cancel.setEnabled(false);
				configure.setEnabled(false);	
				connect.setEnabled(true);
			}
		};
		
		final JobRunner downloadRunner = new JobRunner(pb);
		
		configure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Parameters p = config.getParameters();
					p.setTrigger(getTriggerValue(senseCBs));
					p.setSsid(ssidTextField.getText());
					int i = securityComboBox.getSelectedIndex();
					p.setAuth(i);
					if (Parameters.isWEP(i)) {
						p.setWepKey(pwTextField.getText());
						
					} else if (Parameters.isWPA(i)) {
						p.setPassPhrase(pwTextField.getText());						
					}
//					switch (i) {
//					case 1:
//						p.setWepKey(pwTextField.getText());
//						break;
//					case 2:
//					case 3:
//					case 4:
//						p.setPassPhrase(pwTextField.getText());
//						break;
//					}
					cancel.setEnabled(true);
					configure.setEnabled(false);
					connect.setEnabled(false);
					downloadRunner.execute(downloadJob, downloadFinished);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(parent, ex.getMessage());
					throw new RuntimeException(ex);
				}
			}
		});
		configure.setEnabled(false); // not until connect and upload successful
//		buttonPanel.add(configure);
		
//		p.add(buttonPanel);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
		pb.setMinimumSize(new Dimension(100, 50));
		progressPanel.add(configure);
		progressPanel.add(pb);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadRunner.setCancelled(true);
			}
		});

		cancel.setEnabled(false);
		progressPanel.add(cancel);
		p.add(progressPanel);
		
		//pack();
		this.setSize(UI_WIDTH, 250);
		
	    // Get the size of the screen
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    
	    // Determine the new location of the window
	    int w = getSize().width;
	    int h = getSize().height;
	    int x = (dim.width-w)/2;
	    int y = (dim.height-h)/2;
	    // Move the window
	    setLocation(x, y);
	    
	    this.addWindowListener(new WindowAdapter() {
	    	public void windowClosed(WindowEvent e) {
	    		//o("disconnecting...");
	    		config.disconnect();
	    		//o("... disconnected.  Exiting...");
	    		System.exit(0);
	    	}
	    });
	    
		setVisible(true);
		
	}
	
	public static int getTriggerValue(JCheckBox[] cbs) {
		int ans = 0;
		for (int i=0; i<cbs.length; i++) {
			if (cbs[i].isSelected()) {
				ans += (1<<i); // could also say ans |= (1<<i)
			}
		}
		return ans;
	}
	
	private String connectAndUpload(ConfigureI config, Container parent, Progress progress) {
		try {
			if (!config.isConnected()) config.connect(ipTextField.getText(), progress);
			config.retrieve();
			String mac = config.getMac();
			macLabel.setText(mac);
			Parameters params = config.getParameters();
			for (int i=0; i<4; i++) {
				senseCBs[i].setSelected((params.getTrigger() & (1<<i)) > 0);
			}
			ssidTextField.setText(params.getSsid());
			if (Parameters.isWPA(params.getAuth())) {
				pwTextField.setText(params.getPassPhrase());
			} else if (Parameters.isWEP(params.getAuth())) {
				pwTextField.setText(params.getWepKey());
			} else {
				pwTextField.setText("");
			}
			securityComboBox.setSelectedIndex(params.getAuth());
			configure.setEnabled(true);
			return null;
		} catch (Exception ex) {
			// pop up a dialog
			//throw new RuntimeException(ex);
			//JOptionPane.showMessageDialog(parent, ex.getMessage());
			ex.printStackTrace();
			return ex.getMessage();
		}
	}
	
//	private static void o(Object s) {System.out.println(s);}
}
