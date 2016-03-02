package yqt.mc.rdc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

public class NMSTest {

	//test to ensure the NMS code runs fine on startup
	public static void test(VVD plugin) {
		/* VERSIONS TEST */
		// Supported server versions
		List<String> versions = new LinkedList<String>(Arrays.asList("v1_8_R3", "v1_9_R1"));
		String s = Bukkit.getServer().getClass().getName().substring(23, 30);
		
		if(!versions.contains(s) && !plugin.getDebug())
		{
			//if unsupported version and not debug mode, return as disabled
			plugin.setVVDEnabled(false);
			plugin.setErrorReason(ErrorType.VERSION);
			return;
		}
		
		
		/* NMS TEST */
		//start off by creating a test nms core object
		NMSCore tester = new NMSCore(plugin, true);
		
		//start off by getting tps data
		tester.getServerTPS();
		if(!plugin.getVVDEnabled()) return;
		
		//set render distance to 6
		tester.setRenderDistance(6);
		if(!plugin.getVVDEnabled()) return;
		//back to config set value
		tester.setRenderDistance(Bukkit.getViewDistance());
	}
	
	public enum ErrorType {
		VERSION("the server running an unsupported version."),
		NMS("a caught NMS error."),
		NULL("unknown.");
		
		public String reason;
		
		private ErrorType(String reason) {
			this.reason = reason;
		}
	}
}