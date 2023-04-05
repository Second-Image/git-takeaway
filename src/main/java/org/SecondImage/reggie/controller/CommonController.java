package org.SecondImage.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.channels.MulticastChannel;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){ //参数名 file 应与前端传入的<input name="file">的name的值一致
        //file是临时文件，需要转存到指定位置，因为临时文件完成请求后会删除
        log.info(file.toString());
        //获取文件原名
        String originalFileName = file.getOriginalFilename();
        //获取文件后缀
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        //重新生成文件名，添加后缀
        //文件本体数据是传入的参数file
        String fileName = UUID.randomUUID().toString() + suffix;

        File dir = new File(basePath);
        //判断该目录是否存在，若不存在则创建
        if (!dir.exists()){
            dir.mkdirs();
        }

        try {
            //将临时文件存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            //将临时文件存到指定位置
//            file.transferTo(new File("D:\\hello.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //返回文件名，新增菜品时要获取图片文件名传给服务器存入数据库
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name){
        try {
            //通过输入流读取文件
            FileInputStream inputStream = new FileInputStream(new File(basePath + name));
            //通过response对象的输出流将文件写回浏览器，在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
//            response.setContentType("/image/jpeg");  注意 / ，image/jpeg才是一种数据类型

            int len = 0;
            byte[] bytes = new byte[1024];
            //每次读1Mb的文件数据  读到数据长度-1表示读完，如果数据未读完则无法正常显示（如显示空白）或报错
            while ((len = inputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
