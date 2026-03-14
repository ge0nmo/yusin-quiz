package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
public class ProblemHtmlImageStorageService {

    private final FileService fileService;
    private final UuidHolder uuidHolder;

    @Autowired
    public ProblemHtmlImageStorageService(FileService fileService,
                                          @Qualifier("systemUuidHolder") UuidHolder uuidHolder) {
        this.fileService = fileService;
        this.uuidHolder = uuidHolder;
    }

    public String replaceEmbeddedImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent;
        }

        Document document = Jsoup.parseBodyFragment(htmlContent);
        Elements images = document.select("img[src^=data:image]");

        for (Element image : images) {
            replaceImageSource(image);
        }

        return document.body().html();
    }

    private void replaceImageSource(Element image) {
        String source = image.attr("src");

        try {
            String[] encodedImage = source.split(",", 2);
            String header = encodedImage[0];
            String data = encodedImage[1];
            String extension = header.split(";")[0].split("/")[1];
            byte[] imageBytes = Base64.getDecoder().decode(data);
            String filename = uuidHolder.getRandom() + "." + extension;
            String uploadedUrl = fileService.saveByteArray(imageBytes, filename, "image/" + extension);

            image.attr("src", uploadedUrl);
            image.attr("style", "max-width: 100%; height: auto;");
        } catch (RuntimeException exception) {
            log.error("문제 HTML 내 Base64 이미지 업로드에 실패했습니다.", exception);
        }
    }
}
