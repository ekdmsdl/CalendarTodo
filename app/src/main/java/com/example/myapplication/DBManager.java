package com.example.myapplication;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    private final List<Task> taskList = new ArrayList<>();
    private static final String TAG = "DBManager";
    private final DatabaseReference myRef;

    // 날짜 및 시간 형식 지정
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    // 선택된 날짜 저장
    private LocalDate selectedDate;

    // 생성자에서 Firebase Database 참조 초기화
    public DBManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("tasks");
    }

    // 데이터를 Firebase Database에 저장하는 메소드
    public void saveTask(String taskName, LocalTime time, boolean hasSpecificTime, LocalDate date) {
        DatabaseReference newTaskRef = myRef.push(); // 고유한 ID를 생성
        String id = newTaskRef.getKey(); // 생성된 ID를 가져옴

        if (id == null) {
            Log.e(TAG, "Failed to generate a unique ID for the new task.");
            return;
        }

        // 번호를 계산하기 위해 현재 목록의 최대 번호를 찾는다.
        int taskNumber = calculateTaskNumber();

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", id);
        taskMap.put("task", taskName);
        taskMap.put("time", time != null ? time.format(TIME_FORMATTER) : null);
        taskMap.put("hasSpecificTime", hasSpecificTime);
        taskMap.put("number", taskNumber); // 목록 번호를 적절히 계산하여 설정
        taskMap.put("date", date.format(DATE_FORMATTER));

        newTaskRef.setValue(taskMap).addOnCompleteListener(databasetask -> {
            if (databasetask.isSuccessful()) {
                Log.d(TAG, "Task saved successfully. Task ID: " + id);
            } else {
                Log.e(TAG, "Failed to save task.", databasetask.getException());
            }
        });
    }

    // Firebase Database에서 모든 데이터를 불러오는 메소드
    public void loadTasks() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String id = snapshot.child("id").getValue(String.class);
                        String taskName = snapshot.child("task").getValue(String.class);
                        String timeString = snapshot.child("time").getValue(String.class);
                        LocalTime time = timeString != null ? LocalTime.parse(timeString, TIME_FORMATTER) : null;
                        Boolean hasSpecificTimeValue = snapshot.child("hasSpecificTime").getValue(Boolean.class);
                        boolean hasSpecificTime = (hasSpecificTimeValue != null) ? hasSpecificTimeValue : (time != null);

                        // number 필드 변환 처리
                        Object numberObject = snapshot.child("number").getValue();
                        Integer number = null;
                        if (numberObject instanceof String) {
                            try {
                                number = Integer.parseInt((String) numberObject);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing number field from String: " + numberObject, e);
                            }
                        } else if (numberObject instanceof Number) {
                            number = ((Number) numberObject).intValue();
                        } else {
                            Log.e(TAG, "Unexpected type for number field: " + (numberObject != null ? numberObject.getClass().getName() : "null"));
                        }

                        String dateString = snapshot.child("date").getValue(String.class);
                        LocalDate date = dateString != null ? LocalDate.parse(dateString, DATE_FORMATTER) : null;

                        if (id == null || taskName == null || date == null || number == null) {
                            Log.e(TAG, "Error: Missing required task data. ID: " + id + ", Task: " + taskName + ", Date: " + dateString);
                            continue;
                        }

                        Task task = new Task(id, taskName, time, hasSpecificTime, number, date);
                        taskList.add(task);
                        Log.d(TAG, "Task loaded: " + task.getTask() + " on " + task.getDate());
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing task data: ", e);
                    }
                }

                if (taskListObserver != null) {
                    taskListObserver.onTasksLoaded(taskList);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load tasks.", error.toException());
            }
        });
    }

    // TaskListObserver 인터페이스 정의
    public interface TaskListObserver {
        void onTasksLoaded(List<Task> newTasks);
    }

    // DBManager에 TaskListObserver를 설정하는 메서드 추가
    private TaskListObserver taskListObserver;

    public void setTaskListObserver(TaskListObserver observer) {
        this.taskListObserver = observer;
    }

    // 선택된 날짜를 설정하는 메서드
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        // 날짜가 변경되면 RecyclerView를 업데이트
        if (taskListObserver != null) {
            taskListObserver.onTasksLoaded(getTasksForDate(date));
        }
    }

    // 작업 업데이트 메서드
    public void updateTask(String id, Task updatedTask) {
        if (id == null || id.isEmpty()) {
            Log.e(TAG, "Cannot update task: Task ID is null or empty.");
            return;
        }

        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("task", updatedTask.getTask());
        taskMap.put("time", updatedTask.getTime() != null ? updatedTask.getTime().format(TIME_FORMATTER) : null);
        taskMap.put("hasSpecificTime", updatedTask.getTime() != null); // 시간 설정 여부에 따라 hasSpecificTime 설정
        taskMap.put("number", updatedTask.getNumber());
        taskMap.put("date", updatedTask.getDate().format(DATE_FORMATTER));

        myRef.child(id).updateChildren(taskMap)
                .addOnCompleteListener(databasetask -> {
                    if (databasetask.isSuccessful()) {
                        Log.d(TAG, "Task updated successfully.");
                    } else {
                        Log.e(TAG, "Failed to update task.", databasetask.getException());
                    }
                });
    }

    // 작업 삭제 메서드
    public void deleteTask(String id) {
        myRef.child(id).removeValue()
                .addOnCompleteListener(databasetask -> {
                    if (databasetask.isSuccessful()) {
                        Log.d(TAG, "Task deleted successfully. Task ID: " + id);
                    } else {
                        Log.e(TAG, "Failed to delete task. Task ID: " + id, databasetask.getException());
                    }
                });
    }

    // 선택된 날짜에 대한 작업 목록 가져오기
    public List<Task> getTasksForDate(LocalDate date) {
        List<Task> tasksForDate = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getDate().equals(date)) {
                tasksForDate.add(task);
            }
        }
        return tasksForDate;
    }

    // 현재 작업 목록에서 새로운 작업 번호를 계산하는 메소드
    private int calculateTaskNumber() {
        if (taskList.isEmpty()) {
            return 1;
        } else {
            // 현재 목록에서 가장 높은 번호를 찾고 그 다음 번호를 반환
            int maxNumber = taskList.stream()
                    .mapToInt(Task::getNumber)
                    .max()
                    .orElse(0);
            return maxNumber + 1;
        }
    }
}
