package clear.util;

import java.io.UnsupportedEncodingException;

public class JString {

    static public String getUTF8(String str) {
        String utf = "";

        try {
            utf = new String(str.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
        }

        return utf;
    }

    static public String getNormalizedForm(float f) {
        return (f == (int) f) ? String.valueOf((int) f) : String.valueOf(f);
    }

    static public String[] toArray(String... args) {
        return args;
    }
}