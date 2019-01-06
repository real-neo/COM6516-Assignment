import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class show a graph of stock data.
 */
class Graph extends JFrame {
	/**
	 * Window's width.
	 */
	private final static int WINDOW_WIDTH = 1280;
	/**
	 * Window's height.
	 */
	private final static int WINDOW_HEIGHT = 720;
	/**
	 * Graph's padding.
	 */
	private final static int GRAPH_PADDING = 50;

	/**
	 * Constructor of Graph class.
	 *
	 * @param ticker    Ticker symbol.
	 * @param startDate Start date.
	 * @param endDate   End date.
	 */
	Graph(String ticker, String startDate, String endDate) {
		//Set frame look
		this.setTitle(ticker + " " + startDate + " to " + endDate);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		//Complete url for data file
		String url = "https://quotes.wsj.com/" + ticker
				+ "/historical-prices/download?MOD_VIEW=page&num_rows=90&startDate="
				+ startDate + "&endDate=" + endDate;

		//Download data file
		try {
			URL website = new URL(url);
			URLConnection connection = website.openConnection();
			String redirect = connection.getHeaderField("Location");
			//Check 301 redirect
			if (redirect != null) {
				connection = new URL(redirect).openConnection();
			}

			InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
			FileWriter fileWriter = new FileWriter("./temp.csv");

			int c;
			while ((c = inputStreamReader.read()) != -1) {
				fileWriter.write(c);
			}

			fileWriter.close();
			inputStreamReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Read data file
		ArrayList<String[]> data = null;
		ArrayList<String[]> openData = null;
		ArrayList<String[]> highData = null;
		ArrayList<String[]> lowData = null;
		ArrayList<String[]> closeData = null;
		ArrayList<String[]> volumeData = null;
		try {
			FileReader fileReader = new FileReader("./temp.csv");
			Scanner scanner = new Scanner(fileReader);
			//Skip the first line
			if (scanner.hasNextLine()) scanner.nextLine();

			data = new ArrayList<>();
			openData = new ArrayList<>();
			highData = new ArrayList<>();
			lowData = new ArrayList<>();
			closeData = new ArrayList<>();
			volumeData = new ArrayList<>();
			while (scanner.hasNextLine()) {
				String newLine = scanner.nextLine();
				//Split one line by comma
				String[] singleData = newLine.split(",");
				data.add(singleData);
				openData.add(new String[]{singleData[0], singleData[1]});
				highData.add(new String[]{singleData[0], singleData[2]});
				lowData.add(new String[]{singleData[0], singleData[3]});
				closeData.add(new String[]{singleData[0], singleData[4]});
				volumeData.add(new String[]{singleData[0], singleData[5]});
			}
			scanner.close();
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Check data before showing
		JLabel promptLabel = new JLabel("", JLabel.CENTER);
		promptLabel.setFont(MarketGUI.MAIN_FONT);
		if (data == null) {
			promptLabel.setText("Load file failed");
			this.add(promptLabel, BorderLayout.CENTER);
			this.setVisible(true);
			return;
		} else if (data.size() == 0) {
			promptLabel.setText("No data available");
			this.add(promptLabel, BorderLayout.CENTER);
			this.setVisible(true);
			return;
		}

		//First column is date, so set special max and min value of first column
		//for not affecting max and min value of all data (except volume)
		double[] maxValue = new double[5];
		maxValue[0] = Double.MIN_VALUE;

		double[] minValue = new double[5];
		minValue[0] = Double.MAX_VALUE;

		//Find every column's max and min
		for (int i = 1; i < 5; i++) {
			maxValue[i] = Double.parseDouble(data.get(0)[i]);
			//Find one column's max
			for (String[] d : data) {
				double temp = Double.parseDouble(d[i]);
				maxValue[i] = maxValue[i] > temp ? maxValue[i] : temp;
			}

			minValue[i] = Double.parseDouble(data.get(0)[i]);
			//Find one column's min
			for (String[] d : data) {
				double temp = Double.parseDouble(d[i]);
				minValue[i] = minValue[i] < temp ? minValue[i] : temp;
			}
		}

		//Find max of all value (except volume)
		double realMax = maxValue[0];
		for (double d : maxValue) {
			realMax = realMax > d ? realMax : d;
		}

		//Find min of all value (except volume)
		double realMin = minValue[0];
		for (double d : minValue) {
			realMin = realMin < d ? realMin : d;
		}

		//For better looks of graph, calculate a bigger interval
		double top = realMax + (realMax - realMin) * 0.05;

		double tmp = realMin - (realMax - realMin) * 0.05;
		double bottom = tmp > 0.0 ? tmp : 0.0;

		//Find max volume
		double maxVolume = Double.parseDouble(data.get(0)[5]);
		for (String[] d : data) {
			double temp = Double.parseDouble(d[5]);
			maxVolume = maxVolume > temp ? maxVolume : temp;
		}

		//Find min volume
		double minVolume = Double.parseDouble(data.get(0)[5]);
		for (String[] d : data) {
			double temp = Double.parseDouble(d[5]);
			minVolume = minVolume < temp ? minVolume : temp;
		}

		//For better looks of graph, calculate a bigger interval
		double topVolume = maxVolume + (maxVolume - minVolume) * 0.05;

		double v = minVolume - (maxVolume - minVolume) * 0.05;
		double bottomVolume = v > 0.0 ? v : 0.0;

		//Create several graphs and add into a tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Close", new GraphPanel(closeData, top, bottom));
		tabbedPane.add("Open", new GraphPanel(openData, top, bottom));
		tabbedPane.add("High", new GraphPanel(highData, top, bottom));
		tabbedPane.add("Low", new GraphPanel(lowData, top, bottom));
		tabbedPane.add("Volume", new GraphPanel(volumeData, topVolume, bottomVolume));
		this.add(tabbedPane);

		this.pack();
		this.setVisible(true);
	}

	/**
	 * This class draw a graph for specified data.
	 */
	private class GraphPanel extends JPanel {
		/**
		 * Stored data.
		 */
		private ArrayList<String[]> data;
		/**
		 * The border of graph.
		 */
		private double top;
		/**
		 * The border of graph.
		 */
		private double bottom;

		/**
		 * Constructor of GraphPanel class.
		 *
		 * @param data   Stored data.
		 * @param top    Top border of graph.
		 * @param bottom Bottom border of graph.
		 */
		GraphPanel(ArrayList<String[]> data, double top, double bottom) {
			this.data = data;
			this.top = top;
			this.bottom = bottom;
			this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			//Anti-aliasing, for better look
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			//Draw white background
			g2.setColor(Color.WHITE);
			g2.fillRect(GRAPH_PADDING, GRAPH_PADDING, WINDOW_WIDTH - GRAPH_PADDING * 2, WINDOW_HEIGHT - GRAPH_PADDING * 2);

			final int Y_NUM = 10;
			//Create horizontal lines and labels of Y axis
			for (int i = 0; i < Y_NUM + 1; i++) {
				int x0 = GRAPH_PADDING;
				int y0 = (int) ((WINDOW_HEIGHT - GRAPH_PADDING * 2) * ((Y_NUM - i) * 1.0) / Y_NUM + GRAPH_PADDING);
				int x1 = WINDOW_WIDTH - GRAPH_PADDING;
				int y1 = y0;
				g2.setColor(new Color(215, 215, 215));
				g2.drawLine(x0, y0, x1, y1);

				g2.setColor(Color.BLACK);
				g2.drawLine(x0, y0, x0 + 5, y1);
				String yLabel = ((int) ((bottom + (top - bottom) * ((i * 1.0) / Y_NUM)) * 100)) / 100.0 + "";
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
			}

			//X axis
			g2.drawLine(GRAPH_PADDING, WINDOW_HEIGHT - GRAPH_PADDING, WINDOW_WIDTH - GRAPH_PADDING, WINDOW_HEIGHT - GRAPH_PADDING);

			final int X_NUM = data.size() - 1;
			//Create vertical lines and labels of Y axis
			for (int i = 0; i < X_NUM + 1; i++) {
				int x0 = (int) ((WINDOW_WIDTH - GRAPH_PADDING * 2) * (i * 1.0) / X_NUM + GRAPH_PADDING);
				int y0 = GRAPH_PADDING;
				int x1 = x0;
				int y1 = WINDOW_HEIGHT - GRAPH_PADDING;
				g2.setColor(new Color(215, 215, 215));
				g2.drawLine(x0, y0, x1, y1);

				g2.setColor(Color.BLACK);
				g2.drawLine(x0, y1 - 5, x1, y1);
				String xLabel = data.get(X_NUM - i)[0];
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(xLabel);
				g2.drawString(xLabel, x1 - labelWidth / 2, y1 + metrics.getHeight());
			}

			//Y axis
			g2.drawLine(GRAPH_PADDING, GRAPH_PADDING, GRAPH_PADDING, WINDOW_HEIGHT - GRAPH_PADDING);

			//Create points associated to data
			ArrayList<int[]> pointData = new ArrayList<>();
			for (int i = 0; i < X_NUM + 1; i++) {
				int x = (int) (WINDOW_WIDTH - GRAPH_PADDING - (WINDOW_WIDTH - GRAPH_PADDING * 2) * (i * 1.0) / X_NUM);
				int y = (int) (GRAPH_PADDING + (WINDOW_HEIGHT - GRAPH_PADDING * 2) * (top - Double.parseDouble(data.get(i)[1])) / (top - bottom));
				pointData.add(new int[]{x, y});
			}

			//Draw lines to connect points
			for (int i = 0; i < pointData.size() - 1; i++) {
				int x1 = pointData.get(i)[0];
				int y1 = pointData.get(i)[1];
				int x2 = pointData.get(i + 1)[0];
				int y2 = pointData.get(i + 1)[1];
				g2.drawLine(x1, y1, x2, y2);
			}

			//Show every point's value near to the point
			for (int i = 0; i < pointData.size(); i++) {
				int x = pointData.get(i)[0];
				int y = pointData.get(i)[1];

				String closeValue = data.get(i)[1];
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(closeValue);
				g2.drawString(closeValue, x - labelWidth / 2, y - metrics.getHeight());
			}
		}
	}
}
