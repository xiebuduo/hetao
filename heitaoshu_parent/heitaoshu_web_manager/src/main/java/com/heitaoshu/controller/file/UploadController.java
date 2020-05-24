package com.heitaoshu.controller.file;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Date;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private HttpServletRequest request;
    @PostMapping("/native")
    public String nativeUpload(@RequestParam("file") MultipartFile file) {
        //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String path = request.getSession().getServletContext().getRealPath("img");
        String fileName = new Date().getTime() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filePath = path + "/" + fileName;
        File desFile = new File(filePath);
        if (!desFile.getParentFile().exists()) {
            desFile.mkdirs();
        }
        try {
            file.transferTo(desFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("path:---" + filePath);
        return "http://localhost/img/" + fileName;
    }

    @Autowired
    private OSSClient ossClient;

    @PostMapping("/oss")
    public String ossUpload(@RequestParam("file") MultipartFile file, String folder) {
        String bucketName = "heitaoshu";
        String fileName = folder + "/" + folder + new Date().getTime() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        try {
            ossClient.putObject(bucketName, fileName, file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "http://" + bucketName + "." + ossClient.getEndpoint().toString().replace("http://", "") + "/" + fileName;
    }


}
