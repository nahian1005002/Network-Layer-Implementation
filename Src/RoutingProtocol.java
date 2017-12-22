import java.util.*;
import java.net.*;
import java.io.*;


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
	//----------------------------------------------------------------------------------------------	
	
	/**
	 * @param s simulated router
	 */
	public RoutingProtocol(SimRouter s){
		simrouter=s;		
		//To Do: Do other required initialization tasks.
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
		IpAddress ip=simrouter.interfaces[interfaceId].getIpAddress();
		int mask=simrouter.interfaces[interfaceId].getSubnetMask();
		IpAddress nip=ip.getNetworkAddress(mask);
		NetworkAddress na= new NetworkAddress(nip,mask);
		if(status==true)
		{
			simrouter.routingTable.put(na,new NextHop(null,interfaceId) );
			System.out.println("inserting into routing table : network ip:"+nip.getString()+"subnet mask:"+mask+"next hop ip: --- interface :"+interfaceId);
		}
		else
		{
			simrouter.routingTable.remove(na);
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
		for (Map.Entry<NetworkAddress,NextHop> entry : simrouter.routingTable.entrySet()) {
		    NetworkAddress addr = entry.getKey();
		    NextHop hop = entry.getValue();
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
	
	//-------------------Routing Protocol Thread--------------------------------------	
	public void run(){
		//To Do 1: Populate Routing Table with directly connected interfaces using the SimRouter instance. Also print this initial routing table	.	
		for(int i=1;i<=simrouter.interfaceCount;i++)
		{
			if(simrouter.interfaces[i].getIsConfigured()&&simrouter.interfaces[i].getPortStatus())
			{
				IpAddress ip=simrouter.interfaces[i].getIpAddress();
				int mask=simrouter.interfaces[i].getSubnetMask();
				IpAddress nip=ip.getNetworkAddress(mask);
				NetworkAddress na= new NetworkAddress(nip,mask);
				simrouter.routingTable.put(na,new NextHop(null,i) );
				System.out.println("network ip:"+nip.getString()+"subnet mask:"+mask+"next hop ip: --- interface :"+i);
			}
		}
		
		//To Do 2: Send constructed routing table immediately to all the neighbors. Start the update timer.		
		
		//To Do 3: Continuously check whether there are routing updates received from other neighbours.
		//An update has been received, Now:
			//To Do 3.1: Modify routing table according to the update received. 
			//To Do 3.2: Start invalidate timer for each newly added/updated route if any.
			//To Do 3.3:Print the routing table if the routing table has changed
			//To Do Optional 1: Send Triggered update to all the neighbors and reset update timer.
	}	
	
	//----------------------Timer Handler------------------------------------------------------	
	/**
	 * handles what happens when update timer and invalidate timer expires
	 * 
	 * @param type of timer: type 1- update timer and type 2- invalid timer expired
	 */
	public void handleTimerEvent(int type){
		//If update timer has expired, then:
			//To Do 1: Sent routing update to all the interfaces. Use simrouter.sendPacketToInterface(update, interfaceId) function to  send the updates.		
			//To Do Optional 1: Implement split horizon rule while sending update
			//To Do 2: Start the update timer again.
			
		//Else an invalid timer has expired, then:
			//To Do 3:  Delete route from routing table for which invalidate timer has expired.			
	}			
	//----------------------------------------------------------------------------------------------
}

