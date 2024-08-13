package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import androidx.cardview.widget.CardView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;

public class XMLtoUI {

    private LinearLayout createRow(Context context){
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        return ll;
    }

    private CardView createCard(String title, String value, Context context) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardViewLayout = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardViewLayout.weight = 1;
        card.setLayoutParams(cardViewLayout);
        card.setRadius(9);
        card.setCardElevation(9);
        card.setMaxCardElevation(9);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView titleTV = new TextView(context);
        titleTV.setText(title);
        titleTV.setTextSize(20);
        titleTV.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.weight = 1;
        titleTV.setLayoutParams(titleParams);
        ll.addView(titleTV);

        TextView valueTV = new TextView(context);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.gravity = Gravity.CENTER;
        valueParams.weight = 1;
        valueTV.setLayoutParams(valueParams);
        valueTV.setTextSize(20);

        if (value.isEmpty() || value.equals("") || value.equals(String.valueOf(Integer.MAX_VALUE))) {
            value = "N/A";
        }
        if (value.length() > 7) { // Adjust the length threshold as needed
            valueTV.setTextSize(19);
        }

        valueTV.setText(value);
        valueTV.setPadding(10, 10, 10, 10);
        ll.addView(valueTV);

        card.addView(ll);
        return card;
    }

    public LinearLayout createUIFromXML(Context context, Document xmlDoc, Object data) {
        LinearLayout ll = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(params);

        Element rootElement = xmlDoc.getDocumentElement();
        NodeList rows = rootElement.getElementsByTagName("row");
        if(data instanceof CellInformation){
            data = (CellInformation) data;
        }
        for (int i = 0; i < rows.getLength(); i++) {
            Element rowElement = (Element) rows.item(i);
            NodeList columns = rowElement.getElementsByTagName("column");

            LinearLayout rowLayout = createRow(context);

            for (int j = 0; j < columns.getLength(); j++) {
                Element columnElement = (Element) columns.item(j);
                String key = columnElement.getElementsByTagName("key").item(0).getTextContent();
                String value = "";
                if (columnElement.getElementsByTagName("value").getLength() > 0) {
                    value = columnElement.getElementsByTagName("value").item(0).getTextContent();
                } else if (columnElement.getElementsByTagName("range").getLength() > 0) {
                    String min = columnElement.getElementsByTagName("min").item(0).getTextContent();
                    String max = columnElement.getElementsByTagName("max").item(0).getTextContent();
                    value = "Min: " + min + " Max: " + max;
                }
                rowLayout.addView(createCard(key, value, context));
            }

            ll.addView(rowLayout);
        }

        return ll;
    }

    public Document loadXmlFromFile(Context context, int rawResourceId) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(context.getResources().openRawResource(rawResourceId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
