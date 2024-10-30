package com.example.stepappv4.ui.Report;

import static com.example.stepappv4.StepAppOpenHelper.loadStepsByHour;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.databinding.FragmentReportBinding;
import com.example.stepappv4.R;
import com.google.android.material.tabs.TabLayout;
import androidx.annotation.Nullable;

public class ReportFragment extends Fragment {

    public int todaySteps = 0;
    TextView numStepsTextView;
    AnyChartView anyChartView;

    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public Map<Integer, Integer> stepsByHour = null;
    private FragmentReportBinding binding;

    private int brutalFlag = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentReportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize chart view
        anyChartView = root.findViewById(R.id.barChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));

        Cartesian cartesian = createHourBarChart();
//        Cartesian cartesian = createWeeklyBarChart();

        anyChartView.setChart(cartesian);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Cartesian createHourBarChart() {
        stepsByHour = loadStepsByHour(getContext(), current_time);
        Map<Integer, Integer> graph_map = new TreeMap<>();
        for (int i = 0; i < 24; i++)
            graph_map.put(i, 0);
        graph_map.putAll(stepsByHour);

        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        Column column = cartesian.column(data);
        column.fill("#FF0000");

        setupChartUI(cartesian, "Number of steps per hour", "Hour of the day", "Number of steps");
        return cartesian;
    }

    public Cartesian createWeeklyBarChart() {
        Map<String, Integer> weeklySteps = StepAppOpenHelper.loadStepsByDateForLastWeek(getContext());
        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : weeklySteps.entrySet()) {
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        Column column = cartesian.column(data);
        column.fill("#00FF00"); // Change color for weekly chart

        // Tooltip and UI setup for weekly chart
        setupChartUI(cartesian, "Number of steps in the last week", "Date", "Number of steps");
        return cartesian;
    }

    private void setupChartUI(Cartesian cartesian, String title, String xAxisTitle, String yAxisTitle) {
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);
        cartesian.yAxis(0).labels().format("{%Value} Steps");
        cartesian.xAxis(0).title(xAxisTitle);
        cartesian.yAxis(0).title(yAxisTitle);
        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);
        cartesian.title(title);
        cartesian.title().padding(0d, 0d, 10d, 0d);
        cartesian.title().fontSize(16d);
        cartesian.title().fontColor("#1EB980");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Hourly"));
        tabLayout.addTab(tabLayout.newTab().setText("Daily"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0) {
                    binding.textView5.setText(R.string.hourly_report);
                    anyChartView.setChart(createHourBarChart());
                    Toast.makeText(getActivity(), "Hourly Report Selected", Toast.LENGTH_SHORT).show();
                } else {
                    binding.textView5.setText(R.string.daily_report);
                    anyChartView.setChart(createWeeklyBarChart());
                    Toast.makeText(getActivity(), "Daily Report Selected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    anyChartView.setChart(createHourBarChart());
                } else {
                    anyChartView.setChart(createWeeklyBarChart());
                }
            }
        });
    }

}
