package com.example.controller;

import com.example.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传于下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class FilerUploadController {

    //配置文件中导入文件的路径
    @Value("${file.path}")
    private String FilePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());
        log.info(FilePath);
        //若文件不存在创建文件
        File file1=new File(FilePath);
        if(!file1.exists()){
            file1.mkdir();
        }
        //获取原始的文件名，截取其（.jsp）格式
        String originalFilename = file.getOriginalFilename();
        log.info("文件名称："+originalFilename);
        String substring = originalFilename.substring(originalFilename.indexOf("."));
        //构造其文件名
        String FileName= UUID.randomUUID().toString()+substring;
        //写入磁盘
        try {
            file.transferTo(new File(FilePath+FileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(FileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        FileInputStream fileInputStream =null;
        BufferedInputStream bufferedInputStream=null;
        ServletOutputStream outputStream=null;
        try {
            //字节缓冲输入流读取指定name的文件
            fileInputStream=new FileInputStream(new File(FilePath+name));
            bufferedInputStream =new BufferedInputStream(fileInputStream);
            //输出流,写回浏览器
            outputStream = response.getOutputStream();
            int len;
            byte [] bytes =new byte[1024];
            while((len=bufferedInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //释放资源
            log.info("释放资源");
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
