package tc.scworldeditor;
import android.app.Activity;
import android.app.AlertDialog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;

public class U {
    public static final String TAG = "U";
    public static String worldsPath;
    public static final int[] inventoryId = {R.id.inventoryItem0,R.id.inventoryItem1,R.id.inventoryItem2,R.id.inventoryItem3,R.id.inventoryItem4,R.id.inventoryItem5,R.id.inventoryItem6,R.id.inventoryItem10,R.id.inventoryItem11,R.id.inventoryItem12,R.id.inventoryItem13,R.id.inventoryItem14,R.id.inventoryItem15,R.id.inventoryItem16,R.id.inventoryItem17,R.id.inventoryItem18,R.id.inventoryItem19,R.id.inventoryItem20,R.id.inventoryItem21,R.id.inventoryItem22,R.id.inventoryItem23,R.id.inventoryItem24,R.id.inventoryItem25};
    public static final String[] inventorySlot = {"Slot0", "Slot1", "Slot2", "Slot3", "Slot4", "Slot5", "Slot6", "Slot10", "Slot11", "Slot12", "Slot13", "Slot14", "Slot15", "Slot16", "Slot17", "Slot18", "Slot19", "Slot20", "Slot21", "Slot22", "Slot23", "Slot24", "Slot25"};
    public static Element[] inventoryElememts = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
    static float[] seasons = {0.75f,0.75f + 0.25f / 3,0.75f + 0.25f / 1.5f,1f,0.25f / 3,0.25f / 1.5f,0.25f,0.25f + 0.25f / 3,0.25f + 0.25f / 1.5f,0.5f,0.5f + 0.25f / 3,0.5f + 0.25f / 1.5f};

    public static HashMap<String, Object> parseXml(String xml) {
        HashMap<String, Object> result = new HashMap<>();
        HashMap<String, Object> currentElement = result;
        String currentTag = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            StringReader stringReader = new StringReader(xml);
            parser.setInput(stringReader);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    HashMap<String, Object> element = new HashMap<>();
                    if (currentTag != null) {
                        // 如果当前元素不为空，则将新元素作为子元素添加到父元素中
                        currentElement.put(tagName, element);
                    } else {
                        // 如果是根元素，则直接添加到结果中
                        result.put(tagName, element);
                    }
                    currentTag = tagName;
                    currentElement = element;

                    // 解析属性
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);
                        currentElement.put(attrName, attrValue);
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText().trim();
                    if (!text.isEmpty()) {
                        currentElement.put("text", text);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    if (currentTag != null && currentTag.equals(tagName)) {
                        currentTag = null;
                        currentElement = (HashMap<String, Object>) result.get(tagName);
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String elementToString(Element element) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Element getElememtByTagName(Element e, String tag) {
        return (Element) e.getElementsByTagName(tag).item(0);
    }
    public static Element[] getElememtsByTagName(Element e, String tag) {
        NodeList nl = e.getElementsByTagName(tag);
        List<Element> l = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            l.add((Element)nl.item(i));
        }
        return l.toArray(new Element[0]);
    }
    public static Element getElememtByTagNameAndAttr(Element e, String tag, String attrName, String attrValue) {
        Element[] es = getElememtsByTagName(e, tag);
        for (int i = 0; i < es.length; i++) {
            if (es[i].getAttribute(attrName).equals(attrValue)) {
                return es[i];
            }
        }
        return null;
    }
    public static String getStringFromAssets(Context context, String fileName) { 
        try { 
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName)); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while ((line = bufReader.readLine()) != null)
                Result += line + "\n";
            return Result;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "";
        }
    }
    static int getSeasonId(String season) {
        float s=Float.parseFloat(season);
        if (s < 0.25 / 3) {
            return 3;
        } else if (s < 0.25 / 1.5) {
            return 4;
        } else if (s < 0.25) {
            return 5;
        } else if (s < 0.25 + 0.25 / 3) {
            return 6;
        } else if (s < 0.25 + 0.25 / 1.5) {
            return 7;
        } else if (s < 0.25 + 0.25) {
            return 8;
        } else if (s < 0.5 + 0.25 / 3) {
            return 9;
        } else if (s < 0.5 + 0.25 / 1.5) {
            return 10;
        } else if (s < 0.75) {
            return 11;
        } else if (s < 0.75 + 0.25 / 3) {
            return 0;
        } else if (s < 0.75 + 0.25 / 1.5) {
            return 1;
        } else {
            return 2;
        }
    }



    public static void t(Activity act, String a) {
        AlertDialog dialog = new AlertDialog.Builder(act)
            .setMessage(a)
            .create();
        dialog.show();
    }
}
