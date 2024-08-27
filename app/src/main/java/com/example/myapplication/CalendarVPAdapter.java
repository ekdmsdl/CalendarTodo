package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.time.LocalDate;

public class CalendarVPAdapter extends FragmentStateAdapter {
    private final IDateClickListener onClickListener;
    private final LocalDate today;

    public CalendarVPAdapter(@NonNull FragmentActivity fragmentActivity, IDateClickListener onClickListener, LocalDate today) {
        super(fragmentActivity);
        this.onClickListener = onClickListener;
        this.today = today;
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 중앙 위치에서 현재 날짜를 기준으로 페이지의 날짜를 계산
        LocalDate fragmentDate = today.plusWeeks(position - (Integer.MAX_VALUE / 2));
        return CalendarOneWeekFragment.newInstance(fragmentDate, onClickListener);
    }
}
