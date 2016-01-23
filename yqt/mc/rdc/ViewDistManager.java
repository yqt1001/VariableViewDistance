package yqt.mc.rdc;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ViewDistManager {

	private static BukkitTask mainRunnable;
	private static BukkitTask tpsCheckerRunnable;
	private static LinkedList<Double> tpsTests = new LinkedList<Double>();
	private static double prevAvg = 20;
	private static int viewDist = Bukkit.getServer().getViewDistance();
	
	private final static int MAX_TPS_TESTS = 20;
	private final static long TPS_TEST_SPEED = 200;
	private final static long MAIN_RUNNABLE_SPEED = 2400;
	
	public final static String WATERMARK = "[§3§lVVD§f]";
	
	public static void onEnable() {
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
					else if(trend <= 1 && trend > 0 && dev < 0.5 && viewDist != 12)
						viewDistChange += 1;
					
					//if trending vaguely negative and high deviation, decrease render distance
					else if(trend >= -1 && trend < -0.5 && dev > 2)
						viewDistChange -= 1;
					
					//if high deviation and negative trend, decrease render distance
					else if(trend < 0 && dev > 2)
						viewDistChange -= (int) dev / 2;
					
					//if trending positive and low deviation, increase render distance
					if(trend > 0 && dev < 1 && viewDist != 12)
						viewDistChange += 1;
					
				}
				
				viewDist = viewDist + viewDistChange;
				//if there is a needed change to render distance
				if(viewDistChange != 0)
				{
					//make sure view distance doesn't get too high nor too low
					if(viewDist > 12)
						viewDist = 12;
					if(viewDist < 3)
						viewDist = 3;
					
					//change the view distance
					NMSCore.setRenderDistance(viewDist);
					
					//message admins
					for(Player p : Bukkit.getOnlinePlayers()) 
						if(p.isOp())
							p.sendMessage(WATERMARK + " §7View distance has been changed by " + viewDistChange + " to " + viewDist);
					
				}
			}
		}.runTaskTimer(Main.getThis(), 600l, MAIN_RUNNABLE_SPEED);
		
		tpsCheckerRunnable = new BukkitRunnable() {
			@Override
			public void run() {
				tpsCheck();
			}
		}.runTaskTimer(Main.getThis(), 20l, TPS_TEST_SPEED);
	}
	
	public static void onDisable() {
		mainRunnable.cancel();
		tpsCheckerRunnable.cancel();
		Main.enabled = false;
	}
	
	public static void tpsCheck() {
		//gets TPS from inside server
		double tps = NMSCore.getServerTPS()[0];
		if(tps > 20)
			tps = 20.0;
		
		//adds it to the test list
		if(tpsTests.size() == MAX_TPS_TESTS) 
			tpsTests.remove(0);
		tpsTests.add(tps);
	}
	
	public static boolean commandOverride(String[] args, CommandSender sender) {
		
		//command case, just /vvd override
		if(args.length == 1 && args[0].equalsIgnoreCase("override"))
		{
			if(Main.overwritten)
			{
				Main.overwritten = false;
				viewDist = Bukkit.getServer().getViewDistance();
				NMSCore.setRenderDistance(viewDist);
				onEnable();
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
			
			sender.sendMessage(WATERMARK + " §eCurrent server stats. 3m TPS: §a" + Math.round(avg * 100.0) / 100.0 + " §e3m Trend: §a" + Math.round(trend * 100.0) / 100.0 + " §eCurrent render distance: §a" + viewDist);
			
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
				
				if(Main.overwritten)
				{
					NMSCore.setRenderDistance(newViewDist);
					sender.sendMessage(WATERMARK + " §aYou have set the render distance to " + newViewDist);
				}
				else
				{
					Main.overwritten = true;
					NMSCore.setRenderDistance(newViewDist);
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
	
	/* misc methods */
	
	public static boolean isNumeric(String s) {
		for(Character c : s.toCharArray()) {
			if(!Character.isDigit(c))
				return false;
		}
		
		return true;
	}
}
