package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
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
    private static final int MINE_COUNT = 5;
    private static final int FLAG_COUNT = 5;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;
    private ArrayList<Cell> cells;

    private TextView flagView;
    private TextView timeView;
    private TextView flagButton;

    private int flagsNum = FLAG_COUNT;
    private boolean usingFlags = false;
    private int clock = 0;
    private boolean running = false;

    private boolean firstClick = true;

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

        // Switch between flags and pickaxe icon.
        flagButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                usingFlags = !usingFlags;
                if (usingFlags)
                {
                    flagButton.setText(getString(R.string.flag));
                }
                else
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
                //tv.setTextColor(Color.GRAY);
                //tv.setBackgroundColor(Color.GRAY);
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

        //placeMines(MINE_COUNT);
        //findAdjacentMines();
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
                int hours = clock / 3600;
                int minutes = (clock % 3600) / 60;
                int seconds = clock % 60;

                // Show minutes and hours if needed.
                String time;
                if(hours > 0)
                {
                    time = String.format("%d:%02d:%02d", hours, minutes, seconds);
                }
                else if (minutes > 0)
                {
                   time = String.format("%d:%02d", minutes, seconds);
                }
                else
                {
                    time = String.valueOf(seconds);
                }

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

    public void onClickTV(View view) {
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int row = n / COLUMN_COUNT;
        int col = n % COLUMN_COUNT;
        Cell cell = cells.get(n);

        // Mechanics of using flags and selecting cells.
        if(usingFlags)
        {
            if(getString(R.string.flag).contentEquals(tv.getText()))
            {
                tv.setText("");
                flagsNum++;
            }
            else if(flagsNum > 0 && !cell.isRevealed)
            {
                tv.setText(getString(R.string.flag));
                flagsNum--;
            }
            flagView.setText(getString(R.string.flag) + " " + flagsNum);
        }
        else
        {
            if (!running)
            {
                running = true;
            }

            if (firstClick)
            {
                placeMines(MINE_COUNT, n);
                findAdjacentMines();
                firstClick = false;
            }

            // Check to see if this line does anything
            if (cell.isRevealed)
            {
                return;
            }

            if(cell.hasMine)
            {
                tv.setText(getString(R.string.mine));
                tv.setBackgroundColor(Color.RED);
                running = false;
                endGame(false);
                return;
            }

            firstClickAction(row, col);
            if(checkWin())
            {
                endGame(true);
            }

            /*else
            {
                if (cell.adjacementMines > 0)
                {
                    tv.setText(String.valueOf(cell.adjacementMines));
                    tv.setTextColor(Color.GRAY);
                }
                //Check if I need else?
                else
                {
                    tv.setText("");
                }

                tv.setBackgroundColor(Color.LTGRAY);
            }*/
        }
    }

    private void firstClickAction(int row, int col)
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

        if (c.adjacementMines > 0) {
            tv.setText(String.valueOf(c.adjacementMines));
            tv.setTextColor(Color.BLACK);
            return;
        }

        for(int i = -1; i <= 1; i++)
        {
            for(int j = -1; j <= 1; j++)
            {
                if(!(i == 0 && j == 0))
                {
                    firstClickAction(row + i, col + j);
                }
            }
        }
    }

    // Randomly place mines across grid.
    private void placeMines(int n)
    {
        for(Cell c : cells)
        {
            c.hasMine = false;
            c.adjacementMines = 0;
            c.isRevealed = false;
        }

        int placedMines = 0;
        int totalCells = ROW_COUNT * COLUMN_COUNT;

        while (placedMines < MINE_COUNT)
        {
            int index = (int) (Math.random() * totalCells);
            Cell cell = cells.get(index);

            if(index == n)
            {
                continue;
            }

            if (!cell.hasMine)
            {
                cell.hasMine = true;
                placedMines++;
            }
        }
    }

    // Compute adjacent mines.
    private void findAdjacentMines()
    {
        for(int row = 0; row < ROW_COUNT; row++)
        {
            for (int col = 0; col < COLUMN_COUNT; col++)
            {
                int index = row * COLUMN_COUNT + col;
                Cell cell = cells.get(index);

                if (cell.hasMine)
                {
                    cell.adjacementMines = -1;
                    // Check if this line is needed.
                    continue;
                }
                else
                {
                    int count = 0;
                    for (int x = -1; x <= 1; x++)
                    {
                        for (int y = -1; y <= 1; y++)
                        {
                            int adjacentRow = row + x;
                            int adjacentCol = col + y;

                            // Check if it's within the grid.
                            if(inGrid(adjacentRow, adjacentCol))
                            {
                                int adjacentIndex = adjacentRow * COLUMN_COUNT + adjacentCol;
                                if(cells.get(adjacentIndex).hasMine)
                                {
                                    count++;
                                }
                            }
                        }
                    }
                    cell.adjacementMines = count;
                }
            }
        }
    }

    private boolean inGrid(int row, int col)
    {
        return (row >= 0 && row < ROW_COUNT && col >= 0 && col < COLUMN_COUNT);
    }

    private boolean checkWin()
    {
        for(Cell c : cells)
        {
            if(c.hasMine == false && cell.isRevealed == false)
            {
                return false;
            }
        }
        return true;
    }

    private void endGame(boolean won)
    {
        running = false;

        Intent intent = new Intent(MainActivity.this, ResultActivity.class);

        intent.putExtra("time", clock);

        if(hasWon)
        {
            intent.putExtra("result", "Won");
        }
        else
        {
            intent.putExtra("result", "Lost");
        }

        startActivity(intent);

        finish();
    }
}