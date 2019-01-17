import java.awt.*
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import kotlin.collections.ArrayList

/**
 * This class show a graph of stock data.
 */
class GraphKotlin : JFrame {
    companion object {
        /**
         * Window's width.
         */
        internal const val windowWidth = 1280
        /**
         * Window's height.
         */
        internal const val windowHeight = 720
        /**
         * Graph's padding.
         */
        internal const val graphPadding = 50
    }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(ticker: String, startDate: String, endDate: String) {
        //Set frame look
        this.title = "$ticker $startDate to $endDate"
        this.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        this.setSize(windowWidth, windowHeight)
        this.isResizable = false
        this.setLocationRelativeTo(null)

        //Complete url for data file
        val url = "https://quotes.wsj.com/$ticker/historical-prices/download?MOD_VIEW=page&num_rows=90&startDate=$startDate&endDate=$endDate"

        //Download data file
        try {
            var connection = URL(url).openConnection()
            val redirect = connection.getHeaderField("Location")
            //Check 301 redirect
            if (redirect != null) {
                connection = URL(redirect).openConnection()
            }

            val inputStreamReader = InputStreamReader(connection.getInputStream())
            val fileWriter = FileWriter("./temp.csv")

            var c: Int
            while (true) {
                c = inputStreamReader.read()
                if (c == -1) break
                fileWriter.write(c)
            }

            fileWriter.close()
            inputStreamReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //Read data file
        var data: ArrayList<List<String>>? = null
        var openData: ArrayList<List<String>>? = null
        var highData: ArrayList<List<String>>? = null
        var lowData: ArrayList<List<String>>? = null
        var closeData: ArrayList<List<String>>? = null
        var volumeData: ArrayList<List<String>>? = null
        try {
            val fileReader = FileReader("./temp.csv")
            val scanner = Scanner(fileReader)
            //Skip the first line
            if (scanner.hasNextLine()) scanner.nextLine()

            data = ArrayList()
            openData = ArrayList()
            highData = ArrayList()
            lowData = ArrayList()
            closeData = ArrayList()
            volumeData = ArrayList()
            while (scanner.hasNextLine()) {
                val newLine = scanner.nextLine()
                //Split one line by comma
                val singleData = newLine.split(",")
                if (singleData.size < 6) continue //When missing values
                data.add(singleData)
                openData.add(listOf(singleData[0], singleData[1]))
                highData.add(listOf(singleData[0], singleData[2]))
                lowData.add(listOf(singleData[0], singleData[3]))
                closeData.add(listOf(singleData[0], singleData[4]))
                volumeData.add(listOf(singleData[0], singleData[5]))
            }
            scanner.close()
            fileReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //Check data before showing
        val promptLabel = JLabel("", JLabel.CENTER)
        promptLabel.font = MarketGUIKotlin.mainFont
        if (data == null) {
            promptLabel.text = "Load file failed"
            this.add(promptLabel, BorderLayout.CENTER)
            this.isVisible = true
            return
        } else if (data.size == 0) {
            promptLabel.text = "No data available"
            this.add(promptLabel, BorderLayout.CENTER)
            this.isVisible = true
            return
        }

        //First column is date, so set special max and min value of first column
        //for not affecting max and min value of all data (except volume)
        val maxValue = DoubleArray(5)
        maxValue[0] = Double.MIN_VALUE

        val minValue = DoubleArray(5)
        minValue[0] = Double.MAX_VALUE

        //Find every column's max and min
        for (i in 1..4) {
            maxValue[i] = data[0][i].toDouble()
            //Find one column's max
            for (d in data) {
                val temp = d[i].toDouble()
                maxValue[i] = if (maxValue[i] > temp) maxValue[i] else temp
            }

            minValue[i] = data[0][i].toDouble()
            //Find one column's min
            for (d in data) {
                val temp = d[i].toDouble()
                minValue[i] = if (minValue[i] < temp) minValue[i] else temp
            }
        }

        //Find max of all value (except volume)
        var realMax = maxValue[0]
        for (d in maxValue) {
            realMax = if (realMax > d) realMax else d
        }

        //Find min of all value (except volume)
        var realMin = minValue[0]
        for (d in minValue) {
            realMin = if (realMin < d) realMin else d
        }

        //For better looks of graph, calculate a bigger interval
        val top = realMax + (realMax - realMin) * 0.05

        val tmp = realMin - (realMax - realMin) * 0.05
        val bottom = if (tmp > 0.0) tmp else 0.0

        //Find max volume
        var maxVolume = data[0][5].toDouble()
        for (d in data) {
            val temp = d[5].toDouble()
            maxVolume = if (maxVolume > temp) maxVolume else temp
        }

        //Find min volume
        var minVolume = data[0][5].toDouble()
        for (d in data) {
            val temp = d[5].toDouble()
            minVolume = if (minVolume < temp) minVolume else temp
        }

        //For better looks of graph, calculate a bigger interval
        val topVolume = maxVolume + (maxVolume - minVolume) * 0.05

        val temp = minVolume - (maxVolume - minVolume) * 0.05
        val bottomVolume = if (temp > 0.0) temp else 0.0

        //Create several graphs and add into a tabbed pane
        val tabbedPane = JTabbedPane()
        tabbedPane.add("Close", GraphPanelKotlin(closeData!!, top, bottom))
        tabbedPane.add("Open", GraphPanelKotlin(openData!!, top, bottom))
        tabbedPane.add("High", GraphPanelKotlin(highData!!, top, bottom))
        tabbedPane.add("Low", GraphPanelKotlin(lowData!!, top, bottom))
        tabbedPane.add("Volume", GraphPanelKotlin(volumeData!!, topVolume, bottomVolume))
        this.add(tabbedPane)

        this.pack()
        this.isVisible = true
    }

    private inner class GraphPanelKotlin(private val data: ArrayList<List<String>>, private val top: Double, private val bottom: Double) : JPanel() {
        init {
            this.preferredSize = Dimension(GraphKotlin.windowWidth, GraphKotlin.windowHeight)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)

            //Anti-aliasing, for better look
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            //Draw white background
            g2.color = Color.WHITE
            g2.fillRect(GraphKotlin.graphPadding, GraphKotlin.graphPadding, GraphKotlin.windowWidth - GraphKotlin.graphPadding * 2, GraphKotlin.windowHeight - GraphKotlin.graphPadding * 2)

            val numY = 10
            //Create horizontal lines and labels of Y axis
            for (i in 0..numY) {
                val x0 = GraphKotlin.graphPadding
                val y0 = ((GraphKotlin.windowHeight - GraphKotlin.graphPadding * 2) * ((numY - i) * 1.0) / numY + GraphKotlin.graphPadding).toInt()
                val x1 = GraphKotlin.windowWidth - GraphKotlin.graphPadding
                val y1 = y0
                g2.color = Color(215, 215, 215)
                g2.drawLine(x0, y0, x1, y1)

                g2.color = Color.BLACK
                g2.drawLine(x0, y0, x0 + 5, y1)
                val yLabel = (((bottom + (top - bottom) * (i * 1.0 / numY)) * 100).toInt() / 100.0).toString()
                val metrics = g2.fontMetrics
                val labelWidth = metrics.stringWidth(yLabel)
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.height / 2) - 3)
            }

            //X axis
            g2.drawLine(GraphKotlin.graphPadding, GraphKotlin.windowHeight - GraphKotlin.graphPadding, GraphKotlin.windowWidth - GraphKotlin.graphPadding, GraphKotlin.windowHeight - GraphKotlin.graphPadding)

            val numX = data.size - 1
            //Create vertical lines and labels of Y axis
            for (i in 0..numX) {
                val x0 = ((GraphKotlin.windowWidth - GraphKotlin.graphPadding * 2) * (i * 1.0) / numX + GraphKotlin.graphPadding).toInt()
                val y0 = GraphKotlin.graphPadding
                val x1 = x0
                val y1 = GraphKotlin.windowHeight - GraphKotlin.graphPadding
                g2.color = Color(215, 215, 215)
                g2.drawLine(x0, y0, x1, y1)

                g2.color = Color.BLACK
                g2.drawLine(x0, y1 - 5, x1, y1)
                //For better X axis labels display
                if (((numX + 1) <= 15) || ((numX + 1) <= 45 && i % 3 == 0) || ((numX + 1) > 45 && i % 6 == 0)) {
                    val xLabel = data[numX - i][0]
                    val metrics = g2.fontMetrics
                    val labelWidth = metrics.stringWidth(xLabel)
                    g2.drawString(xLabel, x1 - labelWidth / 2, y1 + metrics.height)
                }
            }

            //Y axis
            g2.drawLine(GraphKotlin.graphPadding, GraphKotlin.graphPadding, GraphKotlin.graphPadding, GraphKotlin.windowHeight - GraphKotlin.graphPadding)

            //Create points associated to data
            val pointData = ArrayList<List<Int>>()
            for (i in 0..numX) {
                val x = (GraphKotlin.windowWidth - GraphKotlin.graphPadding - (GraphKotlin.windowWidth - GraphKotlin.graphPadding * 2) * i * 1.0 / numX).toInt()
                val y = (GraphKotlin.graphPadding + (GraphKotlin.windowHeight - GraphKotlin.graphPadding * 2) * (top - (data[i][1]).toDouble()) / (top - bottom)).toInt()
                pointData.add(listOf(x, y))
            }

            //Draw lines to connect points
            for (i in 0..(pointData.size - 2)) {
                val x1 = pointData[i][0]
                val y1 = pointData[i][1]
                val x2 = pointData[i + 1][0]
                val y2 = pointData[i + 1][1]
                g2.drawLine(x1, y1, x2, y2)
            }

            //Show every point's value near to the point
            for (i in 0..(pointData.size - 1)) {
                val x = pointData[i][0]
                val y = pointData[i][1]

                val value = data[i][1]
                val metrics = g2.fontMetrics
                val labelWidth = metrics.stringWidth(value)
                g2.drawString(value, x - labelWidth / 2, y - metrics.height)
            }
        }
    }
}