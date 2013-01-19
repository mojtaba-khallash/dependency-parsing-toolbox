/**
 * Copyright (c) 2011, Regents of the University of Colorado All rights
 * reserved.
 * 
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
* Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the University of Colorado at
 * Boulder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package clear.reader;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jinho D. Choi <b>Last update:</b> 8/2/2011
 */
public class ReaderXML {

    /**
     * Elements
     */
    static public final String READER = "reader";
    static public final String COLUMN = "column";
    /**
     * Attributes
     */
    static public final String READER_FORMAT = "format";
    static public final String COLUMN_INDEX = "index";
    static public final String COLUMN_TYPE = "type";
    /**
     * Values
     */
    static public final String FORMAT_RAW = "raw";
    static public final String FORMAT_LINE = "line";
    static public final String FORMAT_COLUMN = "column";
    public String s_format;
    public ObjectIntOpenHashMap<String> m_columns;

    public ReaderXML(String xmlFile) {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = dFactory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(xmlFile));

            init(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ReaderXML(Document doc) {
        try {
            init(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void init(Document doc) throws Exception {
        NodeList eList = doc.getElementsByTagName(READER);

        if (eList.getLength() <= 0) {
            System.err.println("Error: reader format not defined");
            return;
        }

        Element eReader = (Element) eList.item(0);
        s_format = eReader.getAttribute(READER_FORMAT);

        if (s_format.equals(FORMAT_COLUMN)) {
            NodeList eColumns = eReader.getElementsByTagName(COLUMN);
            Element eColumn;
            int index;
            String type;

            m_columns = new ObjectIntOpenHashMap<>();

            for (int i = 0; i < eColumns.getLength(); i++) {
                eColumn = (Element) eColumns.item(i);
                index = Integer.parseInt(eColumn.getAttribute(COLUMN_INDEX));
                type = eColumn.getAttribute(COLUMN_TYPE);

                m_columns.put(type, index);
            }
        } else if (!s_format.matches(FORMAT_RAW + "|" + FORMAT_LINE)) {
            System.err.println("Error: unknown reader format '" + s_format + "'");
        }
    }

    public void print() {
        System.out.println(s_format);

        if (m_columns != null) {
            for (ObjectCursor<String> cur : m_columns.keys()) {
                System.out.printf("%3d: %s\n", m_columns.get(cur.value), cur.value);
            }
        }
    }
    /*
     * static public void main(String[] args) { new ReaderXML(args[0]).print();
	}
     */
}