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

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
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
		int length = layers.get(0).length();
		for (int i = 1; i < layers.size(); i++) {
			int l = layers.get(i).length();
			length = length > l ? length : l;
		}
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
					if (reader.readLine().trim().equals("1")) {
						balls.put((int) character, new Ball(character, i, j));
					}
				}
			}
			List<Integer> keys = new ArrayList<>(balls.keySet());
			Collections.sort(keys);
			for (Integer id : keys) {
				Ball ball = balls.get(id);
				while (ball.layer == i) {
					char space = layers.get(i).charAt(ball.column);
					if (space == '_') {
						hidden_layer[ball.column]--;
						if (ball.direction == 0) {
							balls.remove(ball.name);
							break;
						} else {
							ball.column += ball.direction;
						}
					} else if (space >= 'A' && space <= 'Z') {
						hidden_layer[ball.column]++;
						ball.direction = 0;
						ball.layer++;
					} else if (space == '\\' || space == '/') {
						if (ball.direction == 0) {
							ball.direction = space == '/' ? -1 : 1;
							ball.column += ball.direction;
						} else {
							ball.direction = 0;
							ball.layer++;
						}
					} else {
						ball.layer++;
						ball.direction = 0;
					}
				}
			}

			for (int j = 0; j < hidden_layer.length; j++) {
				char c;
				if (hidden_layer[j] < 0 && -hidden_layer[j] % 2 == 0) {
					createBall(i + 1, j);
				} else if ((c = layer.charAt(j)) >= 'A' && c <= 'Z') {
					System.out.println(c + ":" + hidden_layer[j]);
				}
			}
		}
	}

	public static void createBall(int layer, int column) {
		int k = 0;
		while ((k < 'a' || k > 'z') && balls.containsKey(k)) {
			k++;
		}
		balls.put(k, new Ball(k, layer, column));
	}

	public static final class Ball {
		public int name, layer, column, direction;

		public Ball(int name, int layer, int column) {
			this.name = name;
			this.layer = layer;
			this.column = column;
			this.direction = 0;
		}

		public String toString() {
			return "Ball " + name + ": [" + layer + ", " + column + "]";
		}
	}
}