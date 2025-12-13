package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.file.controller.dto.response.FileResponse;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RequestMapping("/admin/file")
@RestController
public class FileController
{
    private final FileService fileService;

    @PostMapping
    public String save(@RequestParam(value = "file") MultipartFile file)
    {
        return fileService.save(file);
    }
}
