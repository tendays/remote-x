/**
 * 
 */
package org.gamboni.remote.client;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * @author tendays
 *
 */
public class Client {
	public void run(String... hosts) {
		var frame = new JFrame("Remote");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(v(Stream.of(hosts).map(this::zoomControls).toArray(JComponent[]::new)));
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel zoomControls(String host) {
		return panel(host, v(
				h(button("Audio", post(host, "/key", "Alt+a")), button("Video", post(host, "/key", "Alt+v")),
						button("Chat", post(host, "/key", "Alt+h")), button("CLOSE", post(host, "/key", "Alt+F4"))),
				h(toggle("L", post(host, "/mousedown", "1"), post(host, "/mouseup", "1")), button("R", post(host, "/click", "3")),
						button("v", post(host, "/key", "XF86AudioLowerVolume")),
						button("^", post(host, "/key", "XF86AudioRaiseVolume"))),
				new TouchPad(notifyMouseMove(host), keyTyped -> post(host, "/type", Character.toString(keyTyped))
						)));
	}

	private JPanel v(JComponent... contents) {
		var p = new JPanel();
		p.setLayout(new GridLayout(contents.length, 1));
		for (var c : contents) {
			p.add(c);
		}
		return p;
	}

	private JPanel h(JComponent... contents) {
		var p = new JPanel();
		p.setLayout(new GridLayout(1, contents.length));
		for (var c : contents) {
			p.add(c);
		}
		return p;
	}

	private JPanel panel(String title, JComponent... contents) {
		var p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(title));
		for (var c : contents) {
			p.add(c);
		}
		return p;
	}

	private Supplier<CompletableFuture<?>> post(String host, String url, String body) {
		var uri = uri(host, url);
		return () -> sendRequest(uri, body);
	}

	private TouchPad.Listener notifyMouseMove(String host) {
		var uri = uri(host, "/move");
		return (dx, dy) -> sendRequest(uri, dx + "," + dy);
	}

	private CompletableFuture<? extends HttpResponse<?>> sendRequest(URI uri, String body) {
		return HttpClient.newBuilder().build().sendAsync(
				HttpRequest.newBuilder(uri).POST(BodyPublishers.ofString(body)).build(), BodyHandlers.discarding());
	}

	private URI uri(String host, String path) {
		try {
			return new URI("http://" + host + ":4567" + path);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private JButton button(String label, Supplier<?> action) {
		var b = new JButton(label);
		b.addActionListener(evt -> action.get());
		return b;
	}
	
	private JToggleButton toggle(String label, Supplier<?> onPress, Supplier<?> onRelease) {
		var b = new JToggleButton(label);
		b.addActionListener(evt -> {
			if (b.isSelected()) {
				onPress.get();
			} else {
				onRelease.get();
			}
		});
		return b;
	}
}
