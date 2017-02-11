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
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean output_states = true, debug = false;
		long delay = 0;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--ignore-states") || arg.equals("-i")) {
				output_states = false;
			} else if (arg.equals("--debug") || arg.equals("-d")) {
				debug = true;
			} else if (arg.equals("--wait") || arg.equals("-w")) {
				delay = Long.parseLong(args[++i]);
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
		boolean[][] mf = new boolean[layers.size()][length];
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
				if (character >= '0' && character <= '9') {
					createBall(i, j, character - '0');
				} else if (!balls.containsKey((int) character) && character >= 'a' && character <= 'z') {
					System.out.print(character + ":");
					int value;
					if ((value = Integer.parseInt(reader.readLine().trim())) != 0) {
						balls.put((int) character, new Ball(character, i, j, value));
					}
				}
			}
			hidden_layers[i] = hidden_layer;
		}

		while (keepRunning()) {
			for (long time = 0; time < delay; time++) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
					time--;
				}
			}
			if (debug) {
				System.out.println(balls);
				reader.readLine();
			}
			List<Integer> keys = new ArrayList<>(balls.keySet());
			Collections.sort(keys);
			for (Integer id : keys) {
				Ball ball = balls.get(id);
				if (ball == null) {
				} else if (!valid(ball)) {
					balls.remove(ball.getName());
				} else {
					char space = layers.get(ball.getLayer()).charAt(ball.getColumn());
					int data = metadata[ball.getLayer()][ball.getColumn()];
					if (ball.getValue() == 0 || ball.getValue() != data) {
						if (space == '_') {
							hidden_layers[ball.getLayer()][ball.getColumn()]--;
							if (ball.getDirection() == 0 && ball.getLevitating() != 1) {
								balls.remove(ball.getName());
								break;
							} else {
								ball.setColumn(ball.getColumn() + ball.getDirection());
								if (ball.getLevitating() == 1) {
									ball.setLayer(ball.getLayer() - 1);
								}
							}
						} else if (space == '\\' || space == '/') {
							if (ball.getLevitating() == 1 && ball.getDirection() == 0) {
								ball.setDirection(space == '/' ? 1 : -1);
								ball.setColumn(ball.getColumn() + ball.getDirection());
							} else if (ball.getDirection() == 0) {
								ball.setDirection(space == '/' ? -1 : 1);
								ball.setColumn(ball.getColumn() + ball.getDirection());
							} else {
								ball.setDirection(0);
								ball.setLevitating(-1);
								ball.setLayer(ball.getLayer() + 1);
							}
						} else if (space == '^') {
							ball.setDirection(0);
							ball.setLevitating(1);
							ball.setLayer(ball.getLayer() - 1);
						} else {
							if (space >= 'A' && space <= 'Z') {
								hidden_layers[ball.getLayer()][ball.getColumn()]++;
							} else if (space == '↑') {
								ball.setValue(ball.getValue() + 1);
							} else if (space == '↓') {
								ball.setValue(ball.getValue() - 1);
							} else if (space == '↧') {
								System.out.print(ball.getName() + ":");
								ball.setValue(Integer.parseInt(reader.readLine().trim()));
							} else if (space == '.') {
								System.out.print((char) ball.getValue());
							} else if (space == '↥') {
								System.out.println(ball.getValue());
							} else if (space == '<' || space == '>') {
								metadata[ball.getLayer()][ball.getColumn() + (space == '<' ? -1 : 1)] = ball.getValue();
								balls.remove(ball.getName());
							} else if (space == '+' || space == '-' || space == '*') {
								if (mf[ball.getLayer()][ball.getColumn()]) {
									createBall(ball.getLayer() + 1, ball.getColumn(), Operator.operators.get(space)
											.operate(ball.getValue(), mem[ball.getLayer()][ball.getColumn()]));
									balls.remove(ball.getName());
								} else {
									mem[ball.getLayer()][ball.getColumn()] = ball.getValue();
									balls.remove(ball.getName());
								}

								mf[ball.getLayer()][ball.getColumn()] ^= true;
							}
							ball.setLayer(ball.getLayer() - ball.getLevitating());
							ball.setDirection(0);
						}
					} else {
						ball.setLayer(ball.getLayer() - ball.getLevitating());
						ball.setDirection(0);
					}
				}
			}

			for (int i = 0; i < hidden_layers.length; i++) {
				if (emptyLayer(i)) {
					int[] hidden_layer = hidden_layers[i];
					for (int j = 0; j < hidden_layer.length; j++) {
						if (hidden_layer[j] < 0 && -hidden_layer[j] % 2 == 0) {
							createBall(i + 1, j, 1);
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
			if (ball.getLayer() == layer) {
				return false;
			}
		}
		return true;
	}

	public static boolean valid(Ball ball) {
		return ball.getLayer() >= 0 && ball.getLayer() < layers && ball.getColumn() >= 0 && ball.getColumn() < length;
	}

	public static boolean keepRunning() {
		for (Ball ball : balls.values()) {
			if (ball.getLayer() >= 0 && ball.getLayer() < layers && ball.getColumn() >= 0
					&& ball.getColumn() < length) {
				return true;
			}
		}
		return false;
	}

	public static void createBall(int layer, int column, int value) {
		int k = 0;
		while ((k < 'a' || k > 'z') && balls.containsKey(k)) {
			k++;
		}
		balls.put(k, new Ball(k, layer, column, value));
	}

	public static final class Ball {
		private int name;
		private int layer;
		private int column;
		private int direction;
		private int levitating;
		private int value;

		public Ball(int name, int layer, int column, int value) {
			this.setName(name);
			this.setLayer(layer);
			this.setColumn(column);
			this.setDirection(0);
			this.setLevitating(-1);
			this.setValue(value);
		}

		public String toString() {
			return "Ball " + getName() + ": [" + getLayer() + ", " + getColumn() + "]: " + getValue();
		}

		public int getName() {
			return name;
		}

		public void setName(int name) {
			this.name = name;
		}

		public int getLayer() {
			return layer;
		}

		public void setLayer(int layer) {
			this.layer = layer;
		}

		public int getColumn() {
			return column;
		}

		public void setColumn(int column) {
			this.column = column;
		}

		public int getDirection() {
			return direction;
		}

		public void setDirection(int direction) {
			this.direction = direction;
		}

		public int getLevitating() {
			return levitating;
		}

		public void setLevitating(int levitating) {
			this.levitating = levitating;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	public interface Operator {
		public int operate(int x, int y);

		@SuppressWarnings("serial")
		Map<Character, Operator> operators = new HashMap<Character, Operator>() {
			{
				put('+', (x, y) -> (x + y));
				put('-', (x, y) -> (x - y));
				put('*', (x, y) -> (x * y));
			}
		};
	}
}