import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.time.YearMonth
import javax.swing.*

/**
 * Program starts here.
 *
 * @param args Command line arguments.
 */
fun main(args: Array<String>) {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }

    SwingUtilities.invokeLater { MarketGUIKotlin() }
}

/**
 * This class let user choose ticker and date.
 */
class MarketGUIKotlin : JFrame() {
    /**
     * Window's width.
     */
    private val windowWidth = 720
    /**
     * Window's height.
     */
    private val windowHeight = 320
    /**
     * Available ticker symbols.
     */
    private val tickerSymbols = arrayOf("A", "AAPL", "BRK.A", "C", "GOOG", "HOG", "HPQ", "INTC", "KO", "LUV", "MMM", "MSFT", "T", "TGT", "TXN", "WMT")
    /**
     * Corresponded ticker names.
     */
    private val tickerNames = arrayOf("Agilent Technologies", "Apple Inc.", "Berkshire Hathaway", "Citigroup", "Alphabet Inc.", "Harley-Davidson Inc.", "Hewlett-Packard", "Intel", "The Coca-Cola Company", "Southwest Airlines", "3M", "Microsoft", "AT&T", "Target Corporation", "Texas Instruments", "Walmart")

    init {
        //Set frame look
        this.title = "Stock Market"
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.setSize(windowWidth, windowHeight)
        this.isResizable = false
        this.setLocationRelativeTo(null)

        //Select date
        val startDate = DateDropDownKotlin("Start Date")
        val endDate = DateDropDownKotlin("End Date")
        val northPanel = JPanel(GridLayout(0, 1))
        northPanel.add(startDate)
        northPanel.add(endDate)

        //Select ticker symbol
        val tickerList = JComboBox(tickerNames)
        tickerList.font = mainFont
        tickerList.selectedIndex = 0
        val tickerPanel = JPanel()
        tickerPanel.add(tickerList)
        val centerPanel = JPanel()
        val boxLayout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        centerPanel.layout = boxLayout
        centerPanel.add(Box.createVerticalGlue())
        centerPanel.add(tickerPanel)
        centerPanel.add(Box.createVerticalGlue())

        //OK button
        val button = JButton("OK")
        button.font = mainFont
        button.addActionListener { GraphKotlin(tickerSymbols[tickerList.selectedIndex], startDate.date, endDate.date) }
        val southPanel = JPanel()
        southPanel.add(button)

        this.add(northPanel, BorderLayout.NORTH)
        this.add(centerPanel, BorderLayout.CENTER)
        this.add(southPanel, BorderLayout.SOUTH)

        this.isVisible = true
    }

    /**
     * This class create a date selector.
     */
    private inner class DateDropDownKotlin(prompt: String) : JPanel(), ActionListener {
        /**
         * Year list, contains available years.
         */
        private val yearList: JComboBox<Int> = JComboBox()
        /**
         * Month list, contains available months.
         */
        private val monthList: JComboBox<Int> = JComboBox()
        /**
         * Day list, contains available days.
         */
        private val dayList: JComboBox<Int> = JComboBox()

        /**
         * Get the selected date.
         *
         * @return Selected date, e.g. "12/31/2018";
         */
        internal val date: String
            get() = monthList.selectedItem.toString() + "/" + dayList.selectedItem + "/" + yearList.selectedItem

        init {
            //Create year list
            yearList.font = mainFont
            for (i in 2008..2019)
                yearList.addItem(i)
            yearList.selectedIndex = yearList.itemCount - 1

            //Create month list
            monthList.font = mainFont
            for (i in 1..12)
                monthList.addItem(i)
            monthList.selectedIndex = 0

            //Create day list
            dayList.font = mainFont
            setDayList()

            //Add action listener for year and month lists
            yearList.addActionListener(this)
            monthList.addActionListener(this)

            //Set looks for components
            val promptLabel = JLabel("$prompt: ")
            promptLabel.font = mainFont
            val yearLabel = JLabel("Year")
            yearLabel.font = mainFont
            val monthLabel = JLabel("Month")
            monthLabel.font = mainFont
            val dayLabel = JLabel("Day")
            dayLabel.font = mainFont

            this.add(promptLabel)
            this.add(yearLabel)
            this.add(yearList)
            this.add(monthLabel)
            this.add(monthList)
            this.add(dayLabel)
            this.add(dayList)
        }

        /**
         * Invoked when an action occurs.
         */
        override fun actionPerformed(e: ActionEvent) {
            if (e.source == yearList || e.source == monthList)
                setDayList()
        }

        /**
         * Calculate the days of specified month and recreate day list.
         */
        private fun setDayList() {
            val yearMonth = YearMonth.of(yearList.selectedItem as Int, monthList.selectedItem as Int)
            dayList.removeAllItems()
            val daysInMonth = yearMonth.lengthOfMonth()
            for (i in 1..daysInMonth)
                dayList.addItem(i)
            dayList.selectedIndex = 0
        }
    }

    companion object {
        /**
         * The main font of UI.
         */
        @JvmField
        val mainFont = Font("Arial", Font.PLAIN, 24)
    }
}
