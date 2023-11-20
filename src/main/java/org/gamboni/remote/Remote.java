/**
 * 
 */
package org.gamboni.remote;

import java.util.Arrays;

import org.gamboni.remote.client.Client;
import org.gamboni.remote.server.Server;

/**
 * @author tendays
 *
 */
public class Remote {
	public static void main(String[] a) {
		try {
			if (a.length < 1) {
				usage();
			} else if (a[0].equals("--server")) {
				new Server().run();
			} else if (a[0].equals("--client")) {
				new Client().run(Arrays.asList(a).subList(1, a.length).toArray(String[]::new));
			} else {
				usage();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static void usage() {
		System.err.println(Remote.class.getSimpleName() +" --client host1 [host2]|--server");
	}
}
