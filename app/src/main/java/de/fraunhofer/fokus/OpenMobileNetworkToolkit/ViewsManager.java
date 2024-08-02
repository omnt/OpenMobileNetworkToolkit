package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.Arrays;

public class ViewsManager {
    private final Context context;
    private final View[][] views;

    public ViewsManager(Context context) {
        this.context = context;
        this.views = new View[10][10];
    }

    public View[][] getViewsMap() {
        return views;
    }

    public void addNewView(View view) {
        int position = findLastFilledColumn() + 1;
        if (position < views.length) {
            views[position][0] = view;
        }
    }

    public void addPlaceholderBorder() {
        if (isViewArrayEmpty()) return;
        addVerticalPlaceholders();
        addHorizontalPlaceholders();
    }

    private boolean isViewArrayEmpty() {
        return views.length == 0 || views[0].length == 0;
    }

    private void addVerticalPlaceholders() {
        if(lastFilledColumn() == 0) return;
        for (int col = 0; col < views.length; col++) {
            int lastFilledRow = findLastFilledRow(col);
            if (lastFilledRow < 9 && lastFilledRow != -1) {
                views[col][lastFilledRow + 1] = createPlaceholderCardView();
            }
        }
    }

    private int findLastFilledRow(int col) {
        for (int row = views[col].length - 1; row >= 0; row--) {
            if (isNonPlaceholder(views[col][row])) {
                return row;
            }
        }
        return -1;
    }

    private int lastFilledMaxRow(){
        int lastFilledRow = -1;
        for (View[] column : views) {
            for (int row = 0; row < column.length; row++) {
                if (column[row] != null && !"placeholder".equals(column[row].getTag())) {
                    lastFilledRow = Math.max(lastFilledRow, row);
                }
            }
        }
        return lastFilledRow;
    }
    private void addHorizontalPlaceholders() {
        int lastFilledColumn = findLastFilledColumn();
        if(lastFilledMaxRow() == 0) return;
        if (lastFilledColumn < views.length - 1 && hasNonPlaceholderInColumn(lastFilledColumn)) {
            if (!areAllNonPlaceholdersInFirstRow(lastFilledColumn)) {
                views[lastFilledColumn + 1][0] = createPlaceholderCardView();
            }
        }
    }

    private int findLastFilledColumn() {
        for (int col = views.length - 1; col >= 0; col--) {
            if (hasNonPlaceholderInColumn(col)) {
                return col;
            }
        }
        return -1;
    }

    private boolean hasNonPlaceholderInColumn(int col) {
        for (View view : views[col]) {
            if (isNonPlaceholder(view)) {
                return true;
            }
        }
        return false;
    }

    private boolean areAllNonPlaceholdersInFirstRow(int col) {
        for (int row = 0; row < views[col].length; row++) {
            if (isNonPlaceholder(views[col][row])) {
                return false;
            }
        }
        return true;
    }

    private boolean isNonPlaceholder(View view) {
        return view != null && !"placeholder".equals(view.getTag());
    }

    public void filterAndRemovePlaceholderColumns() {
        if (isViewArrayEmpty()) return;

        int numColumns = views.length;
        int numRows = views[0].length;
        View[][] compressedViews = new View[numColumns][numRows];

        compressViews(numColumns, numRows, compressedViews);
        shiftCompressedViewsLeft(numColumns, numRows, compressedViews);
    }

    private void compressViews(int numColumns, int numRows, View[][] compressedViews) {
        for (int col = 0; col < numColumns; col++) {
            int compressedRowIndex = 0;
            for (int row = 0; row < numRows; row++) {
                View view = views[col][row];
                if (isNonPlaceholder(view)) {
                    compressedViews[col][compressedRowIndex++] = view;
                }
            }
        }
    }

    private void shiftCompressedViewsLeft(int numColumns, int numRows, View[][] compressedViews) {
        int compressedColIndex = 0;
        for (int col = 0; col < numColumns; col++) {
            if (hasNonNullViews(compressedViews[col])) {
                System.arraycopy(compressedViews[col], 0, views[compressedColIndex], 0, numRows);
                compressedColIndex++;
            }
        }
        clearRemainingColumns(compressedColIndex, numColumns);
    }

    private boolean hasNonNullViews(View[] columnViews) {
        for (View view : columnViews) {
            if (view != null) {
                return true;
            }
        }
        return false;
    }

    private void clearRemainingColumns(int fromColumn, int toColumn) {
        for (int col = fromColumn; col < toColumn; col++) {
            Arrays.fill(views[col], null);
        }
    }

    public int lastFilledColumn() {
        return findLastFilledColumn();
    }

    public int lastFilledNonNullColumn() {
        for (int col = views.length - 1; col >= 0; col--) {
            if (views[col][0] != null) {
                return col;
            }
        }
        return -1;
    }

    public int lastFilledMaxNonNullRow() {
        int lastFilledRow = -1;
        for (View[] column : views) {
            for (int row = 0; row < column.length; row++) {
                if (column[row] != null) {
                    lastFilledRow = Math.max(lastFilledRow, row);
                }
            }
        }
        return lastFilledRow;
    }

    public int lastFilledRow(int column) {
        return findLastFilledRow(column);
    }

    private CardView createPlaceholderCardView() {
        CardView cardView = new CardView(context);
        cardView.setRadius(10);
        cardView.setCardElevation(10);
        cardView.setContentPadding(10, 10, 10, 10);
        cardView.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        ));
        cardView.setBackgroundResource(R.drawable.dashed_outline);
        cardView.setTag("placeholder");

        TextView textView = new TextView(context);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        cardView.addView(textView);

        return cardView;
    }

    public Pair<Integer, Integer> findViewPos(View view) {
        if (view == null) return null;
        for (int col = 0; col < views.length; col++) {
            for (int row = 0; row < views[col].length; row++) {
                if (view.equals(views[col][row])) {
                    return new Pair<>(col, row);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ViewsMap:\n");
        for (View[] row : views) {
            for (View view : row) {
                if (view == null) {
                    sb.append("[    ] ");
                } else {
                    String tag = (String) view.getTag();
                    sb.append(String.format("[%s] ", tag != null ? tag : "no tag"));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
