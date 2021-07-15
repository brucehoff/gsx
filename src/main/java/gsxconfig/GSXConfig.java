package gsxconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("all")
public class GSXConfig implements ConfigureI {
	
	private static final boolean VERBOSE = false;
	private static final boolean VERBOSE2 = false; // low level IO
	
	private static final String ip = "169.254.1.1";
	//private static final String ip = "192.168.2.5";
	
	private final Parameters params = new Parameters();

	public static void main(String[] args) throws Exception {
		ConfigureI config;
		if (true) {
			config = new GSXConfig();
		} else {
			// this is purely to test the User Interface
			config = new ConfigureI() {
				boolean isConnected = false;
				public String getIP() {return "111.222.333.444";}
				public boolean isConnected() {return isConnected;}
				public void connect(String ip, Progress p) {isConnected=true;}
				public void disconnect() {isConnected=false;}
				public void retrieve() {}
				public String getMac() {return "00:06:66:00:9C:82";}
				public Parameters getParameters() {
					Parameters p = new Parameters();
					return p;
				}
				public String configure(Progress p, boolean credentialsOnly) {
					// for testing:
					int n = 10;
					for (int i=0; i<n; i++) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException ie) {
						}
						p.set((double)i/(double)(n-1));
						if (p.isCancelled()) throw new RuntimeException("Cancelled");
					}
					return null;
				}
			};			
		}
		new GSXConfigUI(config);
	}
	
	public GSXConfig() {
	}
	
	private GSXSocketClient wifly = null;
//	private InputStream is = null;
//	private OutputStream os = null;
	
	public boolean isConnected() {return wifly != null;}
	
	public void waitForSubdomain(String ip, Progress progress) {
		while (true) {
			try {
				String addr = InetAddress.getLocalHost().getHostAddress();
				if (addr.startsWith(ip.substring(0, 7))) {
					break;
				}
			} catch (UnknownHostException uhe) {
				throw new RuntimeException(uhe);
			}
			if (progress!=null && progress.isCancelled()) throw new RuntimeException("Cancelled upon user request.");
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {}
		}
	}
	
	public void connect(String ip, Progress progress) {
		long startTime = System.currentTimeMillis();
		try {
			waitForSubdomain(ip, progress);
			//o("getLocalHost().getHostAddress returns "+InetAddress.getLocalHost().getHostAddress());
			String addr = InetAddress.getLocalHost().getHostAddress();
			//addr = addr.substring(addr.indexOf("/")+1);
			if (!addr.startsWith(ip.substring(0, 7))) {
				throw new RuntimeException("Not on correct domain.  This machine: "+addr+" but target IP: "+ip);
			}
			long beforeConnect = System.currentTimeMillis();
			wifly = new GSXSocketClient();
			wifly.connect(ip, 2000);
			//wifly.connect(new InetSocketAddress(ip, 2000), 6000/*millis*/);
			long delta = System.currentTimeMillis()-beforeConnect;
			String deltaS = delta<1000 ? ""+delta+" ms." : ""+(delta/1000)+" secs.";
			if (VERBOSE) o("GSXConfig.connect: connected after "+deltaS);
			if (VERBOSE) o("GSXConfig.connect: get streams");
//			is = wifly.getInputStream();
//			os = wifly.getOutputStream();
			if (VERBOSE) o("GSXConfig.connect: done");
			// make the process not so verbose
			// sendCommand("set broadcast interval 255", null);
			// keep the WiFly from disconnecting on us!
			//sendCommand("set com idle 0", "aok", null);
			sendCommand(Arrays.asList(new String[][]{{"set com idle 0", "aok"},{"set sys autoconn 0","aok"}}), null);
		} catch (Exception e) {
			if (wifly!=null) wifly.close();
			wifly = null;
//			is = null;
//			os = null;
			//if (getIP)
			throw new RuntimeException(e);
		} finally {
			if (VERBOSE) o("GSXConfig.connect: Time since start: "+(System.currentTimeMillis()-startTime)+" ms.");
		}
	}
	
	public void disconnect() {
		if (!isConnected()) return;
		wifly.close();
		wifly=null;
	}
	
	// returns informational message
	public String configure(Progress progress, boolean credentialsOnly) {
		try {
			// now set the parameters:
			if (true) { // operational settings
				params.setInterval(0);
				//params.setWakeTimer(3594); // wake up ea. 1 hour
				params.setWakeTimer(21594); // wake up ea. 6 hours
				//params.setWakeTimer(7195); // wake up ea. 2 hours
				params.setProtocol(18);
				params.setSleepTimer(7);
				params.setIdle(2);
				params.setAutoConn(1);
			} else { // settings that allow telnetting in
				params.setSleepTimer(0);
				params.setIdle(0);
				params.setInterval(0);	 // using 255 here seems to disable connecting to web			
				params.setWakeTimer(0); 
				params.setAutoConn(0);
				//params.setAutoConn(2); <<< For some reason this disables the GSX from talking by telnet
				params.setProtocol(18);
			}
			params.setDhcp(3);
			params.setIpAddr("0.0.0.0"); // per Wifly GSX Users' Guide: "so WiFly will use DNS"
			params.setHostAddr("0.0.0.0"); //  per Wifly GSX Users' Guide: "instructs RN-370 to use DNS address of host server"
			params.setNetMask("255.255.255.0");
			params.setDns("192.168.2.1");
			//params.setDns("0.0.0.0");
			params.setIpFlags(7);
			params.setSensors(255);
			// note, can't use 'omwatcher.com' with google app engine, because
			// google apps doesn't support redirection of such 'naked domains'
			params.setName("www.omwatcher.com"); 
			params.setHostPort(80);
			params.setJoinTimer(4500); // need >1000 for Cisco router
			String mac = getMac();
			if (true) {
				params.setFormat(23);
				params.setRemote("GET /om?id="+getMac()+"&e=");
			} else {
				params.setFormat(31);
				params.setRemote("GET /om?e=");
				// it seems like you can't start device-id with a number. 
				// if you put a char prefix in front of the mac, it works.
				// for me, it's not worth the bother.
				params.setDeviceId(mac);
			}
			params.setDeviceId("WiFly-GSX");
			params.setJoin(1);
			//params.setTimeEnable(1); //<<< once we get things working, and if using v 2.21 firmware
			// old versions of the firmware only allowed 0/1.  Newer versions (~2.31)
			// allow a period (minutes) for resetting the clock from the time server
			// this tells the system to wake up and reset clock each 5 minutes
			params.setTimeEnable(5);
			
			List<String[]> cmds = new ArrayList<String[]>();
			if (credentialsOnly) {
				cmds.addAll(params.credentialsOnlyConfigList());
			} else {
				cmds.addAll(params.configList());
			}
			cmds.add(new String[]{"save", ""});
			sendCommand(cmds, progress);
			
			// now double check:
			String s = getData("get everything");
			if (VERBOSE) o(s);
			Parameters params2 = new Parameters(s);
			// note the DNS server and net-mask param's don't appear correctly
			// until the module reboots on the LAN, so don't bother checking
			// Arrays.asList(new String[]{"Dns", "NetMask"})
			List<String> skip = new ArrayList<String>();
			skip.add("Dns");
			skip.add("NetMask");
			// if using WEP, don't confirm WPA param's.  If using WPA, don't confirm WEP params
			if (Parameters.isWEP(params.getAuth())) {
				skip.add("PassPhrase");
			} else if (Parameters.isWPA(params.getAuth())) {
				skip.add("Keynum");
				skip.add("WepKey");
			}
			// as of firmware version 2.3, the OPTION SENSOR command ("Sensor") is gone
			// the following should be version dependent, but for now we just omit
//			skip.add("Sensors");
			String diff = null;
			if (credentialsOnly) {
				diff = params.diffCredentialsOnly(params2);
			} else {
				diff = params.diff(params2, skip);
			}
			return diff==null ? null : "Failed to set all parameters:\n"+diff;
		} finally {
			wifly.close();
			disconnect();
		}
	}
	
	public String getIP() {return ip;}
	
	public String getMac() {
		String mac = getData("get mac");
		return parseMac(mac);
	}
		
	public void retrieve() {
		String s = getData("get everything");
		if (VERBOSE) o("GSXConfig.retrieve: 'get everything' returned \n"+s);
		params.populate(s);
	}
	
	public Parameters getParameters() {return params;}
	
	private static String parseMac(String s) {
		if (!s.startsWith("Mac Addr=")) throw new IllegalArgumentException("Illegal MAC address "+s);
		s = s.substring(9);
		int i = s.indexOf("\r");
		if (i>=0) s = s.substring(0,i);
		return s;
	}
	
	private static void o(Object s) {System.out.println(s);}
	
	private static final int NUM_TRIES = 3;
	/**
	 * 
	 * sending a command:
	 * 1. leave command mode ("Exit")
	 * 2. empty input buffer
	 * 3. enter command mode ("$$$")
	 * 4. verify that response was received ("CMD")
	 * 5. send the command
	 * 6. get response
	 * 7. verify response (Note, 'ack' is optional.  If omitted, no verification occurs.)
	 * 8. leave command mode
	 */
	private void sendCommand(List<String[]> cmdResps, Progress progress) {
		try {
			String s;
			if (true) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				sendOutput("exit"+EOL);
				s = drainInput(); // empty input buffer
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
			}
			for (int i=0; i<NUM_TRIES; i++) {
				sendOutput("$$$"+EOL);
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				s = drainInput().trim();
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				// I don't know why I don't get the whole "CMD" response!!
				if ((s.toUpperCase().startsWith("MD") || s.toUpperCase().startsWith("D"))) {
					// success!
					break;
				}
				if (i>=NUM_TRIES-1) {
					throw new RuntimeException("Failed to enter command mode.  Received <"+s+">");
				}
				o("sendCommand: received "+s+" but expected 'CMD'.  Will retry.");
			}
			int counter = 0;
			for (String[] cmdResp : cmdResps) {
				double progressLevel = (double)(counter++)/(double)cmdResps.size();
				if (progress!=null) progress.set(progressLevel);
				if (progress!=null && progress.isCancelled()) throw new RuntimeException("Cancelled upon user request.");
				String cmd = cmdResp[0];
				String ack = cmdResp[1];
				sendOutput(cmd+EOL);
				try {
					Thread.sleep(1000L); // 4/14/2013 v. 2.38:  to get 'save' to work, had to increase from 250L to 1000L
				} catch (InterruptedException ie) {
				}
				s = drainInput().trim();
				// command is echoed back, missing the first char....
				int i = s.indexOf(cmd.substring(1));
				if (i>=0) { 
					s = s.substring(i+cmd.length()-1).toUpperCase().trim();
				} else {
					// ... or the second char
					i = s.indexOf(cmd.substring(2));
					if (i>=0) {
						s = s.substring(i+cmd.length()-2).toUpperCase().trim();	
					}
				}
				if (ack!=null && !s.startsWith(ack.toUpperCase())) {
					//o("Failed to execute command.  Expected <"+ack+">, but received <"+s+">");
					throw new RuntimeException("Failed to execute command "+cmd+".  Expected <"+ack+">, but received <"+s+">");
				}
			}
			sendOutput("exit"+EOL);
			s = drainInput(); // empty input buffer
			//o("Final drain: <<"+s+">>");
			if (progress!=null) progress.set(1d);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
//	// if you need to send just one command...
//	private void sendCommand(String cmd, String ack, Progress progress) {
//		sendCommand(Arrays.asList(new String[][]{{cmd,ack}}), progress);
//	}
	
	private static final int GET_DATA_RETRY_COUNT = 3;
	
	/**
	 * 
	 * getting a value:
	 * 1. leave command mode ("Exit")
	 * 2. empty input buffer
	 * 3. enter command mode ("$$$")
	 * 4. verify that response was received ("CMD")
	 * 5. send the 'get' command
	 * 6. get response
	 * 7. leave command mode
	 */
	private String getData(String getCmd) {
		for (int retryCounter=0; retryCounter<GET_DATA_RETRY_COUNT; retryCounter++) {
			try {
				if (VERBOSE) o("GSCConfig.getData...");
				sendOutput("exit"+EOL);
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				String s = drainInput(); // empty input buffer
				sendOutput("$$$"+EOL);
				s = drainInput().trim();
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				// I don't know why I don't get the whole "CMD" response!!
				// 3-2-12 for Mac change 'MD' to 'M'
				if (!(s.toUpperCase().startsWith("M") || s.toUpperCase().startsWith("D"))) {
					throw new IOException("Failed to enter command mode.  Received <"+s+">");
				}
				sendOutput(getCmd+EOL);
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				String ans = drainInput().trim();
				// command is echoed back, missing the first char:
				int i = ans.indexOf(getCmd.substring(1));
				if (i>=0) ans = ans.substring(i+getCmd.length()-1).trim();
				sendOutput("exit"+EOL);
				s = drainInput(); // empty input buffer
				if (VERBOSE) o("GSCConfig.getData...done.");
				return ans;
			} catch (IOException ioe) {
				o("Hit exception but will retry: "+ioe.getMessage());
				if (retryCounter>=GET_DATA_RETRY_COUNT-1) throw new RuntimeException(ioe);
			}
		}
		// if we reach this point we've exhausted the retries
		throw new RuntimeException("GetData: Max retries exceeeded for "+getCmd);
	}

	
	private static final String EOL = "\r\n";
	
	public String drainInput() throws IOException {
		StringBuffer sb = new StringBuffer();
		String s;
		boolean firstTime = true;
		InputStream is = wifly.getInputStream();
		do {
			s = firstTime ? getInput(is, 200L) : getInput(is);
			firstTime=false;
			if (VERBOSE2) o("GSXConfig.drainInput: Stream says: <<"+s+">>");
			if (s!=null && s.length()>0) sb.append(s);
		} while (s!=null && s.length()>0);
		return sb.toString();
	}
	
	// private static final int MAX = 200;

	// gets the next line
	// times out after 0.1 sec
	public static String getInput(final InputStream is) throws IOException {
		return getInput(is, 50L);
	}

	public static String getInput(final InputStream is, long joinDelay) throws IOException {
		if (VERBOSE2) o("GSXConfig.getInput...");
		StringBuffer sb = new StringBuffer();
		final MutableInt mi = new MutableInt();
		int i;
//		int count=0;
		do {
			//o("about to read...");
			Thread thread = new Thread() {
				public void run() {
					try {
						mi.set(is.read());
					} catch (IOException e) {
							e.printStackTrace();
							mi.set(-1);
					}
				}
			};
			mi.set(-1);
			thread.start();
			try {
				thread.join(joinDelay);
			} catch (InterruptedException ie) {
			}
			i = mi.get();
			//o("...read");
			if (i>0) sb.append((char)i);
			if (sb.length()>0 && (sb.length() % 200 == 0)) o("\t"+sb);
		} while (/*i>=0 &&*/ i>=32 /*&& count++<MAX*/); // terminates on 10, 13, -1
		if (VERBOSE2) o("GSXConfig.getInput: last char: "+i);
		if (VERBOSE2) o("GSXConfig.getInput...done.");
		return sb.toString();
	}
	
	private void sendOutput(String s) throws IOException {
		if (VERBOSE2) o("\tGSCConfig.sendOutput for "+s+"...");
		OutputStream os = wifly.getOutputStream();
		os.write(s.getBytes());
		os.flush();
		if (VERBOSE2) o("\tGSCConfig.sendOutput...done.");
	}
}

