package yqt.mc.rdc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public static boolean enabled = true;
	public static boolean overwritten = false;
	
	@Override
	public void onEnable() {
		if(!(Bukkit.getServer().getClass().getPackage().getName()).equals("org.bukkit.craftbukkit.v1_8_R3"))
			enabled = false;
		else 
			ViewDistManager.onEnable();
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		ViewDistManager.onDisable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("vvd") && args.length > 0) 
			return ViewDistManager.commandOverride(args, sender);
			
		return false;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(e.getPlayer().isOp() && !enabled)
			e.getPlayer().sendMessage("§cVariableViewDistance is disabled due to an NMS version mismatch or error!");
	}
	
	public static Plugin getThis() {
		return Main.getPlugin(Main.class);
	}
}
