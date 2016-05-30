package yqt.mc.rdc;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ViewDistManager {

	private BukkitTask mainRunnable;
	private BukkitTask tpsCheckerRunnable;
	private LinkedList<Double> tpsTests = new LinkedList<Double>();
	private double prevAvg = 20;
	private int viewDist = Bukkit.getViewDistance();
	private VVD plugin;
	
	private final static int MAX_TPS_TESTS = 20;
	private final static long TPS_TEST_SPEED = 200;
	private final static long MAIN_RUNNABLE_SPEED = 2400;
	public final static String WATERMARK = "[§3§lVVD§f]";
	
	public ViewDistManager(VVD plugin) {
		this.plugin = plugin;
		this.init();
	}
	
	public void init() {
		if(!plugin.getVVDEnabled())
			return;
		
		mainRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				int viewDistChange = 0;
				
				//if max tps tests have been done
				if(tpsTests.size() == MAX_TPS_TESTS)
				{
					double avg = 0;
					for(int i = 0; i < tpsTests.size(); i++)
						avg += tpsTests.get(i);
					avg = avg / tpsTests.size();
					
					//determine the deviation of the average from the ideal
					double dev = 20 - avg;
					
					//determine the trend
					double trend = avg - prevAvg;
					prevAvg = avg;
					
					
					
					//if trending significantly negative, decrease render distance
					if(trend < -1)
					{
						if(dev > 2)
							viewDistChange -= (int) dev / 2;
						else
							viewDistChange -= 1;
					}
					
					//if trending vaguely positive and low deviation, increase render distance
					else if(trend <= 1 && trend > 0 && dev < 0.5 && viewDist != plugin.getMaxRenderDistance())
						viewDistChange += 1;
					
					//if trending vaguely negative and high deviation, decrease render distance
					else if(trend >= -1 && trend < -0.5 && dev > 2)
						viewDistChange -= 1;
					
					//if high deviation and negative trend, decrease render distance
					else if(trend < 0 && dev > 2)
						viewDistChange -= (int) dev / 2;
					
					//if trending positive and low deviation, increase render distance
					if(trend > 0 && dev < 1 && viewDist != plugin.getMaxRenderDistance())
						viewDistChange += 1;
					
				}
				
				// if the current view distance is already out of bounds, stop any change
				if(viewDist >= plugin.getMaxRenderDistance() || viewDist <= 3)
					viewDistChange = 0;
				
				viewDist = viewDist + viewDistChange;
				//if there is a needed change to render distance
				if(viewDistChange != 0)
				{
					//make sure view distance doesn't get too high nor too low
					if(viewDist > plugin.getMaxRenderDistance())
						viewDist = plugin.getMaxRenderDistance();
					if(viewDist < 3)
						viewDist = 3;
					
					//change the view distance
					plugin.getNMS().setRenderDistance(viewDist);
					
					//message admins
					for(Player p : Bukkit.getOnlinePlayers()) 
						if(p.isOp())
							p.sendMessage(WATERMARK + " §7View distance has been changed by " + viewDistChange + " to " + viewDist);
					
				}
			}
		}.runTaskTimer(plugin, 600l, MAIN_RUNNABLE_SPEED);
		
		tpsCheckerRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				tpsCheck();
			}
		}.runTaskTimer(plugin, 20l, TPS_TEST_SPEED);
	}
	
	public void onDisable() {
		if(!plugin.getVVDEnabled())
			return;
		
		mainRunnable.cancel();
		tpsCheckerRunnable.cancel();
	}
	
	public void tpsCheck() {
		//gets TPS from inside server
		double tps = plugin.getNMS().getServerTPS()[0];
		if(tps > 20)
			tps = 20.0;
		
		//adds it to the test list
		if(tpsTests.size() == MAX_TPS_TESTS) 
			tpsTests.remove(0);
		tpsTests.add(tps);
	}
	
	public boolean commandHandler(String[] args, CommandSender sender) {
		
		//double check to ensure that the plugin is not enabled
		if(!plugin.getVVDEnabled())
		{
			sender.sendMessage(plugin.getErrorMessage());
			return true;
		}
		
		//command case, just /vvd override
		if(args.length == 1 && args[0].equalsIgnoreCase("override"))
		{
			if(plugin.getOverwritten())
			{
				plugin.setOverwritten(false);
				viewDist = Bukkit.getServer().getViewDistance();
				plugin.getNMS().setRenderDistance(viewDist);
				this.init();
				sender.sendMessage(WATERMARK + " §eVariable render distance is no longer overwritten.");
			}
			else
				sender.sendMessage(WATERMARK + " §cVariable render distance is not currently overwritten!");
			
			return true;
		}
		
		//command case, /vvd info
		else if(args.length == 1 && args[0].equalsIgnoreCase("info"))
		{
			double avg = 0;
			for(int i = 0; i < tpsTests.size(); i++)
				avg += tpsTests.get(i);
			avg = avg / tpsTests.size();
			
			//determine the trend
			double trend = avg - prevAvg;
			
			sender.sendMessage(WATERMARK + " §a§lCurrent server stats");
			sender.sendMessage(WATERMARK + " §eTPS stored: §a" + tpsTests.size() + "/" + MAX_TPS_TESTS);
			sender.sendMessage(WATERMARK + " §e3m TPS: §a" + Math.round(avg * 100.0) / 100.0);
			sender.sendMessage(WATERMARK + " §e3m Trend: §a" + Math.round(trend * 100.0) / 100.0);
			sender.sendMessage(WATERMARK + " §eCurrent render distance: §a" + viewDist);
			sender.sendMessage(WATERMARK + " §eVVD enabled: " + ((plugin.getVVDEnabled()) ? "§a" : "§c") + plugin.getVVDEnabled());
			sender.sendMessage(WATERMARK + " §eVVD overwritten: " + ((plugin.getOverwritten()) ? "§a" : "§e") + plugin.getOverwritten());
			
			return true;
		}
		
		//command case, /vvd override args[1]
		else if(args.length == 2 && args[0].equalsIgnoreCase("override"))
		{
			if(isNumeric(args[1]))
			{
				int newViewDist = Integer.parseInt(args[1]);
				
				if(newViewDist > 12)
					newViewDist = 12;
				if(newViewDist < 3)
					newViewDist = 3;
				
				if(plugin.getOverwritten())
				{
					plugin.getNMS().setRenderDistance(newViewDist);
					sender.sendMessage(WATERMARK + " §aYou have set the render distance to " + newViewDist);
				}
				else
				{
					plugin.setOverwritten(true);
					plugin.getNMS().setRenderDistance(newViewDist);
					onDisable();
					sender.sendMessage(WATERMARK + " §aYou have set the render distance to " + newViewDist);
				}
			}
			else
				sender.sendMessage(WATERMARK + " §cYou cannot set a non-numeric render distance!");
			
			return true;
		}
		
		
		return false;
	}
	
	/* util methods */
	
	public static boolean isNumeric(String s) {
		for(Character c : s.toCharArray()) {
			if(!Character.isDigit(c))
				return false;
		}
		
		return true;
	}
}
