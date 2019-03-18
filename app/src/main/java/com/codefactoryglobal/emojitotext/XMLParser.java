package com.codefactoryglobal.emojitotext;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class XMLParser {
    private static final String TAG = "ETXMLParser";
    // We don't use namespaces
    private static final String ns = null;
    private final InputStream input;

    public XMLParser(InputStream in) {
        input = in;
    }

    public ArrayList<Translation> parse() {
        ArrayList<Translation> entries = new ArrayList<>();

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input, null);
            parser.nextTag();
            entries = readFeed(parser);
        } catch (Exception id1) {
            Log.e(TAG, "Error parsing XML: " + id1.toString());
        }
        return entries;
    }

    private ArrayList<Translation> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<Translation> entries = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "ldml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            // Starts by looking for the annotation tag
            if (name.equals("annotation")) {
                Translation line = readAnnotation(parser);
                if (line != null) {
                    entries.add(line);
                }
            } else if (!name.equals("annotations")) {
                skip(parser);
            }
        }
        return entries;  //1336
    }


    private Translation readAnnotation(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "annotation");

        String codePoint = "";
        String type = "";
        String description = "";

        codePoint = parser.getAttributeValue(null, "cp");
        type = parser.getAttributeValue(null, "type");

        if (type == null || type.compareToIgnoreCase("tts") != 0) {
            parser.next();
            parser.next();
            parser.require(XmlPullParser.END_TAG, ns, "annotation");
            return null;
        }

        description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "annotation");

        // TODO test
        description = replaceXMLInvalidChars(description);

        return new Translation(codePoint, description);
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String replaceXMLInvalidChars(String line) {
        line = line.replace("\"","&quot;");
        line = line.replace("'","&apos;");
        line = line.replace("&","&amp;");
        line = line.replace("<","&lt;");
        line = line.replace(">","&gt;");
        line = line.replace("...","…");
        return line;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
