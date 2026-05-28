package de.dbspraktikum.loader.parse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class XmlUtil {
    private XmlUtil() {
    }

    public static Document parseXml(Path path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (InputStream input = Files.newInputStream(path)) {
            return builder.parse(input);
        } catch (Exception first) {
            Charset fallback = path.getFileName().toString().contains("leipzig") ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
            try (BufferedReader reader = Files.newBufferedReader(path, fallback)) {
                return builder.parse(new InputSource(reader));
            }
        }
    }

    public static String attr(Element element, String name) {
        return element == null ? null : element.getAttribute(name);
    }

    public static String text(Element element) {
        return element == null ? null : element.getTextContent();
    }

    public static String firstText(Element parent, String name) {
        return text(firstChild(parent, name));
    }

    public static Element firstChild(Element parent, String name) {
        if (parent == null) {
            return null;
        }
        for (Element child : children(parent, name)) {
            return child;
        }
        return null;
    }

    public static List<Element> children(Element parent) {
        if (parent == null) {
            return List.of();
        }
        List<Element> children = new ArrayList<>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node node = parent.getChildNodes().item(i);
            if (node instanceof Element element) {
                children.add(element);
            }
        }
        return children;
    }

    public static List<Element> children(Element parent, String name) {
        if (parent == null) {
            return List.of();
        }
        List<Element> children = new ArrayList<>();
        for (Element child : children(parent)) {
            if (name.equals(child.getTagName())) {
                children.add(child);
            }
        }
        return children;
    }

    public static String directText(Element element) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node node = element.getChildNodes().item(i);
            if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
                builder.append(node.getTextContent());
            }
        }
        return builder.toString();
    }

    public static List<String> valuesFromContainer(Element item, String containerName, String childName) {
        Element container = firstChild(item, containerName);
        if (container == null) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (Element child : children(container, childName)) {
            String value = TextUtil.firstNonBlank(child.getAttribute("name"), child.getAttribute("value"), child.getAttribute("val"), child.getTextContent());
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }
}
