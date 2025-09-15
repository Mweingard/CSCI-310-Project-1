package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cell_tvs = new ArrayList<>();
        cells = new ArrayList<>();

        // Method (2): add four dynamically created cells
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
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
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

        placeMines(5);
        findAdjacentMines();
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
        int i = n / COLUMN_COUNT;
        int j = n % COLUMN_COUNT;

        Cell cell = cells.get(n);

        tv.setText(String.valueOf(i) + String.valueOf(j));

        if(!cell.isRevealed) {
            cell.isRevealed = true;
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.parseColor("lime"));
        } else {
            cell.isRevealed = false;
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }

        /*if (tv.getCurrentTextColor() == Color.GRAY) {
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.parseColor("lime"));
        }else {
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }*/

    }

    // Randomly place mines across grid.
    private void placeMines(int count)
    {
        int placedMines = 0;
        int totalCells = ROW_COUNT * COLUMN_COUNT;

        while (placedMines < count) {
            int index = (int) (Math.random() * totalCells);
            Cell cell = cells.get(index);

            if (!cell.hasMine) {
                cell.hasMine = true;
                placedMines++;
            }
        }
    }

    // Compute adjacent mines.
    private void findAdjacentMines()
    {
        for(int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                int index = row * COLUMN_COUNT + col;
                Cell cell = cells.get(index);

                if (cell.hasMine)
                {
                    cell.adjacementMines = -1;
                }

                int count = 0;
                for (int x = -1; x <= 1; x++)
                {
                    for (int y = -1; y <= 1; y++)
                    {
                        int adjacentRow = row + x;
                        int adjacentCol = col + y;

                        // Check if it's within the grid.
                        if(inGrid(adjacentRow, adjacentCol)) {
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

    private boolean inGrid(int row, int col)
    {
        return (row >= 0 && row < ROW_COUNT && col >= 0 && col < COLUMN_COUNT);
    }
}