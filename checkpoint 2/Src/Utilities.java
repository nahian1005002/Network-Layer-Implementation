import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.net.*;
import java.io.*;

/**
 * the IpAddress class handles ip address including functionalities such as
 * whether two ip addresses are in the same network
 */
class IpAddress {
	/**
	 * contains the ip address in byte format
	 */
	byte[] ipAddr = new byte[4];

	/**
	 * receives an ip address in string format and stores it in instance
	 * variable ipAddr
	 * 
	 * @param ipString
	 *            ip address in String format
	 */
	public IpAddress(String ipString) {
		StringTokenizer strTok = new StringTokenizer(ipString, ".", false);
		int i = 0;
		String octet;
		while (strTok.hasMoreTokens()) {
			octet = strTok.nextToken();
			ipAddr[i++] = (byte) Integer.valueOf(octet, 10).intValue();
		}
	}

	/**
	 * receives an ip address in array of bytes format and stores in instance
	 * variable ipAddr
	 * 
	 * @param ip
	 *            ip address in the array of bytes formats
	 */
	public IpAddress(byte[] ip) {
		System.arraycopy(ip, 0, ipAddr, 0, 4);
	}

	/**
	 * returns ip address in array of bytes format
	 * 
	 * @return ip address in array of bytes format
	 */
	public byte[] getBytes() {
		return ipAddr;
	}

	/**
	 * return ip address in String format
	 * 
	 * @return ip address in String format
	 */
	public String getString() {
		return new String((int) (ipAddr[0] & 0xFF) + "."
				+ (int) (ipAddr[1] & 0xFF) + "." + (int) (ipAddr[2] & 0xFF)
				+ "." + (int) (ipAddr[3] & 0xFF));
	}

	/**
	 * calculates the network address of the ip address from supplied subnet
	 * mask
	 * 
	 * @param mask
	 *            subnet mask
	 * @return IpAddress class having ip address set to the network address
	 */
	public IpAddress getNetworkAddress(int mask) {
		byte[] netMask = new byte[4];
		// find mask
		int j = 0;
		int tMask = 128;
		for (int i = 0; i < 32; i++) {
			if (i < mask) {
				tMask = (tMask >> 1) | 128;
			}
			if ((i + 1) % 8 == 0) {
				netMask[j++] = (byte) tMask;
				tMask = 0;
			}
		}
		// find network address
		byte[] netAddr = new byte[4];
		for (int i = 0; i < 4; i++) {
			netAddr[i] = (byte) (ipAddr[i] & netMask[i]);
		}
		return new IpAddress(netAddr);
	}

	/**
	 * checks whether the supplied instance of IpAddress has ip address which is
	 * in the the same network as that of the invoking instance
	 * 
	 * @param ip
	 *            instance of IpAddress
	 * @param mask
	 *            subnet mask
	 * @return true if the supplied instance of IpAddress has ip address which
	 *         is in the the same network as that of the invoking instance and
	 *         false otherwise
	 */
	public boolean sameSubnet(IpAddress ip, int mask) {
		IpAddress network1 = getNetworkAddress(mask);
		IpAddress network2 = ip.getNetworkAddress(mask);
		return network1.sameIp(network2);
	}

	/**
	 * checks whether the supplied instance of IpAddress has the same ip address
	 * as that of the invoking instance
	 * 
	 * @param ip
	 *            instance of IpAddress class
	 * @return true if the supplied instance of IpAddress has the same ip
	 *         address as that of the invoking instance and false otherwise
	 */
	public boolean sameIp(IpAddress ip) {
		byte[] other = ip.getBytes();
		for (int i = 0; i < 4; i++) {
			if (ipAddr[i] != other[i]) {
				return false;
			}
		}
		return true;
	}
}

/**
 * the Packet class defines the structure of packets in network layer
 */
class Packet {
	/**
	 * ip address of the source encapsulated in an instance if IpAddress
	 */
	IpAddress src;

	/**
	 * ip address of the destination encapsulated in an instance if IpAddress
	 */
	IpAddress dst;

	/**
	 * actual data of the packet in array of bytes format
	 */
	byte payload[];

	/**
	 * initializes instance variables corresponding to source ip, destination
	 * ip, and payload
	 * 
	 * @param an
	 *            array of bytes containing source ip in first four slots,
	 *            destination ip in second four slots, and the payload in rest
	 */
	Packet(byte[] a) {
		byte[] srcIp = new byte[4];
		byte[] dstIp = new byte[4];
		payload = new byte[a.length - 8];
		System.arraycopy(a, 0, srcIp, 0, 4);
		System.arraycopy(a, 4, dstIp, 0, 4);
		System.arraycopy(a, 8, payload, 0, a.length - 8);
		src = new IpAddress(srcIp);
		dst = new IpAddress(dstIp);
	}

	/**
	 * initializes instance variables corresponding to source ip, destination
	 * ip, and payload
	 * 
	 * @param s
	 *            source ip address encapsulated in an instance of IpAddress
	 * @param d
	 *            destination ip address encapsulated in an instance of
	 *            IpAddress
	 * @param a
	 *            array of bytes containing the payload
	 */
	Packet(IpAddress s, IpAddress d, byte[] a) {
		payload = new byte[a.length];
		System.arraycopy(a, 0, payload, 0, a.length);
		src = s;
		dst = d;
	}

	/**
	 * returns the ip address of the source encapsulated in an instance of
	 * IpAddress
	 * 
	 * @return ip address of the source encapsulated in an instance of IpAddress
	 */
	IpAddress getSrcIp() {
		return src;
	}

	/**
	 * returns the ip address of the destination encapsulated in an instance of
	 * IpAddress
	 * 
	 * @return ip address of the destination encapsulated in an instance of
	 *         IpAddress
	 */
	IpAddress getDstIp() {
		return dst;
	}

	/**
	 * returns the payload as array of bytes
	 * 
	 * @return payload as array of bytes
	 */
	byte[] getPayload() {
		return payload;
	}

	/**
	 * returns an array of bytes with embedded source ip address in first four
	 * slots, destination ip address in second four slots, and the rest
	 * containing payload
	 * 
	 * @return array of bytes having source ip in first four slots, destination
	 *         ip in second four slots and payload in the rest
	 */
	byte[] getBytes() {
		byte[] packet = new byte[payload.length + 8];
		try {
			System.arraycopy(src.getBytes(), 0, packet, 0, 4);
			System.arraycopy(dst.getBytes(), 0, packet, 4, 4);
			System.arraycopy(payload, 0, packet, 8, payload.length);
			return packet;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * returns a string informing source ip address, destination ip address and
	 * payload
	 * 
	 * @return string informing source ip address, destination ip address and
	 *         payload
	 */
	String getString() {
		return new String("SrcIP=" + src.getString() + " | DestIP="
				+ dst.getString() + " | Payload=" + new String(payload));
	}
}

/**
 * the Frame class defines the structure of frames in data link layer
 */
class Frame {
	/**
	 * byte representing the source mac address
	 */
	byte srcMac;

	/**
	 * byte representing the destination mac address
	 */
	byte dstMac;

	/**
	 * array of bytes representing the payload
	 */
	byte payload[];

	/**
	 * checksum byte for error checking
	 */
	byte checksum;

	/**
	 * initializes instance variables corresponding to source mac address,
	 * destination mac address and payload
	 * 
	 * @param a
	 *            array of bytes containing source and destination mac address
	 *            in first two slots respectively and payload in the rest
	 */
	Frame(byte[] a) {
		srcMac = a[0];
		dstMac = a[1];
		payload = new byte[a.length - 3];
		try {
			System.arraycopy(a, 2, payload, 0, payload.length);
			checksum = a[a.length - 1];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * initializes instance variables corresponding to source mac address,
	 * destination mac address and payload
	 * 
	 * @param s
	 *            source mac address in integer format
	 * @param d
	 *            destination mac address in integer format
	 * @param a
	 *            payload in array of bytes
	 */
	Frame(int s, int d, byte[] a) {
		srcMac = (byte) s;
		dstMac = (byte) d;
		payload = new byte[a.length];
		try {
			System.arraycopy(a, 0, payload, 0, a.length);
			checksum = calculateChecksum();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * initializes instance variables corresponding to source mac address,
	 * destination mac address, payload initialized to zero
	 * 
	 * @param s
	 *            source mac address in integer format
	 * @param d
	 *            destination mac address in integer format
	 */
	Frame(int s, int d) {
		srcMac = (byte) s;
		dstMac = (byte) d;
		payload = new byte[0];
		checksum = calculateChecksum();
	}

	/**
	 * returns source mac address in byte
	 * 
	 * @return source mac address in byte
	 */
	byte getSrcMac() {
		return srcMac;
	}

	/**
	 * returns destination mac address in byte
	 * 
	 * @return destination mac address in byte
	 */
	byte getDstMac() {
		return dstMac;
	}

	/**
	 * returns value in the checksum field
	 * 
	 * @return value in the checksum field
	 */
	byte getChecksum() {
		return checksum;
	}

	/**
	 * returns payload in array of bytes
	 * 
	 * @return payload in array of bytes
	 */
	byte[] getPayload() {
		return payload;
	}

	/**
	 * returns an array of bytes with embedded mac address in first slot,
	 * destination mac address in second slot, and payload in the rest
	 * 
	 * @return array of bytes having source mac address in first slot,
	 *         destination mac address in second slot, and payload in the rest
	 */
	byte[] getBytes() {
		byte[] frame = new byte[payload.length + 3];
		frame[0] = srcMac;
		frame[1] = dstMac;
		try {
			System.arraycopy(payload, 0, frame, 2, payload.length);
			frame[payload.length + 2] = checksum;
			return frame;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calculate one byte checksum for the frame
	 * 
	 * @return checksum value
	 */
	//author War Ador
	byte calculateChecksum() {
		/*
		 * Replace the following code with checksum generation algorithm to
		 * calculate checksum on src MAC, dst MAC and payload
		 */
		byte ckSm = (byte) (srcMac ^ dstMac);
		if (payload.length == 0) {
			return (byte)ckSm;
		} else {
			for (int i = 0; i < payload.length; i++) {
				ckSm ^= payload[i];
			}
			
			return ckSm;
		}
	}

	/**
	 * checks whether the frame has any checksum error
	 * 
	 * @return true if checksum error is detected and false otherwise
	 */
	//author War Ador
	boolean hasCheckSumError() {
		/* replace the following code with checksum verification */
		byte currentCalculatedChecksum = calculateChecksum();
		if (currentCalculatedChecksum == checksum) {
			System.out.println("\tCurrent Checksum: " + currentCalculatedChecksum+" Received Checksum: "+getChecksum());
			System.out.println("\t\t No Checksum eroor.");
			return false;
		} else {
			System.out.println("\tCurrent Checksum: " + currentCalculatedChecksum+" Received Checksum: "+getChecksum());
			System.out.println("\t\t Have Checksum eroor.");
			return true;
		}
	}

	/**
	 * returns a string informing source mac address, destination mac address
	 * and payload
	 * 
	 * @return string informing source mac address, destination mac address and
	 *         payload
	 */
	String getString() {
		return new String("Src Mac=" + (int) (srcMac & 0xFF) + " | Dst Mac="
				+ (int) (dstMac & 0xFF));
	}
}

/**
 * class ByteArray is a custom method to manipulate array of bytes
 */
class ByteArray {
	/**
	 * array of bytes to be manipulated
	 */
	byte[] bArray;

	/**
	 * instantiates instance variable corresponding to array of bytes
	 * 
	 * @param size
	 *            size of the array
	 */
	ByteArray(int size) {
		bArray = new byte[size];
	}

	/**
	 * initializes instance variable with the supplied array of bytes
	 * 
	 * @param b
	 *            array of bytes
	 */
	ByteArray(byte[] b) {
		bArray = new byte[b.length];
		System.arraycopy(b, 0, bArray, 0, b.length);
	}

	/**
	 * stores value of supplied array of bytes b in instance variable bArray,
	 * starting from an specified index of bArray
	 * 
	 * @param index
	 *            index of instance variable bArray from where the copy should
	 *            start
	 * @param b
	 *            array of bytes to be stored in instance variable bArray
	 */
	void setAt(int index, byte[] b) {
		System.arraycopy(b, 0, bArray, index, b.length);
	}

	/**
	 * returns byte value at specified index of instance variable bArray
	 * 
	 * @param index
	 *            index of instance variable bArray from which byte value should
	 *            be retrieved
	 * @return byte value at specified index of instance variable bArray
	 */
	byte getByteVal(int index) {
		return bArray[index];
	}

	/**
	 * stores byte value at specified index of instance variable bArray
	 * 
	 * @param index
	 *            index of instance variable bArray to which byte value should
	 *            be stored
	 */
	void setByteVal(int index, byte b) {
		bArray[index] = b;
	}

	/**
	 * returns a portion of instance variable bArray according to supplied index
	 * from which copy should start, along with number of bytes to be copied
	 * 
	 * @param index
	 *            index of bArray from which copy should start
	 * @param length
	 *            number of bytes to be copied
	 * @return array of bytes containing portion of instance variable bArray as
	 *         specified through parameters
	 */
	byte[] getAt(int index, int length) {
		byte[] temp = new byte[length];
		System.arraycopy(bArray, index, temp, 0, length);
		return temp;
	}

	/**
	 * returns array of bytes containing value of instance variable bArray
	 * 
	 * @return array of bytes containing value of instance variable bArray
	 */
	byte[] getBytes() {
		return bArray;
	}

	/**
	 * returns size of instance variable bArray
	 * 
	 * @return size of instance variable bArray
	 */
	int getSize() {
		return bArray.length;
	}
}

/**
 * class Buffer<T> acts as a generic storage class
 */
class Buffer<T> {
	/**
	 * array of supplied type
	 */
	T data[];

	/**
	 * size of the buffer
	 */
	int size;

	/**
	 * index of the start of the buffer
	 */
	int head;

	/**
	 * index of the end of the buffer
	 */
	int tail;

	/**
	 * name of the buffer
	 */
	String name;

	/**
	 * initializes name and size of buffer
	 * 
	 * @param n
	 *            String denoting name of the buffer
	 * @param sz
	 *            integer denoting size of the buffer
	 */
	Buffer(String n, int sz) {
		name = n;
		size = sz + 1;
		data = (T[]) new Object[size];
		head = 0;
		tail = 0;
	}

	/**
	 * checks whether buffer is empty
	 * 
	 * @return true if buffer is empty and false otherwise
	 */
	synchronized boolean empty() {
		if (head == tail)
			return true;
		else
			return false;
	}

	/**
	 * checks whether buffer is full
	 * 
	 * @return true if buffer is full and false otherwise
	 */
	synchronized boolean full() {
		if ((tail + 1) % size == head)
			return true;
		else
			return false;
	}

	/**
	 * stores a single item in buffer if space is available and dropped
	 * otherwise
	 * 
	 * @param t
	 *            element of type T that should be stored
	 * @return true of storage was successful and false otherwise
	 */
	synchronized boolean store(T t) {
		if (full()) {
			System.out.println(name + " Buffer Full. Dropping Packet ...");
			return false;
		} else {
			tail = (tail + 1) % size;
			data[tail] = t;
			return true;
		}
	}

	/**
	 * returns a single item from head of the buffer if buffer is not empty and
	 * null otherwise
	 * 
	 * @return a single item from head of the buffer if buffer is not empty and
	 *         null otherwise
	 */
	synchronized T get() {
		if (empty()) {
			System.out.println(name + " Buffer Empty.");
			return null;
		} else {
			head = (head + 1) % size;
			return data[head];
		}
	}
}

class RoutingTableEntry
{
	NextHop nextHop;
	int distance;
	ReschedulableTimer timer;
	RoutingTableEntry(NextHop nextHop,int dist,ReschedulableTimer timer)
	{
		this.nextHop=nextHop;
		this.distance=dist;
		this.timer=timer;
	}
}

class RIPUpdate
{
	HashMap<NetworkAddress,Integer> routeInfo=new HashMap<NetworkAddress,Integer>(); 
	RIPUpdate(HashMap<NetworkAddress,RoutingTableEntry> rt)
	{
		synchronized (rt) {
			for (Map.Entry<NetworkAddress,RoutingTableEntry> entry : rt.entrySet()) {
			    NetworkAddress addr = entry.getKey();
			    RoutingTableEntry re = entry.getValue();
			    routeInfo.put(addr,re.distance);	
			}
		}
	}
	
	RIPUpdate(byte b[])
	{
		if(b.length<4)
		{
			System.out.println("Invalid RIPupdate format");
			return;
		}
		ByteArray bArray=new ByteArray(b);
		int size=byteArrayToInt(bArray.getAt(0, 4));
		if(b.length<size*12+4)
		{
			System.out.println("invalid RIPupdate format");
			return;
		}
		NetworkAddress networkAddress;
		for(int i=0;i<size;i++)
		{
			networkAddress=new NetworkAddress(new IpAddress(bArray.getAt(i*12+4,4)),byteArrayToInt(bArray.getAt(i*12+8, 4)));
			routeInfo.put(networkAddress,byteArrayToInt(bArray.getAt(i*12+12,4)));
		}
	}
	
	byte [] getBytes(){
		//byte ret[]=new byte[routeInfo.size()*8+4];
		ByteArray ret=new ByteArray(routeInfo.size()*12+4);
		ret.setAt(0,intToByteArray(routeInfo.size()));
		int index=4;
		for (Map.Entry<NetworkAddress,Integer> entry : routeInfo.entrySet()) {
		    NetworkAddress addr = entry.getKey();
		    int dist = entry.getValue();
		    ret.setAt(index, addr.ip.getBytes());
		    ret.setAt(index+4, intToByteArray(addr.mask));
		    ret.setAt(index+8, intToByteArray(dist));
		    index+=12;
		}
		return ret.getBytes();
	}
	
	boolean process (RoutingProtocol rp,NextHop nextHop)
	{
		//NetworkAddress a=new NetworkAddress(new IpAddress("127.0.0.1"),24);
		//NetworkAddress b=new NetworkAddress(new IpAddress("127.0.0.1"),24);
		//System.out.println(a.equals(b));
		//System.out.println(a.hashCode()==b.hashCode());
		synchronized (rp) {
			boolean flag=false;
			for (Map.Entry<NetworkAddress,Integer> entry : routeInfo.entrySet()) {
			    NetworkAddress addr = entry.getKey();
			    int dist = entry.getValue();
			    //System.out.println(rt.containsKey(addr));
			    if(rp.simrouter.routingTable.containsKey(addr)==false)
			    {
			    	ReschedulableTimer timer=new ReschedulableTimer();
			    	InvalidTimerTask task=new InvalidTimerTask(rp,addr);
			    	timer.schedule(task, RoutingProtocol.INVALID_TIMER_VALUE*1000);
			    	rp.simrouter.routingTable.put(addr, new RoutingTableEntry(nextHop,dist+1,timer));
			    	flag=true;
			    }
			    else
			    {
			    	//System.out.println("got in\n");
			    	if(rp.simrouter.routingTable.get(addr).distance>dist+1)
			    	{
			    		ReschedulableTimer timer=rp.simrouter.routingTable.get(addr).timer;
			    		rp.simrouter.routingTable.put(addr, new RoutingTableEntry(nextHop,dist+1,timer));
			    		flag=true;
			    	}
			    }
			}
			return flag;
		}
		
	}
	
	public static byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	public static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
}

class InvalidTimerTask implements Runnable {
	RoutingProtocol rp;
	NetworkAddress addr;
	InvalidTimerTask(RoutingProtocol rp,NetworkAddress addr)
	{
		this.rp=rp;
		this.addr=addr;
	}
    public void run() {
    	synchronized(rp.simrouter.routingTable){
    		rp.handleTimerEvent(2,addr);
    	}
    }
}


class ReschedulableTimer extends Timer {
    private Runnable task;
    private TimerTask timerTask;

    public void schedule(Runnable runnable, long delay) {
      task = runnable;
      timerTask = new TimerTask() { 
          public void run() { 
              task.run(); 
              }
          };

          schedule(timerTask, delay);        
    }

    public void reschedule(long delay) {
        System.out.println("rescheduling after seconds "+delay);
      timerTask.cancel();
      timerTask = new TimerTask() { 
          public void run() { 
              task.run(); 
          }
      };
      	schedule(timerTask, delay);        
    }
  }

