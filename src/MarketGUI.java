import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.YearMonth;

/**
 * This class let user choose ticker and date.
 */
public class MarketGUI extends JFrame {
	/**
	 * The main font of UI.
	 */
	final static Font MAIN_FONT = new Font("Arial", Font.PLAIN, 24);
	/**
	 * Window's width.
	 */
	private final static int WINDOW_WIDTH = 720;
	/**
	 * Window's height.
	 */
	private final static int WINDOW_HEIGHT = 320;
	/**
	 * Available ticker symbols.
	 */
	private final static String[] tickerSymbols = {"A", "AAPL", "BRK.A", "C", "GOOG", "HOG",
			"HPQ", "INTC", "KO", "LUV", "MMM", "MSFT", "T", "TGT", "TXN", "WMT"};
	/**
	 * Corresponded ticker names.
	 */
	private final static String[] tickerNames = {"Agilent Technologies", "Apple Inc.", "Berkshire Hathaway",
			"Citigroup", "Alphabet Inc.", "Harley-Davidson Inc.", "Hewlett-Packard", "Intel",
			"The Coca-Cola Company", "Southwest Airlines", "3M", "Microsoft", "AT&T",
			"Target Corporation", "Texas Instruments", "Walmart"};
	/**
	 * Record selected ticker symbol.
	 */
	private String selectTicker;

	/**
	 * Constructor of MarketGUI class.
	 */
	private MarketGUI() {
		//Set frame look
		this.setTitle("Stock Market");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		//Select date
		DateDropDown startDate = new DateDropDown("Start Date");
		DateDropDown endDate = new DateDropDown("End Date");
		JPanel northPanel = new JPanel(new GridLayout(0, 1));
		northPanel.add(startDate);
		northPanel.add(endDate);

		//Select ticker symbol
		JComboBox<String> tickerList = new JComboBox<>(tickerNames);
		tickerList.setFont(MAIN_FONT);
		tickerList.setSelectedIndex(0);
		selectTicker = tickerSymbols[0];
		tickerList.addActionListener(e -> selectTicker = tickerSymbols[tickerList.getSelectedIndex()]);
		JPanel tickerPanel = new JPanel();
		tickerPanel.add(tickerList);
		JPanel centerPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
		centerPanel.setLayout(boxLayout);
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(tickerPanel);
		centerPanel.add(Box.createVerticalGlue());

		//OK button
		JButton button = new JButton("OK");
		button.setFont(MAIN_FONT);
		button.addActionListener(e -> new Graph(selectTicker, startDate.getDate(), endDate.getDate()));
		JPanel southPanel = new JPanel();
		southPanel.add(button);

		this.add(northPanel, BorderLayout.NORTH);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);

		this.setVisible(true);
	}

	/**
	 * Program starts here.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(MarketGUI::new);
	}

	/**
	 * This class create a date selector.
	 */
	private class DateDropDown extends JPanel implements ActionListener {
		/**
		 * Selected year.
		 */
		private int selectYear;
		/**
		 * Selected month.
		 */
		private int selectMonth;
		/**
		 * Selected day.
		 */
		private int selectDay;
		/**
		 * Year list, contains available years.
		 */
		private JComboBox<Integer> yearList;
		/**
		 * Month list, contains available months.
		 */
		private JComboBox<Integer> monthList;
		/**
		 * Day list, contains available days.
		 */
		private JComboBox<Integer> dayList;

		/**
		 * Constructor of DateDropDown class.
		 *
		 * @param prompt Prompt of this date selector.
		 */
		DateDropDown(String prompt) {
			//Create year list.
			yearList = new JComboBox<>();
			yearList.setFont(MAIN_FONT);
			for (int i = 2008; i <= 2019; i++)
				yearList.addItem(i);
			yearList.setSelectedIndex(yearList.getItemCount() - 1);
			selectYear = 2019;

			//Create month list.
			monthList = new JComboBox<>();
			monthList.setFont(MAIN_FONT);
			for (int i = 1; i <= 12; i++)
				monthList.addItem(i);
			monthList.setSelectedIndex(0);
			selectMonth = 1;

			//Create day list.
			dayList = new JComboBox<>();
			dayList.setFont(MAIN_FONT);
			setDayList();
			selectDay = 1;

			//Add action listener for year and month lists.
			yearList.addActionListener(this);
			monthList.addActionListener(this);

			//Set looks for components.
			JLabel promptLabel = new JLabel(prompt + ": ");
			promptLabel.setFont(MAIN_FONT);
			JLabel yearLabel = new JLabel("Year");
			yearLabel.setFont(MAIN_FONT);
			JLabel monthLabel = new JLabel("Month");
			monthLabel.setFont(MAIN_FONT);
			JLabel dayLabel = new JLabel("Day");
			dayLabel.setFont(MAIN_FONT);

			this.add(promptLabel);
			this.add(yearLabel);
			this.add(yearList);
			this.add(monthLabel);
			this.add(monthList);
			this.add(dayLabel);
			this.add(dayList);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(yearList))
				selectYear();
			else if (e.getSource().equals(monthList))
				selectMonth();
			else if (e.getSource().equals(dayList))
				selectDay();
		}

		/**
		 * Record selected year.
		 */
		private void selectYear() {
			selectYear = (int) yearList.getSelectedItem();
			setDayList();
		}

		/**
		 * Record selected month.
		 */
		private void selectMonth() {
			selectMonth = (int) monthList.getSelectedItem();
			setDayList();
		}

		/**
		 * Record selected day.
		 */
		private void selectDay() {
			selectDay = (int) dayList.getSelectedItem();
		}

		/**
		 * Calculate the days of specified month and recreate day list.
		 */
		private void setDayList() {
			YearMonth yearMonth = YearMonth.of(selectYear, selectMonth);
			dayList.removeActionListener(this);
			dayList.removeAllItems();
			int daysInMonth = yearMonth.lengthOfMonth();
			for (int i = 1; i <= daysInMonth; i++)
				dayList.addItem(i);
			dayList.setSelectedIndex(0);
			dayList.addActionListener(this);
		}

		/**
		 * Get the selected date.
		 *
		 * @return Selected date, e.g. "12/31/2018";
		 */
		String getDate() {
			return selectMonth + "/" + selectDay + "/" + selectYear;
		}
	}
}
