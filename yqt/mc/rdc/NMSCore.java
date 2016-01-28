package yqt.mc.rdc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public class NMSCore {

	private static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	
	public static void setRenderDistance(int vdist) {
		
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
	
	public static double[] getServerTPS() {
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
			handler(e);
			return null;
		}
	}
	
	
	public static Object getPrivateField(String name, Class< ?> clazz, Object o) {
		
		Field f = null;
		Object obj = null;
		
		try {
			f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			obj = f.get(o);
		} catch (Exception e) {
			handler(e);
		}
		
		return obj;
	}
	
	//consistently handles NMS errors
	public static void handler(Exception e) {
		e.printStackTrace();
		Bukkit.getServer().getLogger().severe("Serious NMS error.");
		ViewDistManager.onDisable();
		Main.enabled = false;
	}
}
