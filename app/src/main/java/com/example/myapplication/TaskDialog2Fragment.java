package com.example.myapplication;

import android.app.AlertDialog;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TaskDialog2Fragment extends DialogFragment {

    private EditText etTask;
    private RadioGroup radioGroupTime;
    private TimePicker timePicker;
    private RadioButton radioButtonAnytime;
    private RadioButton radioButtonSpecificTime;
    private Button btnRegister;
    private ImageButton closeButton;
    private Button btnDelete;
    private String taskId;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_task_dialog2, null);

        // UI 요소 초기화
        etTask = view.findViewById(R.id.taskInput);
        radioGroupTime = view.findViewById(R.id.radioGroupTime);
        timePicker = view.findViewById(R.id.timePicker);
        radioButtonAnytime = view.findViewById(R.id.radioButtonAnytime);
        radioButtonSpecificTime = view.findViewById(R.id.radioButtonSpecificTime);
        btnRegister = view.findViewById(R.id.btnRegister);
        closeButton = view.findViewById(R.id.closeButton);
        btnDelete = view.findViewById(R.id.btnDelete);

        // 전달받은 Task 정보 설정
        Bundle args = getArguments();
        if (args != null) {
            String task = args.getString("task", "");
            String time = args.getString("time", "Anytime");
            taskId = args.getString("taskId", null); // Task ID를 String으로 받아오기

            etTask.setText(task);

            if ("Anytime".equals(time)) {
                radioButtonAnytime.setChecked(true);
                timePicker.setVisibility(View.GONE);
            } else {
                radioButtonSpecificTime.setChecked(true);
                timePicker.setVisibility(View.VISIBLE);

                String[] timeParts = time.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            }
        }

        // RadioGroup 체크 변경 리스너
        radioGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonAnytime) {
                timePicker.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioButtonSpecificTime) {
                timePicker.setVisibility(View.VISIBLE);
            }
        });

        // TimePicker 초기화
        timePicker.setIs24HourView(true);

        // Register 버튼 클릭 이벤트
        btnRegister.setOnClickListener(v -> {
            String task = etTask.getText().toString().trim();
            if (!validateInput(task)) {
                return; // 태스크가 유효하지 않으면 등록을 중지
            }

            String time = radioButtonSpecificTime.isChecked() ?
                    String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute()) :
                    "Anytime";

            Bundle result = new Bundle();
            result.putString("task", task);
            result.putString("time", time);
            result.putString("taskId", taskId); // Task ID를 String으로 전달
            result.putBoolean("isDeleted", false);
            getParentFragmentManager().setFragmentResult("popupResult", result);
            dismiss();
        });

        // 삭제 버튼 클릭 이벤트
        btnDelete.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString("taskId", taskId); // Task ID를 String으로 전달
            result.putBoolean("isDeleted", true);
            getParentFragmentManager().setFragmentResult("popupResult", result);
            dismiss();
        });

        // Close 버튼 클릭 이벤트
        closeButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
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
}
