import java.util.*;
import java.net.*;
import java.io.*;

/**
 * class SimPhy simulates the physical layer
 */
class SimPhy {
	/**
	 * socket
	 */
	Socket sock;

	/**
	 * input stream
	 */
	DataInputStream br;

	/**
	 * output stream
	 */
	OutputStream bw;

	/**
	 * represents whether connection present
	 */
	boolean hasConnection = false;

	/**
	 * buffer to send bits from physical layer to data link layer
	 */
	Buffer<ByteArray> phy2dll;

	/**
	 * buffer to receive frames from data link layer to physical layer
	 */
	Buffer<ByteArray> dll2phy;

	/**
	 * represents whether the port is up
	 */
	boolean isUp = true;

	/**
	 * @param deviceId
	 *            device id
	 * @param p2d
	 *            buffer for data from physical layer to data link layer
	 * @param d2p
	 *            buffer for data from data link layer to physical layer
	 */
	SimPhy(String deviceId, Buffer<ByteArray> p2d, Buffer<ByteArray> d2p) {
		try {
			sock = new Socket("127.0.0.1", 9009);
			hasConnection = true;

			br = new DataInputStream(sock.getInputStream());

			bw = sock.getOutputStream();

			SimPhy.writeStuffed(bw, deviceId.getBytes());

			phy2dll = p2d;
			dll2phy = d2p;
			new PhySend(this);
			new PhyReceive(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * updates port status
	 * 
	 * @param s
	 *            boolean representing status of port, true if up and false
	 *            otherwise
	 */
	public void setPortStatus(boolean s) {
		isUp = s;
		// System.out.println("Port Status updated for "+portId+" to "+s);
	}

	/**
	 * returns port status
	 * 
	 * @return true if port is up and false otherwise
	 */
	public boolean getPortStatus() {
		return isUp;
	}

	/**
	 * adds post amble to the stuffed bits
	 */
	public static void writeStuffed(OutputStream bw, byte[] f) throws Exception {
		try {
			byte[] temp = SimPhy.bitStuff(f);
			ByteArray b = new ByteArray(temp.length + 2);
			b.setByteVal(0, (byte) 126);// here there is only post amble.
										// Preamble may be added
			b.setAt(1, temp);
			b.setByteVal(temp.length + 1, (byte) 126);// here there is only post
														// amble. Preamble may
														// be added
			byte[] cont = b.getBytes();
			//System.out.print("Bytes in writeStuffed: ");
//			for (byte x : cont) {
//				System.out.print(x + " ");
//			}
//			System.out.println();
			bw.write(b.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 
	 */
	public static byte[] readDeStuffed(DataInputStream br) throws Exception {
		byte[] b = new byte[1000]; // this size is arbitrary.
		int count = 0;
		try {
			byte i = br.readByte();

			while (i != 126) {
			}// skip as long as there is no preamble

			i = br.readByte();
			while (i != 126) {
				b[count++] = i;
				// add code incase count>size of b.
				if(count>b.length)
				{
					byte[] tmp = b;
					b = new byte[(count-1)*2];
					System.arraycopy(tmp, 0, b, 0, count-1);
				}
				i = br.readByte();
				//System.out.print(i + " ");
			}
			byte[] temp = new byte[count];
			System.arraycopy(b, 0, temp, 0, count);

			/* demonstrate the effect of bit stuff */
			return SimPhy.bitDeStuff(temp);
			// return temp;
		} catch (Exception e) {
			e.printStackTrace();
			throw e; // simphy may be invoked from a while(1) loop and everytime
						// there will be an exception. Therefore to avoid it,
						// send some sort of feedback.
		}
	}

	/**
	 * handles bit stuffing
	 */
	public static byte[] bitStuff(byte[] b) {
		// To Do: Write code for bit stuffing here
//		 System.out.println("\n\nReceived bytes In bit stuff");
//		 for (int i = 0 ; i < b.length ; i++)
//		 {
//		 System.out.print(b[i] +" ");
//		 }
		byte currentByte = 0;
		ArrayList<Boolean> bits = new ArrayList<Boolean>();
		for (int i = 0; i < b.length; i++) {
			currentByte = b[i];
			for (int j = 128; j > 0; j = j / 2) {
				if ((currentByte & j) == j) {
					bits.add(true);
				} else {
					bits.add(false);
				}
			}
		}
		// for(int i= 0 ; i < bits.size() ; i++)
		// {
		// if(i%8 == 0)
		// System.out.println(i/8);
		// System.out.print(bits.get(i)+" ");
		//
		// }
		for (int i = 0; i < (bits.size() - 4); i++) {
			if (bits.get(i) && bits.get(i + 1) && bits.get(i + 2)
					&& bits.get(i + 3) && bits.get(i + 4)) {
				//System.out.println("Adding some bits");
				bits.add(i + 5, false);
			}
		}
		// System.out.println("\nAfter stauffing");
		// for(int i= 0 ; i < bits.size() ; i++)
		// {
		// if(i%8 == 0)
		// System.out.println();
		// System.out.print(bits.get(i)+" ");
		//
		// }
		int totalBytesInArray = (int) Math.ceil(bits.size() / 8.0);
		byte[] stuffedBytes = new byte[totalBytesInArray];
		int numberOfFalseBits = totalBytesInArray * 8 - bits.size();
		for (int i = 0; i < numberOfFalseBits; i++) {
			bits.add(false);
		}
		int index = 0;
		String bs = "";
		for (int i = 0; i < bits.size(); i += 8) {
			if (bits.get(i))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 1))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 2))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 3))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 4))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 5))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 6))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 7))
				bs += "1";
			else
				bs += "0";
			// System.out.println("\n\t\t"+i/8+ " BS = " + bs);
			stuffedBytes[index++] = (byte) Integer.parseInt(bs, 2);
			bs = "";
		}
//		 System.out.println("\n\nOutput bytes In bit stuff");
//		 for (int i = 0 ; i < stuffedBytes.length ; i++)
//		 {
//		 System.out.print(stuffedBytes[i] +" ");
//		 }
		return stuffedBytes;// default behaviour
	}

	/**
	 * handles bit de-stuffing
	 */
	public static byte[] bitDeStuff(byte[] b) {
		// To Do: Write code for bit de-stuffing here
//		System.out.println("\n\tReceived bytes In bit destuff");
//		for (int i = 0; i < b.length; i++) {
//			System.out.print(b[i] + " ");
//		}
		byte currentByte = 0;
		ArrayList<Boolean> bits = new ArrayList<Boolean>();
		for (int i = 0; i < b.length; i++) {
			currentByte = b[i];
			for (int j = 128; j > 0; j = j / 2) {
				if ((currentByte & j) == j) {
					bits.add(true);
				} else {
					bits.add(false);
				}
			}
		}
		// for(int i= 0 ; i < bits.size() ; i++)
		// {
		// if(i%8 == 0)
		// System.out.println(i/8);
		// System.out.print(bits.get(i)+" ");
		//
		// }
		int countRemovedBit = 0;
		for (int i = 0; i < (bits.size() - 4); i++) {
			if (bits.get(i) && bits.get(i + 1) && bits.get(i + 2)
					&& bits.get(i + 3) && bits.get(i + 4)) {
				bits.remove(i + 5);
				i=i+4;
				//System.out.println("\n\t\t removing some bits");
				countRemovedBit++;
			}
		}
		int numberOfFalseBits = 0;
		if (countRemovedBit != 0) {
			if (countRemovedBit <= 8) {
				numberOfFalseBits = 8 - countRemovedBit;
			} else {
				numberOfFalseBits = 8 - countRemovedBit % 8;
			}
		}

		// System.out.println("\n\nNumber of false bits " + numberOfFalseBits);
		for (int i = 0; i < numberOfFalseBits; i++) {
			bits.remove(bits.size() - 1);
		}
		int numberOfBytes = bits.size() / 8;
		byte[] deStuffedBytes = new byte[numberOfBytes];
		int index = 0;
		String bs = "";
		for (int i = 0; i < bits.size(); i += 8) {
			if (bits.get(i))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 1))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 2))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 3))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 4))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 5))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 6))
				bs += "1";
			else
				bs += "0";
			if (bits.get(i + 7))
				bs += "1";
			else
				bs += "0";
			//System.out.println("\n\t\t" + i / 8 + " BS = " + bs);
			deStuffedBytes[index++] = (byte) Integer.parseInt(bs, 2);
			bs = "";
		}
//		System.out.println("\n\tOutput from bit de stuff");
//		for (int i = 0; i < deStuffedBytes.length; i++) {
//			System.out.print(deStuffedBytes[i] + " ");
//		}
		return deStuffedBytes;// default behaviour
	}

	/**
	 * checks whether port is connected
	 * 
	 * @return true if port is connected and false otherwise
	 */
	public boolean connected() {
		return hasConnection;
	}

	/**
	 * receives a frame from data link layer
	 */
	public byte[] receiveFromDll() {
		try {
			synchronized (dll2phy) {
				if (dll2phy.empty())
					dll2phy.wait();
				byte[] f = dll2phy.get().getBytes();
				dll2phy.notify();
				//System.out.println("PL: PL < DLL");
				return f;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * sends frame to physical layer
	 * 
	 * @param f
	 *            frame to be sent
	 */
	public void sendToLine(byte[] f) throws Exception {
		try {
			SimPhy.writeStuffed(bw, f);
			//System.out.println("PL: data is send to line.");
		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
		}
	}

	/**
	 * sends frame to data link layer
	 * 
	 * @param f
	 *            frame to be sent
	 */
	public void sendToDll(byte[] f) {
		try {
			synchronized (phy2dll) {
				if (phy2dll.full()) {
					System.out.println("Waiting..");
					phy2dll.wait();
				}
				//System.out.println("Sending..");
				ByteArray b = new ByteArray(f);
				phy2dll.store(b);
				phy2dll.notify();
			}
			//System.out.println("PL: PL > DLL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * receives bits from physical layer, converts the bits to frame and returns
	 * the converted frame
	 * 
	 * @return frame formed from bits from physical layer
	 */
	public byte[] receiveFromLine() throws Exception {
		try {
			byte[] f = readDeStuffed(br);
			//System.out.println("PL: data is received from line.");
			return f;
		} catch (Exception e) {
			// e.printStackTrace(); //not required. already printed by simphy
			throw e;
		}
	}

	/**
	 * class PhySend receives frame from data link layer and sends it to
	 * physical layer
	 */
	private class PhySend extends Thread {
		SimPhy simphy;

		public PhySend(SimPhy s) {
			simphy = s;
			start();
		}

		public void run() {
			try {
				while (true) {
					byte[] f = receiveFromDll();
					sendToLine(f);
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * class PhyReceive receives frame from physical layer and sends it to data
	 * link layer
	 */
	private class PhyReceive extends Thread {
		SimPhy simphy;

		public PhyReceive(SimPhy s) {
			simphy = s;
			start();
		}

		public void run() {
			try {
				while (true) {
					byte[] f = receiveFromLine();
					if (simphy.getPortStatus()) {
						if (f != null)
							sendToDll(f);
					}
				}
			} catch (Exception e) {
			}
		}
	}
	// =======================================================
}
