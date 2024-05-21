package com.example.verifit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class AddExerciseActivity extends AppCompatActivity {

    // Helper Data Structures
    public RecyclerView recyclerView;
    public String exercise_name;
    public static ArrayList<WorkoutSet> Todays_Exercise_Sets = new ArrayList<WorkoutSet>();
    public AddExerciseWorkoutSetAdapter workoutSetAdapter2;
    public static int Clicked_Set = 0;

    // Add Exercise Activity Specifics
    public static EditText et_reps;
    public static EditText et_weight;
    public ImageButton plus_reps;
    public ImageButton minus_reps;
    public ImageButton plus_weight;
    public ImageButton minus_weight;
    public Button bt_save;
    public Button bt_clear;

    // For Alarm
    public long START_TIME_IN_MILLIS = 180000;
    public CountDownTimer countDownTimer;
    public boolean TimerRunning;
    public long TimeLeftInMillis = START_TIME_IN_MILLIS;

    // Timer Dialog Components
    public EditText et_seconds;
    public ImageButton minus_seconds;
    public ImageButton plus_seconds;
    public Button bt_start;
    public Button bt_reset;


    // Comment Items
    Button bt_save_comment;
    Button bt_clear_comment;
    EditText et_exercise_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        // find views
        et_reps = findViewById(R.id.et_reps);
        et_weight = findViewById(R.id.et_seconds);
        plus_reps = findViewById(R.id.plus_reps);
        minus_reps = findViewById(R.id.minus_reps);
        plus_weight = findViewById(R.id.plus_weight);
        minus_weight = findViewById(R.id.minus_weight);
        bt_clear = findViewById(R.id.bt_clear);
        bt_save = findViewById(R.id.bt_save);

        // Self Explanatory I guess
        initActivity();

        // Self Explanatory I guess
        initrecyclerView();

        System.out.println("date_selected: " + MainActivity.date_selected);
    }

    // Button On Click Methods
    public void clickSave(View view)
    {
        if(et_weight.getText().toString().isEmpty() || et_reps.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Vui lòng viết Weight và Reps",Toast.LENGTH_SHORT).show();
        }
        else
        {
            // Get user sets && reps
            Double reps = Double.parseDouble(et_reps.getText().toString());
            Double weight = Double.parseDouble(et_weight.getText().toString());

            // Create New Set Object
            WorkoutSet workoutSet = new WorkoutSet(MainActivity.date_selected,exercise_name, MainActivity.getExerciseCategory(exercise_name),reps,weight);

            // Ignore wrong input
            if(reps == 0 || weight == 0 || reps < 0 || weight < 0)
            {
                Toast.makeText(getApplicationContext(),"Vui lòng viết trọng lượng và đại diện chính xác",Toast.LENGTH_SHORT).show();
            }
            // Save set
            else
            {
                // Find if workout day already exists
                int position = MainActivity.getDayPosition(MainActivity.date_selected);

                // If workout day exists
                if(position >= 0)
                {
                    MainActivity.Workout_Days.get(position).addSet(workoutSet);
                }
                // If not construct new workout day
                else
                {
                    WorkoutDay workoutDay = new WorkoutDay();
                    workoutDay.addSet(workoutSet);
                    MainActivity.Workout_Days.add(workoutDay);
                }

                // Update Local Data Structure
                updateTodaysExercises();
                Toast.makeText(getApplicationContext(),"Đã lưu ",Toast.LENGTH_SHORT).show();
            }
        }

        // Fixed Myria induced bug
        AddExerciseActivity.Clicked_Set = Todays_Exercise_Sets.size()-1;
    }

    // Clear / Delete
    public void clickClear(View view)
    {
        // Clear Function
        if(Todays_Exercise_Sets.isEmpty())
        {
            bt_clear.setText("Đã Clear");
            et_reps.setText("");
            et_weight.setText("");
        }

        // Delete Function
        else
        {
            // Show confirmation dialog  box
            // Prepare to show exercise dialog box
            LayoutInflater inflater = LayoutInflater.from(this);
            View view1 = inflater.inflate(R.layout.delete_set_dialog,null);
            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();

            Button bt_yes = view1.findViewById(R.id.bt_yes3);
            Button bt_no = view1.findViewById(R.id.bt_no3);

            // Dismiss dialog box
            bt_no.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    alertDialog.dismiss();
                }
            });

            // Actually Delete set and update local data structure
            bt_yes.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    // Get soon to be deleted set
                    WorkoutSet to_be_removed_set = Todays_Exercise_Sets.get(Clicked_Set);

                    // Find the set in main data structure and delete it
                    for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
                    {
                        if(MainActivity.Workout_Days.get(i).getSets().contains(to_be_removed_set))
                        {
                            // If last set the delete the whole object
                            if(MainActivity.Workout_Days.get(i).getSets().size() == 1)
                            {
                                MainActivity.Workout_Days.remove(MainActivity.Workout_Days.get(i));
                            }
                            // Just delete the set
                            else
                            {
                                MainActivity.Workout_Days.get(i).removeSet(to_be_removed_set);
                                break;
                            }

                        }
                    }

                    // Let the user know I guess
                    Toast.makeText(getApplicationContext(),"Đặt xóa",Toast.LENGTH_SHORT).show();

                    // Update Local Data Structure
                    updateTodaysExercises();

                    alertDialog.dismiss();

                    // Update Clicked set to avoid crash
                    AddExerciseActivity.Clicked_Set = Todays_Exercise_Sets.size()-1;
                }
            });

            // Show delete confirmation dialog box
            alertDialog.show();
        }
    }

    // Update this activity when a set is clicked
    public static void UpdateViewOnClick()
    {
        // Get selected set
        WorkoutSet clicked_set = Todays_Exercise_Sets.get(AddExerciseActivity.Clicked_Set);

        // Update Edit Texts
        et_weight.setText(clicked_set.getWeight().toString());
        et_reps.setText(String.valueOf(clicked_set.getReps().intValue()));


    }

    // Save Changes in main data structure, save data structure in shared preferences
    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("On Stop1");

        // Sort Before Saving
        MainActivity.sortWorkoutDaysDate();

        System.out.println("On Stop2");

        // Actually Save Changes in shared preferences
        MainActivity.saveWorkoutData(getApplicationContext());

        System.out.println("On Stop3");
    }

    // Do I even need to explain this?
    public void clickPlusWeight(View view)
    {
        if(!et_weight.getText().toString().isEmpty())
        {
            Double weight = Double.parseDouble(et_weight.getText().toString());
            weight = weight + 1;
            et_weight.setText(weight.toString());
        }
        else
        {
            et_weight.setText("1.0");
        }

    }

    // Do I even need to explain this?
    public void clickPlusReps(View view)
    {
        if(!et_reps.getText().toString().isEmpty())
        {
            int reps = Integer.parseInt(et_reps.getText().toString());
            reps = reps + 1;
            et_reps.setText(String.valueOf(reps));
        }
        else
        {
            et_reps.setText("1");
        }

    }

    // Do I even need to explain this?
    public void clickMinusWeight(View view)
    {
        if(!et_weight.getText().toString().isEmpty())
        {
            Double weight = Double.parseDouble(et_weight.getText().toString());
            weight = weight - 1;
            if(weight < 0)
            {
                weight = 0.0;
            }
            et_weight.setText(weight.toString());
        }
    }

    // Do I even need to explain this?
    public void clickMinusReps(View view)
    {
        if(!et_reps.getText().toString().isEmpty())
        {
            int reps = Integer.parseInt(et_reps.getText().toString());
            reps = reps - 1;
            if(reps < 0)
            {
                reps = 0;
            }
            et_reps.setText(String.valueOf(reps));
        }

    }

    // Handles Intent Stuff
    public void initActivity()
    {
        Intent in = getIntent();
        exercise_name = in.getStringExtra("exercise");
        getSupportActionBar().setTitle(exercise_name);
    }

    // Updates Local Data Structure
    public void updateTodaysExercises()
    {
        // Clear since we don't want duplicates
        Todays_Exercise_Sets.clear();

        // Find Sets for a specific date and exercise
        for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
        {
            // If date matches
            if(MainActivity.Workout_Days.get(i).getDate().equals(MainActivity.date_selected))
            {
                for(int j  = 0; j < MainActivity.Workout_Days.get(i).getSets().size(); j++)
                {
                    // If exercise matches
                    if(exercise_name.equals(MainActivity.Workout_Days.get(i).getSets().get(j).getExercise()))
                    {
                        Todays_Exercise_Sets.add(MainActivity.Workout_Days.get(i).getSets().get(j));
                    }
                }
            }
        }

        // Change Button Functionality
        if(Todays_Exercise_Sets.isEmpty())
        {
            bt_clear.setText("Clear");
        }
        else
        {
            bt_clear.setText("Delete");
        }

        // Update Recycler View
        workoutSetAdapter2.notifyDataSetChanged();

    }

    // Initialize Recycler View Object
    public void initrecyclerView()
    {
        // Clear since we don't want duplicates
        Todays_Exercise_Sets.clear();

        // Find Sets for a specific date and exercise
        for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
        {
            // If date matches
            if(MainActivity.Workout_Days.get(i).getDate().equals(MainActivity.date_selected))
            {
                for(int j  = 0; j < MainActivity.Workout_Days.get(i).getSets().size(); j++)
                {
                    // If exercise matches
                    if(exercise_name.equals(MainActivity.Workout_Days.get(i).getSets().get(j).getExercise()))
                    {
                        Todays_Exercise_Sets.add(MainActivity.Workout_Days.get(i).getSets().get(j));
                    }
                }
            }
        }

        // Find Recycler View Object
        recyclerView = findViewById(R.id.recycler_view);
        workoutSetAdapter2 = new AddExerciseWorkoutSetAdapter(this,Todays_Exercise_Sets);
        recyclerView.setAdapter(workoutSetAdapter2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Set Edit Text values to max set volume if possible
        initEditTexts();


        // Change Button Functionality
        if(Todays_Exercise_Sets.isEmpty())
        {
            bt_clear.setText("Clear");
        }
        else
        {
            bt_clear.setText("Delete");
        }

        // Initialize Integer position or else we get a crash
        AddExerciseActivity.Clicked_Set = Todays_Exercise_Sets.size() - 1;

    }

    // Set Edit Text values to max set volume if sets exist
    public void initEditTexts()
    {
        Double max_weight = 0.0;
        int max_reps = 0;
        Double max_exercise_volume = 0.0;

        // Find Max Weight and Reps for a specific exercise
        for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
        {
            for(int j = 0; j < MainActivity.Workout_Days.get(i).getSets().size(); j++)
            {
                if(MainActivity.Workout_Days.get(i).getSets().get(j).getVolume() > max_exercise_volume && MainActivity.Workout_Days.get(i).getSets().get(j).getExercise().equals(exercise_name))
                {
                    max_exercise_volume = MainActivity.Workout_Days.get(i).getSets().get(j).getVolume();
                    max_reps = (int)Math.round(MainActivity.Workout_Days.get(i).getSets().get(j).getReps());
                    max_weight = MainActivity.Workout_Days.get(i).getSets().get(j).getWeight();
                }
            }
        }

        // If never performed the exercise leave Edit Texts blank
        if(max_reps == 0 || max_weight == 0.0)
        {
            et_reps.setText("");
            et_weight.setText("");
        }else
        {
            et_reps.setText(String.valueOf(max_reps));
            et_weight.setText(max_weight.toString());
        }
    }

    // Menu Stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_exercise_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // Timer
        if(item.getItemId() == R.id.timer)
        {
            // Prepare to show timer dialog box
            LayoutInflater inflater = LayoutInflater.from(AddExerciseActivity.this);
            View view = inflater.inflate(R.layout.timer_dialog,null);
            AlertDialog alertDialog = new AlertDialog.Builder(AddExerciseActivity.this).setView(view).create();

            // Get Objects (use view because dialog box from menu)
            et_seconds = view.findViewById(R.id.et_seconds);
            minus_seconds = view.findViewById(R.id.minus_seconds);
            plus_seconds = view.findViewById(R.id.plus_seconds);
            bt_start = view.findViewById(R.id.bt_start);
            bt_reset = view.findViewById(R.id.bt_close);

            // Set default seconds value to 180 i.e 3 minutes
            if(!TimerRunning)
            {
                // Derive String value from chosen start time
                // et_seconds.setText(String.valueOf((int) START_TIME_IN_MILLIS /1000));
                loadSeconds();
            }
            else
            {
                updateCountDownText();
            }

            // Reset Timer Button
            bt_reset.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    resetTimer();
                }
            });

            // Start Timer Button
            bt_start.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(TimerRunning)
                    {
                        pauseTimer();
                    }
                    else
                    {
                        saveSeconds();
                        startTimer();
                    }

                }
            });

            // Minus Button
            minus_seconds.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!et_seconds.getText().toString().isEmpty())
                    {
                        Double seconds  = Double.parseDouble(et_seconds.getText().toString());
                        seconds = seconds - 1;
                        if(seconds < 0)
                        {
                            seconds = 0.0;
                        }
                        int seconds_int = seconds.intValue();
                        et_seconds.setText(String.valueOf(seconds_int));
                    }
                }
            });

            // Plus Button
            plus_seconds.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!et_seconds.getText().toString().isEmpty())
                    {
                        Double seconds  = Double.parseDouble(et_seconds.getText().toString());
                        seconds = seconds + 1;
                        if(seconds < 0)
                        {
                            seconds = 0.0;
                        }
                        int seconds_int = seconds.intValue();
                        et_seconds.setText(String.valueOf(seconds_int));
                    }
                }
            });

            // Show Timer Dialog Box
            alertDialog.show();
        }

        // Exercise History
        else if(item.getItemId() == R.id.history)
        {
            // Prepare to show exercise history dialog box
            LayoutInflater inflater = LayoutInflater.from(AddExerciseActivity.this);
            View view = inflater.inflate(R.layout.exercise_history_dialog,null);
            AlertDialog alertDialog = new AlertDialog.Builder(AddExerciseActivity.this).setView(view).create();


            // Declare local data structure
            ArrayList<WorkoutExercise> All_Performed_Sessions = new ArrayList<>();

            // Find all performed sessions of a specific exercise and add them to local data structure
            for(int i = MainActivity.Workout_Days.size()-1; i >= 0; i--)
            {
                for(int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
                {
                    if(MainActivity.Workout_Days.get(i).getExercises().get(j).getExercise().equals(exercise_name))
                    {
                        All_Performed_Sessions.add(MainActivity.Workout_Days.get(i).getExercises().get(j));
                    }
                }
            }


            // Set Exercise Name
            TextView tv_exercise_name = view.findViewById(R.id.tv_exercise_name);
            tv_exercise_name.setText(exercise_name);


            // Set Exercise History Recycler View
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView_Exercise_History);
            ExerciseHistoryExerciseAdapter workoutExerciseAdapter4 = new ExerciseHistoryExerciseAdapter(AddExerciseActivity.this,All_Performed_Sessions);


            // Crash Here
            recyclerView.setAdapter(workoutExerciseAdapter4);
            recyclerView.setLayoutManager(new LinearLayoutManager(AddExerciseActivity.this));


            alertDialog.show();
        }

        // Exercise Stats Chart
        else if(item.getItemId() == R.id.graph)
        {
            // Prepare to show exercise history dialog box
            LayoutInflater inflater = LayoutInflater.from(AddExerciseActivity.this);
            View view = inflater.inflate(R.layout.exercise_graph_dialog,null);
            AlertDialog alertDialog = new AlertDialog.Builder(AddExerciseActivity.this).setView(view).create();


            // Get Chart Object
            LineChart lineChart = (LineChart) view.findViewById(R.id.lineChart);

            // Create Array List that will hold graph data
            ArrayList<Entry> Volume_Values = new ArrayList<>();

            int x = 0;

            // Get Exercise Volume
            for(int i = 0; i < MainActivity.Workout_Days.size(); i++)
            {
                for (int j = 0; j < MainActivity.Workout_Days.get(i).getExercises().size(); j++)
                {
                    WorkoutExercise current_exercise = MainActivity.Workout_Days.get(i).getExercises().get(j);

                    if(current_exercise.getExercise().equals(exercise_name))
                    {
                        Volume_Values.add(new Entry(x,current_exercise.getVolume().floatValue()));
                        x++;
                    }
                }
            }

            LineDataSet volumeSet = new LineDataSet(Volume_Values,"Volume");
            LineData data = new LineData(volumeSet);


            volumeSet.setLineWidth(2f);
            volumeSet.setValueTextSize(10f);
            volumeSet.setValueTextColor(Color.BLACK);

            lineChart.setData(data);
            lineChart.getDescription().setEnabled(false);


            // Show Chart Dialog box
            alertDialog.show();


        }

        // Exercise Comments
        else if(item.getItemId() == R.id.comment)
        {
            // Prepare to show exercise history dialog box
            LayoutInflater inflater = LayoutInflater.from(AddExerciseActivity.this);
            View view = inflater.inflate(R.layout.add_exercise_comment_dialog,null);
            AlertDialog alertDialog = new AlertDialog.Builder(AddExerciseActivity.this).setView(view).create();


            bt_save_comment = view.findViewById(R.id.bt_save_comment);
            bt_clear_comment = view.findViewById(R.id.bt_clear_comment);
            et_exercise_comment = view.findViewById(R.id.et_exercise_comment);

            // Check if exercise exists (to show the comment if it has one)
            // Find if workout day already exists
            int exercise_position = MainActivity.getExercisePosition(MainActivity.date_selected,exercise_name);

            // Exists, then show the comment
            if(exercise_position >= 0)
            {
                System.out.println("We can comment, exercise exists");

                int day_position = MainActivity.getDayPosition(MainActivity.date_selected);

                String comment = MainActivity.Workout_Days.get(day_position).getExercises().get(exercise_position).getComment();

                et_exercise_comment.setText(comment);
            }



            bt_clear_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    clearComment();
                }
            });

            bt_save_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveComment();
                }
            });

            // Show Chart Dialog box
            alertDialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    // Makes necesary checks and saves comment
    public void saveComment()
    {
        // Check for empty input
        if(et_exercise_comment.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Hãy viết một bình luận",Toast.LENGTH_SHORT).show();
            return ;
        }

        // Check if exercise exists (cannot comment on non-existant exercise)
        // Find if workout day already exists
        int exercise_position = MainActivity.getExercisePosition(MainActivity.date_selected,exercise_name);

        if(exercise_position >= 0)
        {
            System.out.println("Chúng tôi có thể nhận xét, tập thể dục tồn tại");
        }
        else
        {
            System.out.println("Chúng tôi không thể bình luận, bài tập không tồn tại");
            Toast.makeText(getApplicationContext(),"Không thể bình luận nếu không có bộ",Toast.LENGTH_SHORT).show();
            return;
        }



        // Get user comment
        String comment = et_exercise_comment.getText().toString();

        // Print it for sanity check
        System.out.println(comment);

        // Get the date for today
        int day_position = MainActivity.getDayPosition(MainActivity.date_selected);

        // Modify the data structure to add the comment
        MainActivity.Workout_Days.get(day_position).getExercises().get(exercise_position).setComment(comment);


        Toast.makeText(getApplicationContext(),"Đã ghi nhận xét",Toast.LENGTH_SHORT).show();

    }

    // Makes necesary checks and clears comment
    public void clearComment()
    {
        et_exercise_comment.setText("");

        // Check if exercise exists (cannot comment on non-existant exercise)
        // Find if workout day already exists
        int exercise_position = MainActivity.getExercisePosition(MainActivity.date_selected,exercise_name);

        if(exercise_position >= 0)
        {
            System.out.println("Chúng tôi có thể nhận xét, tập thể dục tồn tại");
        }
        else
        {
            System.out.println("Chúng tôi không thể bình luận, bài tập không tồn tại");
            Toast.makeText(getApplicationContext(),"Không thể bình luận nếu không có bộ",Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user comment
        String comment = et_exercise_comment.getText().toString();

        // Print it for sanity check
        System.out.println(comment);

        // Get the date for today
        int day_position = MainActivity.getDayPosition(MainActivity.date_selected);

        // Modify the data structure to add the comment
        MainActivity.Workout_Days.get(day_position).getExercises().get(exercise_position).setComment(comment);


        Toast.makeText(getApplicationContext(),"Nhận xét đã Xóa",Toast.LENGTH_SHORT).show();
    }

    public void startTimer()
    {
        countDownTimer = new CountDownTimer(TimeLeftInMillis, 1000)
        {
            @Override
            public void onTick(long MillisUntilFinish)
            {
                TimeLeftInMillis = MillisUntilFinish;
                updateCountDownText();
            }

            @Override
            public void onFinish()
            {
                TimerRunning = false;
                bt_start.setText("Start");
            }
        }.start();

        TimerRunning = true;
        bt_start.setText("Pause");

    }

    public void loadSeconds()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        String seconds = sharedPreferences.getString("seconds","180");

        // Change actual values that timer uses
        START_TIME_IN_MILLIS = Integer.parseInt(seconds) * 1000;
        TimeLeftInMillis = START_TIME_IN_MILLIS;

        et_seconds.setText(seconds);
    }

    public void saveSeconds()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(!et_seconds.getText().toString().isEmpty())
        {
            String seconds = et_seconds.getText().toString();

            // Change actual values that timer uses
            START_TIME_IN_MILLIS = Integer.parseInt(seconds) * 1000;
            TimeLeftInMillis = START_TIME_IN_MILLIS;

            // Save to shared preferences
            editor.putString("seconds",et_seconds.getText().toString());
            editor.apply();
        }
    }

    public void pauseTimer()
    {
        countDownTimer.cancel();
        TimerRunning = false;
        bt_start.setText("Start");
    }

    public void resetTimer()
    {
        if(TimerRunning)
        {
            pauseTimer();
            TimeLeftInMillis = START_TIME_IN_MILLIS;
            updateCountDownText();
        }

    }

    public void updateCountDownText()
    {
        int seconds = (int) TimeLeftInMillis / 1000;
        int minutes = (int) seconds / 60;
        et_seconds.setText(String.valueOf(seconds));
    }

}