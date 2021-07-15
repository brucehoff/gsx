package gsxconfig;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GSXConfigTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private static final int MAX = 100;
	
	/*
	 * This shows the difference between the clock time reported by the GSX module
	 * and that reported by the local system.
	 */
	@Test
	public void testRTC() throws Exception {
		GSXConfig config = new GSXConfig();
		DateFormat df = getDateFormat();
		try {
			config.connect("192.168.2.10", null);
			long lastDiff = 0L;
			long lastTime = System.currentTimeMillis();
			while (true) {
				String s = config.drainInput();
				if (s==null || s.length()==0) continue;
				int rtci = s.indexOf("rtc=");
				if (rtci<0) continue;
				String rtc = s.substring(rtci+4, rtci+4+8);
				Date date = ntpToDate(rtc);
				long now = System.currentTimeMillis();
				long diff = date.getTime()-now;
				System.out.println(diff/1000L);
				if (Math.abs(diff-lastDiff)>2000L) {
					System.out.println("*** difference changed by: "+((diff-lastDiff)/1000L)+" seconds.");
					System.out.println("*** last time this happened was "+((now-lastTime)/1000L)+" seconds ago.");
					lastTime = now;
				}
				lastDiff = diff;
				Thread.sleep(10000L);
			}
		} finally {
			config.disconnect();
		}
	}
	
	// this is duplicated out of the GSXUtil class in the OMWatcher project
	
	// from
	// http://www.devdaily.com/java/jwarehouse/commons-net-2.2/src/main/java/org/apache/commons/net/ntp/TimeStamp.java.shtml
    /**
     * baseline NTP time if bit-0=0 -> 7-Feb-2036 @ 06:28:16 UTC
     */
    private static final long msb0baseTime = 2085978496000L;

    /**
     *  baseline NTP time if bit-0=1 -> 1-Jan-1900 @ 01:00:00 UTC
     */
    private static final long msb1baseTime = -2208988800000L; 

	public static Date ntpToDate(String ntpHexString) {
		return ntpToDate(Long.parseLong(ntpHexString, 16));
	}
	
	public static Date ntpToDate(long ntpSeconds) {

        /*
         * If the most significant bit (MSB) on the seconds field is set we use
         * a different time base. The following text is a quote from RFC-2030 (SNTP v4):
         *
         *  If bit 0 is set, the UTC time is in the range 1968-2036 and UTC time
         *  is reckoned from 0h 0m 0s UTC on 1 January 1900. If bit 0 is not set,
         *  the time is in the range 2036-2104 and UTC time is reckoned from
         *  6h 28m 16s UTC on 7 February 2036.
         */
        long msb = ntpSeconds & 0x80000000L;
        long dateBase = 0L;
        if (msb == 0) {
            // use base: 7-Feb-2036 @ 06:28:16 UTC
        	dateBase = msb0baseTime + (ntpSeconds * 1000);
        } else {
            // use base: 1-Jan-1900 @ 01:00:00 UTC
        	dateBase = msb1baseTime + (ntpSeconds * 1000);
        }
		return new Date(dateBase);
	}

	private static final String TIME_STAMP_FORMAT = "EEE ddMMMyyyy HH:mm:ss.SSS";
	
	private static DateFormat getDateFormat() {
		DateFormat df = new SimpleDateFormat(TIME_STAMP_FORMAT);
		df.setTimeZone(TimeZone.getDefault());
		return df;
	}
	

}
