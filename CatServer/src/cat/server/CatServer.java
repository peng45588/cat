package cat.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import cat.function.CatBean;
import cat.function.ClientBean;
import cat.util.CatUtil;

public class CatServer {
	private static ServerSocket ss;
	public static HashMap<String, ClientBean> onlines;
	static {
		try {
			ss = new ServerSocket(8532);
			onlines = new HashMap<String, ClientBean>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class CatClientThread extends Thread {
		private Socket client;
		private CatBean bean;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public CatClientThread(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				// 不停的从客户端接收信息
				while (true) {
					// 读取从客户端接收到的catbean信息
					ois = new ObjectInputStream(client.getInputStream());
					bean = (CatBean) ois.readObject();

					// 分析catbean中，type是那样一种类型
					switch (bean.getType()) {
					// 上下线更新
					case 0: { // 上线
						// 记录上线客户的用户名和端口在clientbean中
						ClientBean cbean = new ClientBean();
						cbean.setName(bean.getName());
						cbean.setSocket(client);
						// 添加在线用户
						onlines.put(bean.getName(), cbean);
						// 创建服务器的catbean，并发送给客户端
						CatBean serverBean = new CatBean();
						serverBean.setType(0);
						serverBean.setInfo(bean.getTimer() + "  "
								+ bean.getName() + "上线了");
						// 通知所有客户更新上线列表
						HashSet<String> set = new HashSet<String>();
						// 客户昵称
						set.addAll(onlines.keySet());
						serverBean.setClients(set);
						sendAll(serverBean);
						
						//发给好友与自己 上线信息，端口为100
						serverBean.setType(100);
						HashSet<String> set100 = getFriendSet(bean,set);
						set100.add(bean.getName());
						serverBean.setClients(set100);
						sendMessage(serverBean);
						break;
					}
					case -1: { // 下线
						// 创建服务器的catbean，并发送给客户端
						CatBean serverBean = new CatBean();
						serverBean.setType(-1);

						try {
							oos = new ObjectOutputStream(
									client.getOutputStream());
							oos.writeObject(serverBean);
							oos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						onlines.remove(bean.getName());

						// 向剩下的在线用户发送有人离开的通知
						CatBean serverBean2 = new CatBean();
						serverBean2.setInfo(bean.getTimer() + "  "
								+ bean.getName() + " " + " 下线了");
						serverBean2.setType(0);
						HashSet<String> set = new HashSet<String>();
						set.addAll(onlines.keySet());
						serverBean2.setClients(set);

						sendAll(serverBean2);
						return;
					}
					case 1: { // 聊天
						//获取该用户好友列表,判断是否为好友，若不是则发回
						HashSet<String> set = getFriendSet(bean,bean.getClients());
						CatBean serverBean = new CatBean();
						if (set.isEmpty()) {
							set.add(bean.getName());
						}
						serverBean.setClients(set);
						////////////////////
						
						serverBean.setType(1);
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());
						// 向选中的客户发送数据
						sendMessage(serverBean);
						break;
					}
					case 110:{
						CatBean serverBean = new CatBean();

						serverBean.setType(110);
						serverBean.setClients(bean.getClients()); // 文件来源
						serverBean.setName(bean.getName());// 接收的客户名称
						serverBean.setTimer(bean.getTimer());
						serverBean.setInfo(bean.getInfo());
						sendMessage(serverBean);

						break;
					}
					case 2: { // 请求接受文件
						// 创建服务器的catbean，并发送给客户端
						CatBean serverBean = new CatBean();
						String info = bean.getTimer() + "  " + bean.getName()
								+ "向你传送文件,是否需要接受";

						serverBean.setType(2);
						serverBean.setClients(bean.getClients()); // 这是发送的目的地
						serverBean.setFileName(bean.getFileName()); // 文件名称
						serverBean.setSize(bean.getSize()); // 文件大小
						serverBean.setInfo(info);
						serverBean.setName(bean.getName()); // 来源
						serverBean.setTimer(bean.getTimer());
						// 向选中的客户发送数据
						sendMessage(serverBean);

						break;
					}
					case 3: { // 确定接收文件
						CatBean serverBean = new CatBean();

						serverBean.setType(3);
						serverBean.setClients(bean.getClients()); // 文件来源
						serverBean.setTo(bean.getTo()); // 文件目的地
						serverBean.setFileName(bean.getFileName()); // 文件名称
						serverBean.setIp(bean.getIp());
						serverBean.setPort(bean.getPort());
						serverBean.setName(bean.getName()); // 接收的客户名称
						serverBean.setTimer(bean.getTimer());
						// 通知文件来源的客户，对方确定接收文件
						sendMessage(serverBean);
						break;
					}
					case 4: {//中途取消接收文件
						CatBean serverBean = new CatBean();

						serverBean.setType(4);
						serverBean.setClients(bean.getClients()); // 文件来源
						serverBean.setTo(bean.getTo()); // 文件目的地
						serverBean.setFileName(bean.getFileName());
						serverBean.setInfo(bean.getInfo());
						serverBean.setName(bean.getName());// 接收的客户名称
						serverBean.setTimer(bean.getTimer());
						sendMessage(serverBean);

						break;
					}
					case 5: {//登陆
						// 创建服务器的catbean，并发送给客户端
						CatBean serverBean = new CatBean();
						serverBean.setType(5);
						serverBean.setInfo("登陆成功");
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());

						// TODO 需要判断是否登陆成功
						Properties userPro = new Properties();
						File file = new File("Users.properties");
						CatUtil.loadPro(userPro, file);// userPro 取得的所有用户名密码数据
						if (userPro.containsKey(bean.getName())) {// 用户名存在
							if (userPro.getProperty(bean.getName()).equals(
									bean.getPassword())) {
								serverBean.setInfo("登陆成功");
							} else {
								serverBean.setInfo("您输入的密码有误！");
							}
						} else {
							serverBean.setInfo("该用户不存在");
						}
						HashSet<String> set = new HashSet<String>();
						// 设置返回给自己
						set.add(bean.getName());
						serverBean.setClients(set);
						sendMessageToOne(serverBean);
						break;
					}
					case 6: {// 注册事件
						// 创建服务器的catbean，并发送给客户端
						CatBean serverBean = new CatBean();
						serverBean.setType(6);
						serverBean.setName(bean.getName());
						serverBean.setTimer(bean.getTimer());

						// TODO 需要判断是否注册成功
						Properties userPro = new Properties();
						File file = new File("Users.properties");
						CatUtil.loadPro(userPro, file);// userPro 取得的所有用户名密码数据
						if (userPro.containsKey(bean.getName())) {// 用户名已存在
							serverBean.setInfo("用户名已存在");
						} else {
							setPassword(userPro, file, bean.getName(),
									bean.getPassword());
							serverBean.setInfo("注册成功");
						}

						HashSet<String> set = new HashSet<String>();
						// 客户昵称
						set.add(bean.getName());
						serverBean.setClients(set);
						sendMessageToOne(serverBean);
						break;
					}
					case 7 :{
						// 添加好友
						Properties friendPro = new Properties();
						File file = new File("Friend.properties");
						CatUtil.loadPro(friendPro, file);// friendPro 取得的所有用户名密码数据
						if (bean.getInfo().equals("addFriend")) {//发起好友请求
							//判断是否为好友，若不是则将名字加到ret中
							HashSet<String> ret = new HashSet<String>();
							Iterator<String> it = bean.getClients().iterator();
							while (it.hasNext()) {
								String name = it.next();
								if (!friendPro.toString().contains("friend"+bean.getName()+"="+name)) {
									ret.add(name);
								}
							}
							
							CatBean serverBean = new CatBean();
							serverBean.setType(7);
							
							serverBean.setClients(ret);
							serverBean.setInfo(bean.getInfo());
							serverBean.setName(bean.getName());
							serverBean.setTimer(bean.getTimer());
							// 向选中的客户发送数据
							sendMessage(serverBean);
						}else if (bean.getInfo().equals("addFriendBack")) {//同意好友请求
							//TODO 加入Friend列表中
							int i = 1;
							while (true) {
								if (friendPro.toString().contains(i+"friend"+bean.getName())) {
									i++;
								}else {
									break;
								}
							}
							Iterator<String> it = bean.getClients().iterator();
							while (it.hasNext()) {
								setPassword(friendPro, file, i+"friend"+bean.getName(),
										it.next());
								i++;
							}
							it = bean.getClients().iterator();
							while (it.hasNext()) {
								String name = it.next();
								i = 1;
								while (true) {
									if (friendPro.toString().contains(i+"friend"+name)) {
										i++;
										}else {
										break;
									}
								}
								setPassword(friendPro, file, i+"friend"+name,
										bean.getName());
							}
							
							//TODO bean.getClients()只能为一个   待改
							CatBean serverBean = new CatBean();
							serverBean.setType(7);
							serverBean.setClients(bean.getClients());
							serverBean.setInfo(bean.getInfo());
							serverBean.setName(bean.getName());
							serverBean.setTimer(bean.getTimer());
							// 向选中的客户发送数据
							sendMessage(serverBean);
						}
						
						break;
					}
					default: {
						break;
					}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close();
			}
		}

		private HashSet<String> getFriendSet(CatBean bean,HashSet<String> clients) {
			// TODO Auto-generated method stub
			Properties userPro = new Properties();
			File file = new File("Friend.properties");
			CatUtil.loadPro(userPro, file);// userPro 取得的所有用户名密码数据
			
			int i = 1;
			HashSet<String> set = new HashSet<String>();
			if (clients==null) {
				return new HashSet<String>();
			}
			Object [] arr = clients.toArray();
			while(true){
				if (userPro.containsKey(i+"friend"+bean.getName())) {// 用户名存在
					for (int j = 0; j < arr.length; j++) {
						if (userPro.get(i+"friend"+bean.getName()).toString().equals(arr[j].toString())) {
							set.add(userPro.get(i+"friend"+bean.getName()).toString());
							break;
						}
					}
					i++;
				} else {
					break;
				}
			}
			return set;
		}

		// 向选中的用户发送数据
		private void sendMessage(CatBean serverBean) {
			// 首先取得所有的values
			Set<String> cbs = onlines.keySet();
			Iterator<String> it = cbs.iterator();
			// 选中客户
			HashSet<String> clients = serverBean.getClients();
			while (it.hasNext()) {
				// 在线客户
				String client = it.next();
				// 选中的客户中若是在线的，就发送serverbean
				if (clients.contains(client)) {
					Socket c = onlines.get(client).getSocket();
					ObjectOutputStream oos;
					try {
						oos = new ObjectOutputStream(c.getOutputStream());
						oos.writeObject(serverBean);
						oos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		// 向选中的用户发送数据
		private void sendMessageToOne(CatBean serverBean) {
			// 首先取得所有的values
			Socket c = client;
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(c.getOutputStream());
				oos.writeObject(serverBean);
				oos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// 向所有在线的用户发送数据
		public void sendAll(CatBean serverBean) {
			Collection<ClientBean> clients = onlines.values();
			Iterator<ClientBean> it = clients.iterator();
			ObjectOutputStream oos;
			while (it.hasNext()) {
				Socket c = it.next().getSocket();
				try {
					oos = new ObjectOutputStream(c.getOutputStream());
					oos.writeObject(serverBean);
					oos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void close() {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void setPassword(Properties userPro, File file, String u_name,
			String u_pwd) {
		userPro.setProperty(u_name, u_pwd);
		try {
			userPro.store(new FileOutputStream(file),
					"Copyright (c) Boxcode Studio");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void setFriend(Properties userPro, File file, String u_name,
			String u_pwd) {
		userPro.setProperty(u_name, u_pwd);
		try {
			userPro.store(new FileOutputStream(file),
					"Copyright (c) Boxcode Studio");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void start() {
		try {
			while (true) {
				Socket client = ss.accept();
				new CatClientThread(client).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new CatServer().start();
	}

}
