package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TaskDialogFragment extends DialogFragment {

    public static final String KEY_TASK = "task";
    public static final String KEY_TIME = "time";
    public static final String KEY_HAS_SPECIFIC_TIME = "hasSpecificTime";
    public static final String KEY_DATE = "date";

    private EditText etTask;
    private RadioGroup radioGroupTime;
    private TimePicker timePicker;
    private RadioButton radioButtonAnytime;
    private RadioButton radioButtonSpecificTime;
    private ImageButton closeButton;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 다이얼로그 레이아웃 인플레이션
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_my_dialog, null);

        // UI 요소 초기화
        etTask = view.findViewById(R.id.taskInput);
        radioGroupTime = view.findViewById(R.id.radioGroupTime);
        timePicker = view.findViewById(R.id.timePicker);
        radioButtonAnytime = view.findViewById(R.id.radioButtonAnytime);
        radioButtonSpecificTime = view.findViewById(R.id.radioButtonSpecificTime);
        closeButton = view.findViewById(R.id.closeButton);

        // 시간 피커 24시간 형식으로 설정
        timePicker.setIs24HourView(true);

        // 라디오 버튼 체크 변경 리스너
        radioGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonAnytime) {
                timePicker.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioButtonSpecificTime) {
                timePicker.setVisibility(View.VISIBLE);
            }
        });

        // 날짜를 arguments로부터 읽어옵니다
        Bundle args = getArguments();
        String dateString = (args != null && args.getString(KEY_DATE) != null) ? args.getString(KEY_DATE) : LocalDate.now().toString();
        LocalDate selectedDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);

        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view)
                .setPositiveButton("등록", null); // 클릭 리스너를 나중에 설정합니다.

        // 다이얼로그 생성 후 클릭 리스너 설정
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String task = etTask.getText().toString().trim();

                if (!validateInput(task)) {
                    return; // 태스크가 유효하지 않으면 등록을 중지
                }

                int selectedRadioId = radioGroupTime.getCheckedRadioButtonId();
                if (selectedRadioId == -1) {
                    // 라디오 버튼이 선택되지 않은 경우 경고 팝업을 띄웁니다.
                    showTimeSelectionAlert();
                    return;
                }

                boolean hasSpecificTime = radioButtonSpecificTime.isChecked();
                String time = null;
                if (hasSpecificTime) {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    time = String.format("%02d:%02d", hour, minute); // 24시간 형식
                }

                // 데이터 반환
                Bundle result = new Bundle();
                result.putString(KEY_TASK, task);
                result.putString(KEY_TIME, time);
                result.putBoolean(KEY_HAS_SPECIFIC_TIME, hasSpecificTime);
                result.putString(KEY_DATE, selectedDate.toString()); // 날짜를 올바르게 전달

                getParentFragmentManager().setFragmentResult("requestKey", result);
                dialog.dismiss();
            });
        });

        closeButton.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private boolean validateInput(String task) {
        if (TextUtils.isEmpty(task)) {
            etTask.setError("할 일을 입력하세요.");
            showTaskInputAlert();
            return false;
        }
        return true;
    }

    private void showTaskInputAlert() {
        new AlertDialog.Builder(requireActivity())
                .setTitle("경고")
                .setMessage("할 일을 입력해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }

    private void showTimeSelectionAlert() {
        new AlertDialog.Builder(requireActivity())
                .setTitle("경고")
                .setMessage("시간 여부를 선택해주세요.")
                .setPositiveButton("확인", null)
                .show();
    }
}
