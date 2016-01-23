package yqt.mc.rdc;

import org.bukkit.Bukkit;

public class NMSExceptionHandling {

	//consistently handles NMS throwing errors
	public static void handler(Exception e) {
		e.printStackTrace();
		Bukkit.getServer().getLogger().severe("Serious NMS error.");
		ViewDistManager.onDisable();
	}
}
