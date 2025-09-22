package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 10;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private ArrayList<Cell> cells;

    private TextView flagView;
    private TextView timeView;
    private TextView flagButton;

    private int flagsNum = 5;
    private boolean gameMode = false;
    private int clock = 0;
    private boolean running = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flagView = findViewById(R.id.textViewFlag);
        timeView = findViewById(R.id.textViewTime);
        flagButton = findViewById(R.id.flagButton);

        flagView.setText(getString(R.string.flag) + " " + flagsNum);
        timeView.setText(getString(R.string.clock) + " 0");

        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }

        runTimer();

        // Switch between flags and pickaxe.
        flagButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Actually switches between flag and pick.
                gameMode = !gameMode;
                if (gameMode) // Flag
                {
                    flagButton.setText(getString(R.string.flag));
                }
                else // Pickaxe.
                {
                    flagButton.setText(getString(R.string.pick));
                }
            }
        });

        // Create 10 x 10 grid.
        cell_tvs = new ArrayList<>();
        cells = new ArrayList<>();

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        grid.setRowCount(ROW_COUNT);
        grid.setColumnCount(COLUMN_COUNT);

        for (int i = 0; i < ROW_COUNT; i++) {
            for (int j = 0; j < COLUMN_COUNT; j++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(32));
                tv.setWidth(dpToPixel(32));
                tv.setTextSize(18);
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.parseColor("lime"));
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);

                Cell c = new Cell();
                cells.add(c);
            }
        }
        // Randomly place mines.
        placeMines();

        // Calculate adjacent mines.
        findAdjacentMines();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String time = String.valueOf(clock);
                timeView.setText(getString(R.string.clock) + " " + time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n = 0; n < cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    // Mechanics of using flags and selecting cells.
    public void onClickTV(View view) {
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int row = n / COLUMN_COUNT;
        int col = n % COLUMN_COUNT;
        Cell c = cells.get(n);

        if(gameMode) // Flag
        {
            if(getString(R.string.flag).contentEquals(tv.getText()))
            {
                tv.setText("");
                flagsNum++;
            }
            // Allows user to user more than 5 flags.
            else if(!c.isRevealed)
            {
                tv.setText(getString(R.string.flag));
                flagsNum--;
            }
            flagView.setText(getString(R.string.flag) + " " + flagsNum);
        }
        // Pickaxe cases.
        else
        {
            // Starts timer.
            if (!running)
            {
                running = true;
            }

            // Doesn't allow user to click flagged cells.
            if (getString(R.string.flag).contentEquals(tv.getText()))
            {
                return;
            }

            // Doesn't allow user to click revealed cells.
            if (c.isRevealed)
            {
                return;
            }

            if(c.hasMine) {
                tv.setText(getString(R.string.mine));
                tv.setBackgroundColor(Color.RED);
                gameResult(false);
                return;
            }

            // Classic first click function in Minesweeper.
            firstClick(row, col);

            if(checkPlayerResult())
            {
                gameResult(true);
            }
        }
    }

    private void firstClick(int row, int col)
    {
        if(!inGrid(row, col))
        {
            return;
        }

        int index = row * COLUMN_COUNT + col;
        Cell c = cells.get(index);
        TextView tv = cell_tvs.get(index);

        if(c.isRevealed || c.hasMine)
        {
            return;
        }

        c.isRevealed = true;
        tv.setBackgroundColor(Color.LTGRAY);

        if (c.adjacementMines > 0)
        {
            tv.setText(String.valueOf(c.adjacementMines));
            tv.setTextColor(Color.GRAY);
            return;
        }

        for(int i = -1; i <= 1; i++)
        {
            for(int j = -1; j <= 1; j++)
            {
                if(i != 0 || j != 0)
                {
                    firstClick(row + i, col + j);
                }
            }
        }
    }

    // Randomly place mines across grid.
    private void placeMines()
    {
        for(int i = 0; i < cells.size(); i++)
        {
            Cell c = cells.get(i);
            c.hasMine = false;
            c.adjacementMines = 0;
            c.isRevealed = false;
        }

        int placedMines = 0;
        int totalCells = ROW_COUNT * COLUMN_COUNT;

        while (placedMines < 5)
        {
            int index = (int) (Math.random() * totalCells);
            Cell c = cells.get(index);

            if (!c.hasMine)
            {
                c.hasMine = true;
                placedMines++;
            }
        }
    }

    // Calculate adjacent mines.
    private void findAdjacentMines()
    {
        for(int row = 0; row < ROW_COUNT; row++)
        {
            for (int col = 0; col < COLUMN_COUNT; col++)
            {
                int index = row * COLUMN_COUNT + col;
                Cell c = cells.get(index);

                if (c.hasMine)
                {
                    c.adjacementMines = -1;
                }
                else
                {
                    int minesNum = 0;
                    for (int i = -1; i <= 1; i++)
                    {
                        for (int j = -1; j <= 1; j++)
                        {
                            int adjacentRow = row + i;
                            int adjacentCol = col + j;

                            // Check if it's within the grid.
                            if(inGrid(adjacentRow, adjacentCol))
                            {
                                int adjacentIndex = adjacentRow * COLUMN_COUNT + adjacentCol;
                                Cell adjacentC = cells.get(adjacentIndex);

                                if(adjacentC.hasMine)
                                {
                                    minesNum++;
                                }
                            }
                        }
                    }
                    c.adjacementMines = minesNum;
                }
            }
        }
    }

    private boolean inGrid(int row, int col)
    {
        return (row >= 0 && row < ROW_COUNT && col >= 0 && col < COLUMN_COUNT);
    }

    private boolean checkPlayerResult() {
        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if (!c.hasMine && !c.isRevealed)
            {
                return false;
            }
        }
        return true;
    }
    private void gameResult(boolean playerResult)
    {
        // Stop clock.
        running = false;

        // Reveal all cells.
        for(int i = 0; i < cells.size(); i++)
        {
            Cell c = cells.get(i);
            TextView tv = cell_tvs.get(i);

            if(c.hasMine)
            {
                tv.setText(getString(R.string.mine));
                tv.setBackgroundColor(Color.RED);
            }
            else
            {
                if (c.adjacementMines > 0)
                {
                    tv.setText(String.valueOf(c.adjacementMines));
                    tv.setTextColor(Color.GRAY);
                }
                else
                {
                    tv.setText("");
                }
            }
        }
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("time", clock);

        if(playerResult)
        {
            intent.putExtra("result", "won");
        }
        else
        {
            intent.putExtra("result", "lost");
        }
        startActivity(intent);
        finish();
    }
}
