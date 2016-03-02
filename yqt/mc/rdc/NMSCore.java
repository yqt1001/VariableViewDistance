package yqt.mc.rdc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

import yqt.mc.rdc.NMSTest.ErrorType;

public class NMSCore {

	private static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	private VVD plugin;
	private boolean isTest;
	
	public NMSCore(VVD plugin, boolean test) {
		this.plugin = plugin;
		this.isTest = test;
	}
	
	public void setRenderDistance(int vdist) {
		
		//get CraftBukkit CraftWorld class
		Class <?> craftworldclass = null;
		try {
			craftworldclass = Class.forName("org.bukkit.craftbukkit." + version + "CraftWorld");
		} catch (ClassNotFoundException e) {
			handler(e);
			return;
		}
		
		//get the NMS PlayerChunkManager object for this world
		Object cw = craftworldclass.cast(Bukkit.getWorld("world"));
		Object ws = getPrivateField("world", craftworldclass, cw);
		Object pcm = getPrivateField("manager", ws.getClass(), ws);
		
		//invoke the public a() method in the PlayerChunkManager class that changes the render distance
		try {
			Method viewDistChanger = pcm.getClass().getMethod("a", int.class);
			viewDistChanger.invoke(pcm, vdist);
		} catch (Exception e) {
			handler(e);
		}
	}
	
	public double[] getServerTPS() {
		//get CraftServer class
		Class <?> craftserverclass = null;
		
		try {
			craftserverclass = Class.forName("org.bukkit.craftbukkit." + version + "CraftServer");
		} catch (ClassNotFoundException e) {
			handler(e);
			return null;
		}
		
		//get the NMS server class for this world which stores TPS data
		Object cs = craftserverclass.cast(Bukkit.getServer());
		Object mcs = getPrivateField("console", craftserverclass, cs);
		try {
			return (double[]) mcs.getClass().getField("recentTps").get(mcs);
		} catch (Exception e) {
			this.handler(e);
			return null;
		}
	}
	
	
	private Object getPrivateField(String name, Class< ?> clazz, Object o) {
		
		Field f = null;
		Object obj = null;
		
		try {
			f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			obj = f.get(o);
		} catch (Exception e) {
			this.handler(e);
		}
		
		return obj;
	}
	
	//consistently handles NMS errors
	public void handler(Exception e) {
		e.printStackTrace();
		Bukkit.getServer().getLogger().severe("Serious NMS error.");
		plugin.setVVDEnabled(false);
		plugin.setErrorReason(ErrorType.NMS);
		
		if(!isTest)
			plugin.getVVDManager().onDisable();
	}
}
