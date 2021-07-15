package gsxconfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Parameters {
	// adhoc
	private int beacon;
	private int probe;
	// broadcast
	private String address;
	private int port;
	private int interval;
	// com
	private char cmdChar;
	private String open;
	private String close;
	private String remote; // remember that spaces are replaced with $ when sending to GSX
	private int idle;
	private int match;
	private int size;
	private int timer;
	/*
	DNS=192.168.2.1
	Name=www.omwatcher.appspot.com
	Backup=backup2
	 */
	private String dns;
	private String name;
	private String backup;
	
	/* 
	FTP (skipping for now...):
	FTP=208.109.78.34:21
	File=etage=xmessage=set
	User=roving
	Pass=Pass123
	 */
	/*
	IP:
	IF=UP
	DHCP=ON
	IP=192.168.2.4:2000
	NM=255.255.255.0
	GW=192.168.2.1
	HOST=72.14.213.141:80
	PROTO=TCP,HTTP,
	MTU=1460
	FLAGS=0x7
	BACKUP=0.0.0.0
	 */
	private String ipIf;
	private int dhcp;
	private String ipAddr;
	private int ipPort;
	private String netMask;
	private String gateway;
	private String hostAddr;
	public int getHostPort() {
		return hostPort;
	}

	public void setHostPort(int hostPort) {
		this.hostPort = hostPort;
	}

	private int hostPort;
	private int protocol;
	private int mtu;
	private int ipFlags;
	private String backupAddr;

	/*
	OPTION:
	JoinTmr=1000
	Replace=0x24
	DeviceId=WiFly-GSX
	Password=
	Format=0x1f
	Sensors=0xff As of v 2.3. this field is 'Sensor=0x...', and is set by 'set q sensor', not 'set option sensor'
	 */	
	private int joinTimer;
	private int replace;
	private String deviceId;
	private String password;
	private int format;
	private int sensors;
	
	/*
	System:
	SleepTmr=0
	WakeTmr=0
	Trigger=0x2
	Autoconn=1
	IoFunc=0x0
	IoMask=0x20f0
	PrintLvl=0x1
	 */
	private int sleepTimer;
	private int wakeTimer;
	private int trigger;
	private int autoConn;
	private int ioFunc;
	private int ioMask;
	private int printLvl;
	/*
	 * the command "get wlan" returns
	SSID=HoffsHomeNet
	Chan=0
	ExtAnt=0
	Join=1
	Auth=MIXED
	Mask=0x1fff
	Rate=12, 24 Mb
	Linkmon=0
	Passphrase=xxxxx

	If you use WEP the command returns
	SSID=HoffsHomeNet
	Chan=0
	ExtAnt=0
	Join=1
	Auth=WEP
	Mask=0x1fff
	Rate=12, 24 Mb
	Linkmon=0
	Keynum=1
	Key=00 00 00 00 00 00 00 00 00 00 00 00 00
	 */
	private String ssid;
	private int chan;
	private int extAnt;
	private int join;
	private int auth;
	private int mask;
	private int rate;
	private int linkMon;
	private String passPhrase;
	private int keynum;
	private String wepKey;
	
	private int timeEnable;
	
	private int uartMode;

	public Parameters() {}
	
	public Parameters(String s) {
		populate(s);
	} 
	
	public void populate(String s) {
		setBeacon(Integer.parseInt(extract(s, "Beacon=")));
		setProbe(Integer.parseInt(extract(s, "Probe=")));
		String bcast = extract(s, "BCAST=");
		int colon = bcast.indexOf(":");
		if (colon<0) throw new RuntimeException(bcast);
		setAddress(bcast.substring(0,colon));
		setPort(Integer.parseInt(bcast.substring(colon+1)));
		setInterval(Integer.parseInt(extract(s, "Interval=0x"),16));
		setOpen(extract(s, "OPEN="));
		setClose(extract(s, "CLOSE="));
		setRemote(extract(s, "REMOTE="));
		setIdle(Integer.parseInt(extract(s, "IdleTimer=")));
		setMatch(Integer.parseInt(extract(s, "MatchChar=")));
		setSize(Integer.parseInt(extract(s, "FlushSize=")));
		setTimer(Integer.parseInt(extract(s, "FlushTimer=")));
		setCmdChar(extract(s, "CmdChar=").charAt(0));
		
		// DNS
		setDns(extract(s, "DNS="));
		setName(extract(s, "Name="));
		setBackup(extract(s, "Backup="));
		
		// IP
		setIpIf(extract(s, "IF="));
		String dhcpString = extract(s, "DHCP=");
		int dhcp = 0;
		if ("OFF".equalsIgnoreCase(dhcpString)) {
			dhcp = 0;
		} else if ("ON".equalsIgnoreCase(dhcpString)) {
			dhcp = 1;
		} else if ("CACHE".equalsIgnoreCase(dhcpString)) {
			dhcp = 3;
		} else {
			throw new IllegalArgumentException(dhcpString);
		}
		setDhcp(dhcp);
		String ip = extract(s, "IP=");
		colon = ip.indexOf(":");
		if (colon<0) throw new RuntimeException(ip);
		setIpAddr(ip.substring(0, colon));
		setIpPort(Integer.parseInt(ip.substring(colon+1)));
		setNetMask(extract(s, "NM="));
		setGateway(extract(s, "GW="));
		String host = extract(s, "HOST=");
		colon = host.indexOf(":");
		if (colon<0) throw new RuntimeException(host);
		setHostAddr(host.substring(0, colon));
		setHostPort(Integer.parseInt(host.substring(colon+1)));
		
		String proto = extract(s, "PROTO=");
		int protoCode = 0;
		// need to find other choices!!
		if (proto.indexOf("UDP")>=0) protoCode |= 1;
		if (proto.indexOf("TCP")>=0) protoCode |= 2;
		if (proto.indexOf("HTTP")>=0) protoCode |= 16;
		setProtocol(protoCode);
		
		setMtu(Integer.parseInt(extract(s, "MTU=")));
		setIpFlags(Integer.parseInt(extract(s, "FLAGS=0x"), 16));
		setBackupAddr(extract(s, "BACKUP="));
		
		// System
		setSleepTimer(Integer.parseInt(extract(s, "SleepTmr=")));
		setWakeTimer(Integer.parseInt(extract(s, "WakeTmr=")));
		setTrigger(Integer.parseInt(extract(s, "Trigger=0x"),16));
		setAutoConn(Integer.parseInt(extract(s, "Autoconn=")));
		setIoFunc(Integer.parseInt(extract(s, "IoFunc=0x"),16));
		setIoMask(Integer.parseInt(extract(s, "IoMask=0x"),16));
		setPrintLvl(Integer.parseInt(extract(s, "PrintLvl=0x"),16));
		
		// Option
		setJoinTimer(Integer.parseInt(extract(s, "JoinTmr=")));
		setReplace(Integer.parseInt(extract(s, "Replace=0x"),16));
		setDeviceId(extract(s, "DeviceId="));
		setPassword(extract(s, "Password="));
		String format = extract(s, "Format=0x");
		if (format==null || format.length()==0) {
			setFormat(0);
		} else {
			setFormat(Integer.parseInt(format,16));
		}
		// before v. 2.23 this was 'Sensors=...'
		String sensors = extract(s, "Sensor=0x");
		if (sensors==null || sensors.length()==0) {
			setSensors(0);
		} else {
			setSensors(Integer.parseInt(sensors,16));
		}
		
		// wlan
		setSsid(extract(s, "SSID="));
		setChan(Integer.parseInt(extract(s, "Chan=")));
		setExtAnt(Integer.parseInt(extract(s, "ExtAnt=")));
		setJoin(Integer.parseInt(extract(s, "Join=")));
		setAuth(authNum(extract(s, "Auth=")));
		setMask(Integer.parseInt(extract(s, "Mask=0x"), 16));
		setRate(Integer.parseInt(extract(s, "Rate=", ", ")));
		setLinkMon(Integer.parseInt(extract(s, "Linkmon=")));
		setPassPhrase(extract(s, "Passphrase="));
		String keynumString = extract(s, "Keynum=");
		if (keynumString!=null && keynumString.length()>0) {
			setKeynum(Integer.parseInt(keynumString));
		}
		setWepKey(extract(s, "Key="));
		setTimeEnable(Integer.parseInt(extract(s, "TimeEna=")));
		setUartMode(Integer.parseInt(extract(s, "Mode=0x"), 16));
	}
	
	public List<String[]> configList() {
		List<String[]> ans = new ArrayList<String[]>();
		ans.add(new String[]{"set adhoc beacon "+getBeacon(), "aok"});
		ans.add(new String[]{"set adhoc probe "+getProbe(), "aok"});
		ans.add(new String[]{"set broadcast address "+getAddress(), "aok"});
		ans.add(new String[]{"set broadcast interval "+getInterval(), "aok"});
		ans.add(new String[]{"set broadcast port "+getPort(), "aok"});
		ans.add(new String[]{"set com $ "+getCmdChar(), "aok"});
		ans.add(new String[]{"set com open "+getOpen(), "aok"});
		ans.add(new String[]{"set com close "+getClose(), "aok"});
		ans.add(new String[]{"set com remote "+getRemote().replace(' ', '$'), "aok"});
		ans.add(new String[]{"set com idle "+getIdle(), "aok"});
		ans.add(new String[]{"set com match "+getMatch(), "aok"});
		ans.add(new String[]{"set com size "+getSize(), "aok"});
		ans.add(new String[]{"set com time "+getTimer(), "aok"});
		
		// dns
		if (0==getDhcp()) {
			ans.add(new String[]{"set dns address "+getDns(), "aok"});
		}
		ans.add(new String[]{"set dns name "+getName(), "aok"});
		ans.add(new String[]{"set dns backup "+getBackup(), "aok"});
		
		// ip
		ans.add(new String[]{"set ip dchp "+getDhcp(), "aok"});
		ans.add(new String[]{"set ip address "+getIpAddr(), "aok"});
		if (0==getDhcp()) {
			// IP address, gateway, netmask
			ans.add(new String[]{"set ip netmask "+getNetMask(), "aok"});
			ans.add(new String[]{"set ip gateway "+getGateway(), "aok"});
		}
		ans.add(new String[]{"set ip localport "+getIpPort(), "aok"});
		ans.add(new String[]{"set ip backup "+getBackup(), "aok"});
		ans.add(new String[]{"set ip flags "+getIpFlags(), "aok"});
		//if (getName()==null || getName().length()==0) {
			ans.add(new String[]{"set ip host "+getHostAddr(), "aok"});
		//}
		ans.add(new String[]{"set ip remote "+getHostPort(), "aok"});
		ans.add(new String[]{"set ip protocol "+getProtocol(), "aok"});
		
		// option
		ans.add(new String[]{"set option jointmr "+getJoinTimer(), "aok"});
		// set option sensor used to be here
		// set option format used to be here
		ans.add(new String[]{"set option replace "+getReplace(), "aok"});
		ans.add(new String[]{"set option deviceid "+getDeviceId(), "aok"});
		if (getPassword()!=null && getPassword().length()>0) {
			ans.add(new String[]{"set option password "+getPassword(), "aok"});
		}
		
		// system
		ans.add(new String[]{"set system autoconn "+getAutoConn(), "aok"});
		// ans.add(new String[]{"set system autosleep "+get(), "aok"});
		ans.add(new String[]{"set system iofunc "+getIoFunc(), "aok"});
		ans.add(new String[]{"set system mask "+getIoMask(), "aok"});
		ans.add(new String[]{"set system printlvl "+getPrintLvl(), "aok"});
		// ans.add(new String[]{"set system output "+get(), "aok"});
		ans.add(new String[]{"set system sleep "+getSleepTimer(), "aok"});
		ans.add(new String[]{"set system trigger "+getTrigger(), "aok"});
		ans.add(new String[]{"set system wake "+getWakeTimer(), "aok"});
		
		// wlan
		ans.add(new String[]{"set wlan auth "+getAuth(), "aok"});
		ans.add(new String[]{"set wlan channel "+getChan(), "aok"});
//		from v 2.30 of http://www.rovingnetworks.com/files/resources/WiFly-RN-UM.pdf
//		NOTE: This command applies only to RN-131. This command is not applied to the RN-171. 
//		Issuing this command on the RN-171 will give an error message: ERR: Bad Args
//		(so we simply disable it)
//		ans.add(new String[]{"set wlan ext_antenna "+getExtAnt(), "aok"});
		ans.add(new String[]{"set wlan join "+getJoin(), "aok"});
		if (isWEP(getAuth())) {
			ans.add(new String[]{"set wlan key "+getWepKey(), ""}); // e.g. 112233445566778899AABBCCDD
			ans.add(new String[]{"set wlan num "+getKeynum(), "aok"});			
		} else if (isWPA(getAuth())) {
			ans.add(new String[]{"set wlan phrase "+getPassPhrase(), ""});			
		}
		// ans.add(new String[]{"set wlan linkmon "+getLinkMon(), ""}); 4/14/2013 seems to cause a problem with whatever command comes next
		ans.add(new String[]{"set wlan mask "+getMask(), "aok"}); 
		// ans.add(new String[]{"set wlan rate "+getRate(), "aok"});  4/14/2013 doesn't seem to work
		// ans.add(new String[]{"set wlan window "+, "aok"});
		ans.add(new String[]{"set wlan ssid "+getSsid().replace(' ', '$'), ""}); // << 4/14/2013 for v 2.38 this is duplicated, see below.

		// These are only available in v 2.19 (and later) but not in v 2.15 of the firmware
		// 'set option sensor' works with 2.19 and 2.21 but not 2.21D
		// changed "set option sensor <mask>" to set q sensor <mask> in ver 2.23
		//ans.add(new String[]{"set option sensor "+hexFormat(getSensors()), "aok"});
		ans.add(new String[]{"set q sensor "+hexFormat(getSensors()), "aok"});
		ans.add(new String[]{"set option format "+getFormat(), "aok"});

		ans.add(new String[]{"set time enable "+getTimeEnable(), "aok"});
		
		ans.add(new String[]{"set uart mode "+getUartMode(), "aok"});
		
		ans.add(new String[]{"set wlan ssid "+getSsid().replace(' ', '$'), ""}); // << 4/14/2013 for v 2.38 this works if repeated at end of command list.

		return ans;
	}
	
	// this is a sub-selection of the above, just to set wi-fi credentials
	public List<String[]> credentialsOnlyConfigList() {
		List<String[]> ans = new ArrayList<String[]>();
		// wlan
		ans.add(new String[]{"set wlan auth "+getAuth(), "aok"});
		if (isWEP(getAuth())) {
			ans.add(new String[]{"set wlan key "+getWepKey(), ""}); // e.g. 112233445566778899AABBCCDD
			ans.add(new String[]{"set wlan num "+getKeynum(), "aok"});			
		} else if (isWPA(getAuth())) {
			ans.add(new String[]{"set wlan phrase "+getPassPhrase(), ""});			
		}
		ans.add(new String[]{"set wlan ssid "+getSsid().replace(' ', '$'), ""}); // << 4/14/2013 for v 2.38 this works if repeated at end of command list.

		return ans;		
	}

	public static String hexFormat(int i) {
		return "0x"+Integer.toHexString(i).toUpperCase();
	}
	
	protected static String removeWS(String s) {
		 if (s==null) return s;
	     StringTokenizer st = new StringTokenizer(s);
	     StringBuffer sb = new StringBuffer();
	     while (st.hasMoreTokens()) {
	         sb.append(st.nextToken());
	     }
	     return sb.toString();
	}
	
	protected static String extract(String s, String prefix) {
		return extract(s, prefix, "\r");
	}

	// returns the substring of 's' that starts after the given
	// prefix and ends with suffix
	// if the prefix is not found, returns null
	protected static String extract(String s, String prefix, String suffix) {
		int i = s.toUpperCase().indexOf(prefix.toUpperCase());
		if (i<0) return null;
		int start = i+prefix.length();
		int end = s.toUpperCase().indexOf(suffix.toUpperCase(), start);
		if (end<0) {
			return s.substring(start);
		} else {
			return s.substring(start, end);
		}
	}

	public int getBeacon() {
		return beacon;
	}

	public void setBeacon(int beacon) {
		this.beacon = beacon;
	}

	public int getProbe() {
		return probe;
	}

	public void setProbe(int probe) {
		this.probe = probe;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public char getCmdChar() {
		return cmdChar;
	}

	public void setCmdChar(char cmdChar) {
		this.cmdChar = cmdChar;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getClose() {
		return close;
	}

	public void setClose(String close) {
		this.close = close;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public int getIdle() {
		return idle;
	}

	public void setIdle(int idle) {
		this.idle = idle;
	}

	public int getMatch() {
		return match;
	}

	public void setMatch(int match) {
		this.match = match;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}
	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBackup() {
		return backup;
	}

	public void setBackup(String backup) {
		this.backup = backup;
	}

	public String getIpIf() {
		return ipIf;
	}

	public void setIpIf(String ipIf) {
		this.ipIf = ipIf;
	}

	public int getDhcp() {
		return dhcp;
	}

	public void setDhcp(int dhcp) {
		this.dhcp = dhcp;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getIpPort() {
		return ipPort;
	}

	public void setIpPort(int ipPort) {
		this.ipPort = ipPort;
	}

	public String getNetMask() {
		return netMask;
	}

	public void setNetMask(String netMask) {
		this.netMask = netMask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getHostAddr() {
		return hostAddr;
	}

	public void setHostAddr(String hostAddr) {
		this.hostAddr = hostAddr;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public int getMtu() {
		return mtu;
	}

	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

	public int getIpFlags() {
		return ipFlags;
	}

	public void setIpFlags(int ipFlags) {
		this.ipFlags = ipFlags;
	}

	public String getBackupAddr() {
		return backupAddr;
	}

	public void setBackupAddr(String backupAddr) {
		this.backupAddr = backupAddr;
	}

	public int getJoinTimer() {
		return joinTimer;
	}

	public void setJoinTimer(int joinTimer) {
		this.joinTimer = joinTimer;
	}

	public int getReplace() {
		return replace;
	}

	public void setReplace(int replace) {
		this.replace = replace;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public int getSensors() {
		return sensors;
	}

	public void setSensors(int sensors) {
		this.sensors = sensors;
	}

	public int getSleepTimer() {
		return sleepTimer;
	}

	public void setSleepTimer(int sleepTimer) {
		this.sleepTimer = sleepTimer;
	}

	public int getWakeTimer() {
		return wakeTimer;
	}

	public void setWakeTimer(int wakeTimer) {
		this.wakeTimer = wakeTimer;
	}

	public int getTrigger() {
		return trigger;
	}

	public void setTrigger(int trigger) {
		this.trigger = trigger;
	}

	public int getAutoConn() {
		return autoConn;
	}

	public void setAutoConn(int autoConn) {
		this.autoConn = autoConn;
	}

	public int getIoFunc() {
		return ioFunc;
	}

	public void setIoFunc(int ioFunc) {
		this.ioFunc = ioFunc;
	}

	public int getIoMask() {
		return ioMask;
	}

	public void setIoMask(int ioMask) {
		this.ioMask = ioMask;
	}

	public int getPrintLvl() {
		return printLvl;
	}

	public void setPrintLvl(int printLvl) {
		this.printLvl = printLvl;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public int getChan() {
		return chan;
	}

	public void setChan(int chan) {
		this.chan = chan;
	}

	public int getExtAnt() {
		return extAnt;
	}

	public void setExtAnt(int extAnt) {
		this.extAnt = extAnt;
	}

	public int getJoin() {
		return join;
	}

	public void setJoin(int join) {
		this.join = join;
	}

	public int getAuth() {
		return auth;
	}
	
	public static boolean isWEP(int auth) {return auth==1;}
	public static boolean isWPA(int auth) {return auth==2 || auth==3 || auth==4;}
	
	public static int authNum(String a) {
		//String a = getAuth();
		if (a==null) throw new NullPointerException();
		if ("OPEN".equalsIgnoreCase(a)) return 0;
		if ("WEP".equalsIgnoreCase(a)) return 1;
		if ("WPA1".equalsIgnoreCase(a)) return 2;
		if ("MIXED".equalsIgnoreCase(a)) return 3;
		if ("WPA2".equalsIgnoreCase(a)) return 4;
		if ("AUTO".equalsIgnoreCase(a)) return 5;
		if ("ADHOC".equalsIgnoreCase(a)) return 6;
		throw new RuntimeException(a);
	}

	public void setAuth(int auth) {
		this.auth = auth;
	}

	public int getMask() {
		return mask;
	}

	public void setMask(int mask) {
		this.mask = mask;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getLinkMon() {
		return linkMon;
	}

	public void setLinkMon(int linkMon) {
		this.linkMon = linkMon;
	}

	public String getPassPhrase() {
		return passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public int getKeynum() {
		return keynum;
	}

	public void setKeynum(int keynum) {
		this.keynum = keynum;
	}

	public String getWepKey() {
		return wepKey;
	}

	public void setWepKey(String wepKey) {
		String s = removeWS(wepKey);
		this.wepKey = s==null ? null : s.toLowerCase();
	}

	public int getTimeEnable() {
		return timeEnable;
	}

	public void setTimeEnable(int timeEnable) {
		this.timeEnable = timeEnable;
	}

	public int getUartMode() {
		return uartMode;
	}

	public void setUartMode(int uartMode) {
		this.uartMode = uartMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + auth;
		result = prime * result + autoConn;
		result = prime * result + ((backup == null) ? 0 : backup.hashCode());
		result = prime * result
				+ ((backupAddr == null) ? 0 : backupAddr.hashCode());
		result = prime * result + beacon;
		result = prime * result + chan;
		result = prime * result + ((close == null) ? 0 : close.hashCode());
		result = prime * result + cmdChar;
		result = prime * result
				+ ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + dhcp;
		result = prime * result + ((dns == null) ? 0 : dns.hashCode());
		result = prime * result + extAnt;
		result = prime * result + format;
		result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
		result = prime * result
				+ ((hostAddr == null) ? 0 : hostAddr.hashCode());
		result = prime * result + hostPort;
		result = prime * result + idle;
		result = prime * result + interval;
		result = prime * result + ioFunc;
		result = prime * result + ioMask;
		result = prime * result + ((ipAddr == null) ? 0 : ipAddr.hashCode());
		result = prime * result + ipFlags;
		result = prime * result + ((ipIf == null) ? 0 : ipIf.hashCode());
		result = prime * result + ipPort;
		result = prime * result + join;
		result = prime * result + joinTimer;
		result = prime * result + keynum;
		result = prime * result + linkMon;
		result = prime * result + mask;
		result = prime * result + match;
		result = prime * result + mtu;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((netMask == null) ? 0 : netMask.hashCode());
		result = prime * result + ((open == null) ? 0 : open.hashCode());
		result = prime * result
				+ ((passPhrase == null) ? 0 : passPhrase.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + printLvl;
		result = prime * result + probe;
		result = prime * result + protocol;
		result = prime * result + rate;
		result = prime * result + ((remote == null) ? 0 : remote.hashCode());
		result = prime * result + replace;
		result = prime * result + sensors;
		result = prime * result + size;
		result = prime * result + sleepTimer;
		result = prime * result + ((ssid == null) ? 0 : ssid.hashCode());
		result = prime * result + timeEnable;
		result = prime * result + timer;
		result = prime * result + trigger;
		result = prime * result + wakeTimer;
		result = prime * result + ((wepKey == null) ? 0 : wepKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameters other = (Parameters) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (auth != other.auth)
			return false;
		if (autoConn != other.autoConn)
			return false;
		if (backup == null) {
			if (other.backup != null)
				return false;
		} else if (!backup.equals(other.backup))
			return false;
		if (backupAddr == null) {
			if (other.backupAddr != null)
				return false;
		} else if (!backupAddr.equals(other.backupAddr))
			return false;
		if (beacon != other.beacon)
			return false;
		if (chan != other.chan)
			return false;
		if (close == null) {
			if (other.close != null)
				return false;
		} else if (!close.equals(other.close))
			return false;
		if (cmdChar != other.cmdChar)
			return false;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (dhcp != other.dhcp)
			return false;
		if (dns == null) {
			if (other.dns != null)
				return false;
		} else if (!dns.equals(other.dns))
			return false;
		if (extAnt != other.extAnt)
			return false;
		if (format != other.format)
			return false;
		if (gateway == null) {
			if (other.gateway != null)
				return false;
		} else if (!gateway.equals(other.gateway))
			return false;
		if (hostAddr == null) {
			if (other.hostAddr != null)
				return false;
		} else if (!hostAddr.equals(other.hostAddr))
			return false;
		if (hostPort != other.hostPort)
			return false;
		if (idle != other.idle)
			return false;
		if (interval != other.interval)
			return false;
		if (ioFunc != other.ioFunc)
			return false;
		if (ioMask != other.ioMask)
			return false;
		if (ipAddr == null) {
			if (other.ipAddr != null)
				return false;
		} else if (!ipAddr.equals(other.ipAddr))
			return false;
		if (ipFlags != other.ipFlags)
			return false;
		if (ipIf == null) {
			if (other.ipIf != null)
				return false;
		} else if (!ipIf.equals(other.ipIf))
			return false;
		if (ipPort != other.ipPort)
			return false;
		if (join != other.join)
			return false;
		if (joinTimer != other.joinTimer)
			return false;
		if (keynum != other.keynum)
			return false;
		if (linkMon != other.linkMon)
			return false;
		if (mask != other.mask)
			return false;
		if (match != other.match)
			return false;
		if (mtu != other.mtu)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (netMask == null) {
			if (other.netMask != null)
				return false;
		} else if (!netMask.equals(other.netMask))
			return false;
		if (open == null) {
			if (other.open != null)
				return false;
		} else if (!open.equals(other.open))
			return false;
		if (passPhrase == null) {
			if (other.passPhrase != null)
				return false;
		} else if (!passPhrase.equals(other.passPhrase))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (port != other.port)
			return false;
		if (printLvl != other.printLvl)
			return false;
		if (probe != other.probe)
			return false;
		if (protocol != other.protocol)
			return false;
		if (rate != other.rate)
			return false;
		if (remote == null) {
			if (other.remote != null)
				return false;
		} else if (!remote.equals(other.remote))
			return false;
		if (replace != other.replace)
			return false;
		if (sensors != other.sensors)
			return false;
		if (size != other.size)
			return false;
		if (sleepTimer != other.sleepTimer)
			return false;
		if (ssid == null) {
			if (other.ssid != null)
				return false;
		} else if (!ssid.equals(other.ssid))
			return false;
		if (timeEnable != other.timeEnable)
			return false;
		if (timer != other.timer)
			return false;
		if (trigger != other.trigger)
			return false;
		if (wakeTimer != other.wakeTimer)
			return false;
		if (wepKey == null) {
			if (other.wepKey != null)
				return false;
		} else if (!wepKey.equals(other.wepKey))
			return false;
		return true;
	}

	public String diff(Parameters that, List<String> skip) {
		Method[] methods = Parameters.class.getMethods();
		String ans = "";
		for (Method m : methods) {
			String name = m.getName();
			if (!name.startsWith("get")) continue;
			// so we know we have a 'getter' and can assume it has no arguments
			String field = name.substring(3);
			if (skip.contains(field)) continue;
			try {
				Object o1 = m.invoke(this);
				Object o2 = m.invoke(that);
				if (o1==null && o2==null) continue;
				if (o1==null) ans += field+": null    "+o2+"\n";
				if (o2==null) ans += field+": "+o1+"    null\n";
				if (o1.equals(o2)) continue;
				ans += field+": "+o1+"    "+o2+"\n";
			} catch (Exception e) {
				throw new RuntimeException("method="+m.getName(), e);
			}
		}
		if (ans.length()==0) return null;
		return ans;
	}
	
	public String diffCredentialsOnly(Parameters that) {
		String ans = "";
		if (this.getAuth()!=that.getAuth()) ans += "auth: "+this.getAuth()+"    "+that.getAuth()+"\n";
		if (isWEP(getAuth())) {
			if (!this.getWepKey().equals(that.getWepKey())) ans += "wepKey: "+this.getWepKey()+"    "+that.getWepKey()+"\n";
			if (this.getKeynum()!=that.getKeynum()) ans += "keynum: "+this.getKeynum()+"    "+that.getKeynum()+"\n";
		} else if (isWPA(getAuth())) {
			if (!this.getPassPhrase().equals(that.getPassPhrase())) ans += "pass phrase: "+this.getPassPhrase()+"    "+that.getPassPhrase()+"\n";
		}
		if (!this.getSsid().equals(that.getSsid())) ans += "ssid: "+this.getSsid()+"    "+that.getSsid()+"\n";
		
		if (ans.length()==0) return null;
		return ans;
	}
}
