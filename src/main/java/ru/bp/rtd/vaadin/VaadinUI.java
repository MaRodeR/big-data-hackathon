package ru.bp.rtd.vaadin;


import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import org.vaadin.highcharts.HighChart;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

    @Override
    protected void init(VaadinRequest request) {

        HighChart chart = new HighChart();
        chart.setHcjs("var options = { title: { text: 'Accidents by make / year' }, xAxis: {type: 'datetime', maxZoom: 48 * 3600 * 1000}," +
                " series: [{\n" +
                "        data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4],\n" +
                "        pointStart: Date.UTC(2010, 0, 1),\n" +
                "        pointInterval: 24 * 365 * 3600 * 1000\n" +
                "    }]" +
                "};");

        setContent(chart);
    }
}