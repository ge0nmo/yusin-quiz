package com.cpa.yusin.quiz.config;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class LogbackConfigurationTest
{
    @Test
    void defaultAndLocalProfilesShouldUseConsoleAppenderOnly() throws Exception
    {
        Document document = parseLogbackConfiguration();

        assertThat(findTopLevelAppender(document, "FILE")).isNull();

        Element consoleOnlyProfile = findSpringProfile(document, "default | local | test");

        assertThat(consoleOnlyProfile).isNotNull();
        assertThat(findAppender(consoleOnlyProfile, "FILE")).isNull();
        assertThat(findAppenderRef(consoleOnlyProfile, "FILE")).isNull();
        assertThat(findAppenderRef(consoleOnlyProfile, "CONSOLE")).isNotNull();
    }

    @Test
    void nonLocalProfilesShouldDeclareFileAppenderWithConfigurablePath() throws Exception
    {
        Document document = parseLogbackConfiguration();

        Element fileLoggingProfile = findSpringProfile(document, "!default & !local & !test");

        assertThat(fileLoggingProfile).isNotNull();
        assertThat(findAppenderRef(fileLoggingProfile, "FILE")).isNotNull();
        assertThat(textContentOfChild(findAppender(fileLoggingProfile, "FILE"), "file"))
                .isEqualTo("${logPath}/application.log");
        assertThat(textContentOfChild(findFirstChild(findAppender(fileLoggingProfile, "FILE"), "rollingPolicy"), "fileNamePattern"))
                .isEqualTo("${logPath}/application.%d{yyyy-MM-dd}.log");
    }

    private Document parseLogbackConfiguration() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // 외부 엔티티를 차단해서 테스트가 로컬 환경 설정에 영향받지 않게 유지해야 함.
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logback-spring.xml")) {
            assertThat(inputStream).isNotNull();

            Document document = factory.newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();
            return document;
        }
    }

    private Element findSpringProfile(Document document, String profileExpression)
    {
        NodeList springProfiles = document.getElementsByTagName("springProfile");

        for (int i = 0; i < springProfiles.getLength(); i++) {
            Element springProfile = (Element) springProfiles.item(i);
            if (profileExpression.equals(springProfile.getAttribute("name"))) {
                return springProfile;
            }
        }

        return null;
    }

    private Element findTopLevelAppender(Document document, String appenderName)
    {
        return findNamedChild(document.getDocumentElement(), "appender", appenderName);
    }

    private Element findAppender(Element parent, String appenderName)
    {
        return findNamedDescendant(parent, "appender", appenderName);
    }

    private Element findAppenderRef(Element parent, String appenderName)
    {
        return findNamedDescendant(parent, "appender-ref", appenderName);
    }

    private Element findNamedChild(Element parent, String tagName, String name)
    {
        NodeList childNodes = parent.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element childElement = (Element) childNode;
            if (tagName.equals(childElement.getTagName()) && name.equals(childElement.getAttribute("name"))) {
                return childElement;
            }
        }

        return null;
    }

    private Element findNamedDescendant(Element parent, String tagName, String name)
    {
        NodeList descendants = parent.getElementsByTagName(tagName);

        for (int i = 0; i < descendants.getLength(); i++) {
            Element descendant = (Element) descendants.item(i);
            if (name.equals(descendant.getAttribute("name")) || name.equals(descendant.getAttribute("ref"))) {
                return descendant;
            }
        }

        return null;
    }

    private Element findFirstChild(Element parent, String tagName)
    {
        NodeList childNodes = parent.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element childElement = (Element) childNode;
            if (tagName.equals(childElement.getTagName())) {
                return childElement;
            }
        }

        return null;
    }

    private String textContentOfChild(Element parent, String tagName)
    {
        Element child = findFirstChild(parent, tagName);
        assertThat(child).isNotNull();
        return child.getTextContent().trim();
    }
}
