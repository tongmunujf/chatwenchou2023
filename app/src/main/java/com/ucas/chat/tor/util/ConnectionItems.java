package com.ucas.chat.tor.util;

import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionItems {

	private CopyOnWriteArrayList<ConnectionStatusItem> connection= new CopyOnWriteArrayList<ConnectionStatusItem>();

	public void addNewConn(ConnectionStatusItem item) {
		if(item!=null)
			this.connection.add(item);
	}
	public ConnectionStatusItem getConnBySocket(Socket socket) {
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
			if(item.getSocket().equals(socket))
				return item;
		}
		return null;
	}

	public ConnectionStatusItem getConnByPurpose(int purpose) {
//		System.out.println("getConnByPurpose: "+purpose);
		int index=-1;
		int count =0;
		ConnectionStatusItem result=null;
//		System.out.println("ConnectionItems getConnByPurpose total Connection "+this.connection.size());
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
//			if(item.getPurpose()==purpose && item.getShareKey()!=null) {
			if(item.getPurpose()==purpose && item.getShareKey()!=null) {
//				System.out.println("ConnectionItems getConnByPurpose find connection of purpose "+ item.toString()+" "+purpose);
				return item;
			}else if(item.getShareKey()!=null){
				index= i;
				count = count +1;
//				System.out.println("ConnectionItems getConnByPurpose find connection of purpose not the same "+ item.getPurpose());
			}else if (item.getPurpose()==purpose){
//				System.out.println("ConnectionItems getConnByPurpose find connection sharedkey is null"+item.toString());
				result= item;
			}else{

			}
		}
		if(index>-1) {
			ConnectionStatusItem item=this.connection.get(index);
//			item.setPurpose(purpose);
//			System.out.println("ConnectionItems getConnByPurpose reset connection  purpose "+ item.toString()+" "+purpose);
			return item;
		}
//		System.out.println("ConnectionItems getConnByPurpose purpose "+ purpose +" not found");
		return result;
	}


	public int  removeConnBySocket(Socket socket) {
//		System.out.println("ConnectionItems removeConnBySocket 11111 "+ socket );
		int status=-1;
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
//			System.out.println("ConnectionItems removeConnBySocket  22222 "+ item.getSocket() );
			if(item.getSocket().equals(socket)) {
				this.connection.remove(i);
				status=item.getPurpose();
//				System.out.println("ConnectionItems removeConnBySocket  333333 "+ socket +" success");
				break;
			}
		}
//		System.out.println("ConnectionItems removeConnBySocket 44444 "+ socket +"\t"+status);
		return status;
	}

	public int updateStatus(Socket socket,int status,byte[] sharedKey) {
		int flag=0;
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
			if(item.getSocket().equals(socket)) {
				item.setStatus(status);
				if(sharedKey!=null)
					item.setShareKey(sharedKey);
				flag=1;
				break;
			}
		}
		return flag;
	}

	public int updateConnectionPurpose(Socket socket,int purpose) {
		int flag=0;
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
			if(item.getSocket().equals(socket)) {
				item.setPurpose(purpose);
				flag=1;
				break;
			}
		}
		return flag;
	}

	public int getConnectionNumber() {
		return this.connection.size();
	}
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<this.connection.size();i++) {
			ConnectionStatusItem item =  this.connection.get(i);
			sb.append(item.toString());
			sb.append("\n");
		}

		return  sb.toString();
	}

}
