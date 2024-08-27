package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentCalendarOneWeekBinding;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarOneWeekFragment extends Fragment {
    private FragmentCalendarOneWeekBinding binding;
    private List<TextView> textViewList;
    private List<LocalDate> dates;

    private LocalDate selectedDate; // 현재 선택된 날짜
    private LocalDate today; // 오늘 날짜
    private IDateClickListener onClickListener;

    private static final String PREF_NAME = "CALENDAR-APP";
    private static final String PREF_SELECTED_DATE = "SELECTED-DATE";

    private static final int COLOR_TODAY = R.color.Jblue; // 오늘 날짜 강조 색상
    private static final int COLOR_SELECTED_DATE = R.color.purple; // 선택된 날짜 강조 색상
    private static final int COLOR_DEFAULT = R.color.transparent; // 기본 상태 색상

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarOneWeekBinding.inflate(inflater, container, false);
        initViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String dateString = getArguments().getString("DATE");
            if (dateString != null) {
                today = LocalDate.now();
                LocalDate newDate = LocalDate.parse(dateString);
                calculateDatesOfWeek(newDate);
                setOneWeekDateIntoTextView();
                updateSelectedDate();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 앱 재시작 후, 날짜 강조 업데이트
        updateSelectedDate();
    }

    private void initViews() {
        textViewList = new ArrayList<>();
        textViewList.add(binding.tv1);
        textViewList.add(binding.tv2);
        textViewList.add(binding.tv3);
        textViewList.add(binding.tv4);
        textViewList.add(binding.tv5);
        textViewList.add(binding.tv6);
        textViewList.add(binding.tv7);
    }

    private void loadSelectedDate() {
        SharedPreferences sharedPreference = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String selectedDateStr = sharedPreference.getString(PREF_SELECTED_DATE, "");
        selectedDate = selectedDateStr.isEmpty() ? null : LocalDate.parse(selectedDateStr);
    }

    private void saveSelectedDate(LocalDate date) {
        SharedPreferences sharedPreference = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(PREF_SELECTED_DATE, date.toString());
        editor.apply();
    }

    private void updateSelectedDate() {
        loadSelectedDate();

        if (dates != null && !dates.isEmpty()) {
            for (int i = 0; i < textViewList.size(); i++) {
                LocalDate date = dates.get(i);
                TextView textView = textViewList.get(i);

                if (date.equals(selectedDate)) {
                    setSelectedDate(textView, date.isBefore(today), COLOR_SELECTED_DATE);
                } else if (date.equals(today)) {
                    setTodayDate(textView);
                } else {
                    resetDate(textView);
                }
            }
        }
    }

    private void resetDate(TextView textView) {
        // 기본 상태로 재설정
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(null, Typeface.NORMAL);
        textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), COLOR_DEFAULT)));
    }

    private void setSelectedDate(TextView textView, boolean isPast, int color) {
        // 선택된 날짜를 표시하는 색상 설정
        textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color)));
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    private void setTodayDate(TextView textView) {
        // 오늘 날짜를 강조 표시
        textView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), COLOR_TODAY)));
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }

    private TextView getViewForDate(LocalDate date) {
        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).equals(date)) {
                return textViewList.get(i);
            }
        }
        return null;
    }

    private void setOneWeekDateIntoTextView() {
        for (int i = 0; i < textViewList.size(); i++) {
            setDate(textViewList.get(i), dates.get(i));
        }
    }

    private void setDate(TextView textView, LocalDate date) {
        String dayOfMonth = String.valueOf(date.getDayOfMonth());
        textView.setText(dayOfMonth);
        textView.setOnClickListener(v -> {
            saveSelectedDate(date); // 날짜 선택 시 저장
            updateSelectedDate(); // 날짜 선택 후 강조 표시 업데이트
            if (onClickListener != null) {
                onClickListener.onClickDate(date); // 클릭한 날짜 전달
            }
        });

        // 오늘 날짜가 현재 주일 때만 강조 표시하지 않음
        if (today.equals(date) && selectedDate == null) {
            setTodayDate(textView);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void calculateDatesOfWeek(LocalDate startDate) {
        dates = new ArrayList<>();
        LocalDate startOfWeek = startDate.with(DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            dates.add(startOfWeek.plusDays(i));
        }
    }

    public static CalendarOneWeekFragment newInstance(LocalDate date, IDateClickListener onClickListener) {
        CalendarOneWeekFragment fragment = new CalendarOneWeekFragment();
        Bundle args = new Bundle();
        args.putString("DATE", date.toString()); // LocalDate를 String으로 저장
        fragment.setArguments(args);
        fragment.onClickListener = onClickListener;
        return fragment;
    }
}
