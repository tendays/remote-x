/**
 * 
 */
package org.gamboni.remote.server;

import spark.Spark;

/**
 * @author tendays
 *
 */
public class Server {

	public void run() {
		exposeCommand("key");
		exposeCommand("type");
		exposeCommand("click");
		exposeCommand("mousedown");
		exposeCommand("mouseup");

		Spark.post("/move", (req, res) -> {
			int comma = req.body().indexOf(',');
			String dx = req.body().substring(0, comma);
			String dy = req.body().substring(comma+1);
			Runtime.getRuntime().exec(new String[]{"/usr/bin/xdotool", "mousemove_relative", "--", dx, dy});
			return "ok";
		});
	}

	private void exposeCommand(String command) {
		Spark.post("/"+command, (req,res) -> {
			Runtime.getRuntime().exec(new String[]{"/usr/bin/xdotool", command, req.body()});
			return "ok";
		});
	}

}
