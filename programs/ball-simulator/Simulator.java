package simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author alex_liao
 * 
 *         Copyright (c) 2017 Alexander Liao
 * 
 *         Permission is hereby granted, free of charge, to any person obtaining
 *         a copy of this software and associated documentation files (the
 *         "Software"), to deal in the Software without restriction, including
 *         without limitation the rights to use, copy, modify, merge, publish,
 *         distribute, sublicense, and/or sell copies of the Software, and to
 *         permit persons to whom the Software is furnished to do so, subject to
 *         the following conditions:
 * 
 *         The above copyright notice and this permission notice shall be
 *         included in all copies or substantial portions of the Software.
 * 
 *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *         EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *         MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *         NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *         BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *         ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *         CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *         SOFTWARE.
 */

public class Simulator {
	private static final Map<Integer, Ball> balls = new HashMap<>();
	private static int layers, length;

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		boolean output_states = true;
		for (String arg : args) {
			if (arg.equals("--ignore-states")) {
				output_states = false;
			}
		}
		List<String> layers = new ArrayList<>();
		String layer;
		while (true) {
			layer = reader.readLine();
			if (layer.contains("#")) {
				layer = layer.substring(0, layer.indexOf("#"));
			}
			if (layer.trim().isEmpty()) {
				break;
			} else {
				layers.add(layer);
			}
		}
		Simulator.layers = layers.size();
		int length = layers.get(0).length();
		for (int i = 1; i < layers.size(); i++) {
			int l = layers.get(i).length();
			length = length > l ? length : l;
		}
		Simulator.length = length;
		int[][] hidden_layers = new int[layers.size()][];
		int[][] metadata = new int[layers.size()][length];
		int[][] mem = new int[layers.size()][length];
		for (int i = 0; i < layers.size(); i++) {
			StringBuffer string = new StringBuffer(length);
			string.append(layers.get(i));
			for (int j = 0; j < length - layers.get(i).length(); j++) {
				string.append(' ');
			}
			layers.set(i, string.toString());
		}
		for (int i = 0; i < layers.size(); i++) {
			layer = layers.get(i);
			int[] hidden_layer = new int[layer.length()];
			for (int j = 0; j < layer.length(); j++) {
				char character = layer.charAt(j);
				if (character == '1') {
					createBall(i, j);
				} else if (!balls.containsKey((int) character)
						&& character >= 'a' && character <= 'z') {
					System.out.print(character + ":");
					int value;
					if ((value = Integer.parseInt(reader.readLine().trim())) != 0) {
						balls.put((int) character, new Ball(character, i, j,
								value));
					}
				}
			}
			hidden_layers[i] = hidden_layer;
		}

		while (keepRunning()) {
			List<Integer> keys = new ArrayList<>(balls.keySet());
			Collections.sort(keys);
			for (Integer id : keys) {
				Ball ball = balls.get(id);
				if (ball == null) {
				} else if (!valid(ball)) {
					balls.remove(ball.name);
				} else {
					char space = layers.get(ball.layer).charAt(ball.column);
					int data = metadata[ball.layer][ball.column];
					boolean run = ball.value == 0 || ball.value != data;
					if (run) {
						if (space == '_') {
							hidden_layers[ball.layer][ball.column]--;
							if (ball.direction == 0 && ball.levitating != 1) {
								balls.remove(ball.name);
								break;
							} else {
								ball.column += ball.direction;
								if (ball.levitating == 1) {
									ball.layer--;
								}
							}
						} else if (space == '\\' || space == '/') {
							if (ball.levitating == 1 && ball.direction == 0) {
								ball.direction = space == '/' ? 1 : -1;
								ball.column += ball.direction;
							} else if (ball.direction == 0) {
								ball.direction = space == '/' ? -1 : 1;
								ball.column += ball.direction;
							} else {
								ball.direction = 0;
								ball.levitating = -1;
								ball.layer++;
							}
						} else if (space == '^') {
							ball.direction = 0;
							ball.levitating = 1;
							ball.layer--;
						} else {
							if (space >= 'A' && space <= 'Z') {
								hidden_layers[ball.layer][ball.column]++;
							} else if (space == 'I') {
								ball.value++;
							} else if (space == 'D') {
								ball.value--;
							} else if (space == ',') {
								System.out.print(ball.name + ":");
								ball.value = Integer.parseInt(reader.readLine()
										.trim());
							} else if (space == '.') {
								System.out.print((char) ball.value);
							} else if (space == 'P') {
								System.out.println(ball.value);
							} else if (space == '<' || space == '>') {
								metadata[ball.layer][ball.column
										+ (space == '<' ? -1 : 1)] = ball.value;
								balls.remove(ball.name);
							}
							ball.layer -= ball.levitating;
							ball.direction = 0;
						}
					}
				}
			}

			for (int i = 0; i < hidden_layers.length; i++) {
				if (emptyLayer(i)) {
					int[] hidden_layer = hidden_layers[i];
					for (int j = 0; j < hidden_layer.length; j++) {
						if (hidden_layer[j] < 0 && -hidden_layer[j] % 2 == 0) {
							createBall(i + 1, j);
							hidden_layer[j] = 0;
						}
					}
				}
			}
		}

		if (output_states) {
			for (int i = 0; i < hidden_layers.length; i++) {
				int[] hidden_layer = hidden_layers[i];
				for (int j = 0; j < hidden_layer.length; j++) {
					char c;
					if ((c = layers.get(i).charAt(j)) >= 'A' && c <= 'Z') {
						System.out.println(c + ":" + hidden_layer[j]);
					}
				}
			}
		}
	}

	public static boolean emptyLayer(int layer) {
		for (Ball ball : balls.values()) {
			if (ball.layer == layer) {
				return false;
			}
		}
		return true;
	}

	public static boolean valid(Ball ball) {
		return ball.layer >= 0 && ball.layer < layers && ball.column >= 0
				&& ball.column < length;
	}

	public static boolean keepRunning() {
		for (Ball ball : balls.values()) {
			if (ball.layer >= 0 && ball.layer < layers && ball.column >= 0
					&& ball.column < length) {
				return true;
			}
		}
		return false;
	}

	public static void createBall(int layer, int column) {
		int k = 0;
		while ((k < 'a' || k > 'z') && balls.containsKey(k)) {
			k++;
		}
		balls.put(k, new Ball(k, layer, column, 1));
	}

	public static final class Ball {
		public int name, layer, column, direction, levitating, value;

		public Ball(int name, int layer, int column, int value) {
			this.name = name;
			this.layer = layer;
			this.column = column;
			this.direction = 0;
			this.levitating = -1;
			this.value = value;
		}

		public String toString() {
			return "Ball " + name + ": [" + layer + ", " + column + "]: "
					+ value;
		}
	}
}