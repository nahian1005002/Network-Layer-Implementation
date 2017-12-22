import java.util.*;
import java.util.Map.Entry;
import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;


/**
 * class RoutingProtocol simulates the routing protocol
 */
class RoutingProtocol extends Thread{	
	/**
	 * router simulated by SimRouter
	 */
	SimRouter simrouter;
	
	/**
	 * update timer duration
	 */
	public static int UPDATE_TIMER_VALUE=30;
	
	/**
	 * invalidate timer duration
	 */
	public static int INVALID_TIMER_VALUE=90;
	
	//To Do: Declare any other variables required
	
	Buffer<ByteArray> buffer;
	ReschedulableTimer updateTimer;
	//----------------------------------------------------------------------------------------------	
	
	/**
	 * @param s simulated router
	 */
	public RoutingProtocol(SimRouter s){
		simrouter=s;		
		//To Do: Do other required initialization tasks.
		buffer=new Buffer<ByteArray>("Buffer",10);
		start();
	}
	
	//------------------------Routing Function-----------------------------------------------	
	/**
	 * stores the update data in a shared memory to be processed by the 'RoutingProtocol' thread later
	 *
	 * @param p ByteArray
	 */
	void notifyRouteUpdate(ByteArray p){//invoked by SimRouter
		//Write code to just to stores the route update data; do not process at this moment, otherwise the simrouter thread will get blocked
		try{
			synchronized(buffer){
				if(buffer.full())
					System.out.println("Routing protocol buffer full: RIPUpdate packet dropped");
				else
				{
					buffer.store(p);
					buffer.notify();
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	//----------------------------------------------------------------------------------------------	
	/**
	 * update the routing table according to the changed status of an interface; if interface is UP (status=TRUE), add to routing table, if interface is down, remove from routing table
	 *
	 * @param interfaceId interface id of the router
	 * @param status status denoting whether interface is on or off, true if on and false otehrwise 
	 */
	public void notifyPortStatusChange(int interfaceId, boolean status){//invoked by SimRouter
		//To Do: Update the routing table according to the changed status of an interface. If interface in UP (status=TRUE), add to routing table, if interface is down, remove from routing table
		synchronized (simrouter.routingTable) {
			System.out.println("entered"); 
			IpAddress ip=simrouter.interfaces[interfaceId].getIpAddress();
			int mask=simrouter.interfaces[interfaceId].getSubnetMask();
			IpAddress nip=ip.getNetworkAddress(mask);
			NetworkAddress na= new NetworkAddress(nip,mask);
			System.out.println("check1"); 
			if(status==true)
			{
				System.out.println("checkif"); 
				simrouter.routingTable.put(na,new RoutingTableEntry(new NextHop(null,interfaceId),0,new ReschedulableTimer()) );
				System.out.println("inserting into routing table : network ip:"+nip.getString()+"subnet mask:"+mask+"next hop ip: --- interface :"+interfaceId);
			}
			else
			{
				simrouter.routingTable.remove(na);
				System.out.println("check2"); 
				List<NetworkAddress> list=new ArrayList<NetworkAddress>();
				for (Entry<NetworkAddress, RoutingTableEntry> entry : simrouter.routingTable.entrySet()) {
				    NetworkAddress addr = entry.getKey();
				    RoutingTableEntry re= entry.getValue();
				    System.out.println("check2.5"); 
				    if(re.nextHop.interfaceId==interfaceId)
				    {
				    	list.add(addr);
				    	
				    	System.out.println("check2.8"); 
				    }
				    System.out.println("check2.9"); 
				}
				for(NetworkAddress addr : list){
					simrouter.routingTable.remove(addr);
					}
				System.out.println("check3"); 
				printRoutingTable();
			}	
		}
	}
	
	//---------------------Forwarding Function------------------------------------------
	/**
	 * returns an NextHop object corresponding the destination IP Address, dstIP. If route in unknown, return null
	 *
	 * @param destination ip
	 * @return returns an NextHop object corresponding the destination IP Address if route is known, else returns null
	 */
	NextHop getNextHop(IpAddress dstIp){//invoked by SimRouter
		//To Do: Write code that  returns an NextHop object corresponding the destination IP Address: dstIP. If route in unknown, return null
		synchronized (simrouter.routingTable) {
			for (Entry<NetworkAddress, RoutingTableEntry> entry : simrouter.routingTable.entrySet()) {
			    NetworkAddress addr = entry.getKey();
			    RoutingTableEntry re= entry.getValue();
			    NextHop hop =re.nextHop;
			    if(addr.ip.sameIp(dstIp.getNetworkAddress(addr.mask)))
			    {
			    	IpAddress nextHopIp=dstIp;
			    	if(hop.ip!=null)
			    		nextHopIp=hop.ip;
			    	return new NextHop(nextHopIp,hop.getInterfaceId());
			    }
			    	
			}
			return null; //default return value
		}
	}	
	
	//-------------------Routing Protocol Thread--------------------------------------	
	public void run(){
		//To Do 1: Populate Routing Table with directly connected interfaces using the SimRouter instance. Also print this initial routing table	.	
		//synchronization is not necessary in this loop becasue only one thread exist now
		for(int i=1;i<=simrouter.interfaceCount;i++)
		{
			if(simrouter.interfaces[i].getIsConfigured()&&simrouter.interfaces[i].getPortStatus())
			{
				IpAddress ip=simrouter.interfaces[i].getIpAddress();
				int mask=simrouter.interfaces[i].getSubnetMask();
				IpAddress nip=ip.getNetworkAddress(mask);
				NetworkAddress na= new NetworkAddress(nip,mask);
				simrouter.routingTable.put(na,new RoutingTableEntry(new NextHop(null,i),0,new ReschedulableTimer()) );
			//	System.out.println("network ip:"+nip.getString()+"subnet mask:"+mask+"next hop ip: --- interface :"+i);
			}
		}
		printRoutingTable();
		
		//To Do 2: Send constructed routing table immediately to all the neighbors. Start the update timer.		
		sendRoutingTableToInterfaces();
		updateTimer=new ReschedulableTimer();
		updateTimer.schedule(new Runnable() { 
              public void run() { 
                   handleTimerEvent(1,null);
                  }
              },UPDATE_TIMER_VALUE*1000);
		//To Do 3: Continuously check whether there are routing updates received from other neighbours.
		//An update has been received, Now:
			//To Do 3.1: Modify routing table according to the update received. 
			//To Do 3.2: Start invalidate timer for each newly added/updated route if any.
			//To Do 3.3:Print the routing table if the routing table has changed
		
		while(true)
		{
			ByteArray bArray;
			try{
				synchronized(buffer)
				{
					if(buffer.empty())
						buffer.wait();
					bArray=buffer.get();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
			Packet packet=new Packet(bArray.getBytes());
			RIPUpdate update=new RIPUpdate(packet.getPayload());
			NextHop nextHop=getNextHop(packet.src);
			if(update.process(this, nextHop))
				printRoutingTable();
			
			System.out.printf("Network Address/Mask \tDistance\n");
			for (Map.Entry<NetworkAddress,Integer> entry : update.routeInfo.entrySet()) {
			    NetworkAddress addr = entry.getKey();
			    int re= entry.getValue();
			    System.out.printf("%s/%d \t\t%d\n",addr.ip.getString(),addr.mask,re);
			}
			
		}
		//To Do Optional 1: Send Triggered update to all the neighbors and reset update timer.
	}	
	
	void sendRoutingTableToInterfaces()
	{
		synchronized (simrouter.routingTable) {
			RIPUpdate ripUpdate=new RIPUpdate(simrouter.routingTable);
			for(int i=1;i<=simrouter.interfaceCount;i++)
			{
				if(!simrouter.interfaces[i].isConfigured || !simrouter.interfaces[i].isUp)
					continue;
				IpAddress IpOfInterface=simrouter.interfaces[i].getIpAddress();
				
				Packet p= new Packet(IpOfInterface,new IpAddress(simrouter.RP_MULTICAST_ADDRESS),ripUpdate.getBytes());
				ByteArray bArray=new ByteArray(p.getBytes().length+1);
				bArray.setByteVal(0,DataLinkLayer.BROADCAST_MAC );
				bArray.setAt(1,p.getBytes() );
				simrouter.sendPacketToInterface(bArray, i);
			}
		}
	}
	
	void printRoutingTable()
	{
		synchronized (simrouter.routingTable) {
			System.out.printf("Network Address/Mask\t NextHop\t Interface\t Distance\n");
			for (Map.Entry<NetworkAddress,RoutingTableEntry> entry : simrouter.routingTable.entrySet()) {
			    NetworkAddress addr = entry.getKey();
			    RoutingTableEntry re= entry.getValue();
			   // System.out.println(simrouter.routingTable.containsKey(addr));
			    NextHop hop =re.nextHop;
			    String nextHop="null";
			    if(hop.ip!=null)
			    	nextHop=hop.ip.getString();
			    System.out.printf("%s/%d \t\t%s \t\t%d\t\t %d\n",addr.ip.getString(),addr.mask,nextHop,hop.interfaceId,re.distance);
			}
		}
	}
	
	//----------------------Timer Handler------------------------------------------------------	
	/**
	 * handles what happens when update timer and invalidate timer expires
	 * 
	 * @param type of timer: type 1- update timer and type 2- invalid timer expired
	 */
	public void handleTimerEvent(int type,NetworkAddress addr){
		//If update timer has expired, then:
			//To Do 1: Sent routing update to all the interfaces. Use simrouter.sendPacketToInterface(update, interfaceId) function to  send the updates.		
			//To Do Optional 1: Implement split horizon rule while sending update
			//To Do 2: Start the update timer again.
		//Else an invalid timer has expired, then:
		//To Do 3:  Delete route from routing table for which invalidate timer has expired.			
			if(type==1)
			{
				sendRoutingTableToInterfaces();
				updateTimer.reschedule(UPDATE_TIMER_VALUE*1000);
			}
			else
			{
				System.out.printf("invalid timer expired for %s/%d",addr.ip.getString(),addr.mask);
				simrouter.routingTable.remove(addr);
				printRoutingTable();
			}
			
		}			
	//----------------------------------------------------------------------------------------------
}

