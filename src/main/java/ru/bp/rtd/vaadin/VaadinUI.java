package ru.bp.rtd.vaadin;


import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.List;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

    @Autowired
    GBCrashAnalyzerService gbCrashAnalyzerService;

    @Override
    protected void init(VaadinRequest request) {

        List<Row> rows = gbCrashAnalyzerService.getCrashCount("C:/Users/810681/Downloads/MakeModel2015/2015_Make_Model.csv");
        String categories = "";
        String series = "";
        for (Row row : rows) {
            String make = row.getString(0);

            if(!"NULL".equalsIgnoreCase(make)) {
                Long year = row.getLong(1);
                categories = categories + "'" + make + "'" + ",";

                series = series + "{name: '" + make + "', y: " + year + "},";
            }

        }


        HighChart chart = new HighChart();

        chart.setHcjs("var options = { title: {  text: 'Crashes by make'}, chart: {\n" +
                "        type: 'column', " +
                "    },\n" +
                "\n" +
                "    xAxis: {\n" +
                "        categories: ["+categories+"]" +
                "    },\n" +
                "\n" +
                "    series: [ {name: 'Make', data: ["+series+"]" +
                "    }]};");

        setContent(chart);
    }
}