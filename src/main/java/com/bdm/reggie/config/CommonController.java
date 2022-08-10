package com.bdm.reggie.config;

import com.bdm.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @code Description 文件的上传和下载
 * @code author 本当迷
 * @code date 2022/8/6-7:47
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传：注意MultipartFile 的形参名要和前端的表单提交的name相同
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String>upload(MultipartFile file){
        // file时一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除

        // 原始文件名
        final String originalFilename = file.getOriginalFilename();
        // 文件格式
        assert originalFilename != null;
        final String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        final String fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象
        final File dir = new File(basePath);
        // 判断当前目录是否存在
        if(!dir.exists()){
            // 目录不存在，需要创建
            final boolean mkdir = dir.mkdir();
        }

        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("文件上传。。。");
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        FileInputStream inputStream  = null;
        ServletOutputStream outputStream = null;
        try {
            // 输入流，通过输入流读取文件内容
            inputStream = new FileInputStream(new File(basePath + name));

            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            final byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

}
