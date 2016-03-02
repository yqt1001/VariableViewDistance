package yqt.mc.rdc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import yqt.mc.rdc.NMSTest.ErrorType;

public class VVD extends JavaPlugin implements Listener {

	private boolean enabled = true;
	private boolean overwritten = false;
	private boolean debug = false;
	private ErrorType reason =  ErrorType.NULL;
	
	private int MAX_DIST = Bukkit.getViewDistance();
	private ViewDistManager core = null;
	private NMSCore nms;
	
	@Override
	public void onEnable() {
		//run startup test
		NMSTest.test(this);
		Bukkit.getLogger().info("VVD startup test " + (enabled ? "successful." : "failed due to " + reason.reason));
		
		//create core object for command handling no matter what
		core = new ViewDistManager(this);
		
		//if test passed create nms core, if failed register join event listener
		if(enabled)
			nms = new NMSCore(this, false);
		else
			Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		core.onDisable();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("vvd") && args.length > 0) 
			return core.commandHandler(args, sender);
			
		return false;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(e.getPlayer().isOp() && !enabled)
			e.getPlayer().sendMessage(this.getErrorMessage());
	}
	
	public int getMaxRenderDistance() {
		return MAX_DIST;
	}
	
	public ViewDistManager getVVDManager() {
		return core;
	}
	
	public NMSCore getNMS() {
		return nms;
	}
	
	public boolean getVVDEnabled() {
		return enabled;
	}
	
	public void setVVDEnabled(boolean en) {
		enabled = en;
	}
	
	public boolean getOverwritten() {
		return overwritten;
	}
	
	public void setOverwritten(boolean ov) {
		overwritten = ov;
	}
	
	public boolean getDebug() {
		return debug;
	}
	
	public void setErrorReason(ErrorType r) {
		reason = r;
	}
	
	public String getErrorMessage() {
		return ViewDistManager.WATERMARK + " §cVariableViewDistance is disabled due to " + reason.reason;
	}
}
