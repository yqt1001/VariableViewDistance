package yqt.mc.rdc;

import org.bukkit.Bukkit;

public class NMSTest {

	//test to ensure the NMS code runs fine on startup
	public static void onEnable() {
		
		//start off by getting tps data
		NMSCore.getServerTPS();
		
		//set render distance to 6
		NMSCore.setRenderDistance(6);
		//back to config set value
		NMSCore.setRenderDistance(Main.MAX_DIST);
		
		//check if test failed or succeeded
		Bukkit.getServer().getLogger().info("VVD startup test " + (Main.enabled ? "successful." : "failed!"));
	}
}
