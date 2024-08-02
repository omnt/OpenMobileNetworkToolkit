package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Pair;
import android.view.View;

import androidx.cardview.widget.CardView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ViewsManager {
    private Context context;
    private View[][] views;

    public ViewsManager(Context context) {
        this.context = context;
        this.views = new View[10][10];
    }

    public View[][] getViewsMap() {
        return views;
    }

    public void setViewsMap(View[][] views) {
        this.views = views;
    }

    public void addNewView(View view) {
        int possiblePos = lastFilledColumn() + 1;
        if (possiblePos >= views.length) return;
        views[possiblePos][0] = view;
    }

    public void addPlaceholderBorder() {
        if (views == null || views.length == 0 || views[0].length == 0) {
            return;
        }

        for (int col = 0; col < views.length; col++) {
            int lastFilledRow = -1;
            boolean hasNonPlaceholder = false;
            for (int row = 0; row < views[col].length; row++) {
                if (views[col][row] != null && !"placeholder".equals(views[col][row].getTag())) {
                    lastFilledRow = row;
                    hasNonPlaceholder = true;
                }
            }
            if (lastFilledRow < 9 && hasNonPlaceholder) {
                views[col][lastFilledRow + 1] = createCardView();
            }
        }

        int lastFilledColumn = lastFilledColumn();
        if (lastFilledColumn < views.length - 1) {
            boolean previousColumnHasNonPlaceholder = false;
            for (int row = 0; row < views[lastFilledColumn].length; row++) {
                if (views[lastFilledColumn][row] != null && !"placeholder".equals(views[lastFilledColumn][row].getTag())) {
                    previousColumnHasNonPlaceholder = true;
                    break;
                }
            }
            if (previousColumnHasNonPlaceholder) {
                boolean allNonPlaceholderInOneRow = true;
                for (int row = 0; row < views[lastFilledColumn].length; row++) {
                    if (views[lastFilledColumn][row] != null && !"placeholder".equals(views[lastFilledColumn][row].getTag())) {
                        if (row != 0) {
                            allNonPlaceholderInOneRow = false;
                            break;
                        }
                    }
                }
                if (!allNonPlaceholderInOneRow) {
                    views[lastFilledColumn + 1][0] = createCardView();
                }
            }
        }
    }

    public void filterAndRemovePlaceholderColumns() {
        if (views == null || views.length == 0 || views[0].length == 0) {
            return;
        }

        // Step 1: Compress columns to remove gaps
        int numColumns = views.length;
        int numRows = views[0].length;

        // Create a new compressed grid with the same size
        View[][] compressedViews = new View[numColumns][numRows];

        // Compress columns
        for (int col = 0; col < numColumns; col++) {
            int compressedRowIndex = 0;
            for (int row = 0; row < numRows; row++) {
                View view = views[col][row];
                if (view != null && !"placeholder".equals(view.getTag())) {
                    compressedViews[col][compressedRowIndex++] = view;
                }
            }
        }

        // Step 2: Compress rows to remove gaps
        // We need to collect non-null columns and move them left
        int compressedColIndex = 0;
        for (int col = 0; col < numColumns; col++) {
            boolean hasNonNullViews = false;
            for (int row = 0; row < numRows; row++) {
                if (compressedViews[col][row] != null) {
                    hasNonNullViews = true;
                    break;
                }
            }
            if (hasNonNullViews) {
                // Move the column to the compressed position
                for (int row = 0; row < numRows; row++) {
                    views[compressedColIndex][row] = compressedViews[col][row];
                }
                compressedColIndex++;
            }
        }

        // Clear remaining columns (if any) to null
        for (int col = compressedColIndex; col < numColumns; col++) {
            Arrays.fill(views[col], null);
        }
    }


    private int lastFilledColumn() {
        int lastFilledColumn = 0;
        for (int i = 0; i < views.length; i++) {
            if (views[i][0] != null && !"placeholder".equals(views[i][0].getTag())) {
                lastFilledColumn = i;
            }
        }
        return lastFilledColumn;
    }

    private int lastFilledRow(int column) {
        int lastFilledRow = 0;
        for (int i = 0; i < views[column].length; i++) {
            if (views[column][i] != null && !"placeholder".equals(views[column][i].getTag())) {
                lastFilledRow = i;
            }
        }
        return lastFilledRow;
    }

    private CardView createCardView() {
        CardView cardView = new CardView(context);
        cardView.setRadius(10);
        cardView.setCardElevation(10);
        cardView.setContentPadding(10, 10, 10, 10);
        cardView.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT // Change to match parent height
        ));
        cardView.setBackgroundResource(R.drawable.dashed_outline);
        cardView.setTag("placeholder");
        return cardView;
    }

    public Pair<Integer, Integer> findViewPos(View view) {
        if (view == null) return null;
        for (int i = 0; i < views.length; i++) {
            for (int j = 0; j < views[i].length; j++) {
                if (views[i][j] == null) continue;

                if (views[i][j].equals(view)) {
                    return new Pair<>(i, j);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ViewsMap:\n");
        for (int i = 0; i < views.length; i++) {
            for (int j = 0; j < views[i].length; j++) {
                View view = views[i][j];
                if (view == null) {
                    sb.append("[    ]"); // Empty cell
                } else {
                    String tag = (String) view.getTag();
                    sb.append(String.format("[%s]", tag != null ? tag : "no tag"));
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
