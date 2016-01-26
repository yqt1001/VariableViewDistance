package yqt.mc.rdc;

import org.bukkit.Bukkit;

abstract class NMSExceptionHandling {

	//consistently handles NMS throwing errors
	public static void handler(Exception e) {
		e.printStackTrace();
		Bukkit.getServer().getLogger().severe("Serious NMS error.");
		ViewDistManager.onDisable();
		Main.enabled = false;
	}
}
