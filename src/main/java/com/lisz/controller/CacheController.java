package com.lisz.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private MyFile file = MyFile.getInstance();

    @RequestMapping("/ctrl")
    public ResponseEntity<String> last(@RequestHeader(value="IF-Modified-Since",required = false) Date ifModifiedSince) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

        long now = System.currentTimeMillis() / 1000 *1000;
        // 系统当前时间

        System.out.println(now);
        // 缓存时间
        long maxAge = 20;

        HttpHeaders headers = new HttpHeaders();

        if (null != ifModifiedSince && ifModifiedSince.getTime() == file.getLastModified() ) {

            System.out.println(304);

        }

        // age: 20292957 中age这个字段是CDN下发下来的，后面的数字是指这个资源在CDN上已经存在或更新了多长时间了，单位是秒。清理缓存刷新的时候会改变，不清缓存刷新不变
        // via 也是CDN下发下来的，代表一台服务器的名称，[]里标记缓存有没有命中，MISS就是没命中CDN缓存，request则会打到源服务器（tomcat）上，对于客户端没啥用，是给
        // 开发人员排查错误用的：如果觉得后端服务器的负载量跟预想的不一样，就来查查这个，第一次MISS，第二次还是MISS的话就有问题了
        headers.add("Date", simpleDateFormat.format(new Date(now)));
        headers.add("Expires", simpleDateFormat.format(new Date(now + maxAge * 1000)));
        headers.add("Cache-Control", "max-age="+maxAge);//max-age指的是返回的页面的最大有效时间是多长，一秒为单位
        headers.add("Last-Modified", simpleDateFormat.format(new Date(file.getLastModified())));

        String body = "<a href =''>hi点我</a>";
        return new ResponseEntity<>(body,headers,HttpStatus.OK);
    }

    @RequestMapping("/")
    public ResponseEntity<String> last(@RequestHeader(value="If-None-Match",required = false) String ifNoneMatch) {//value="IF-Modified-Since"

        System.out.println(ifNoneMatch);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

        long now = System.currentTimeMillis() / 1000 *1000;

        HttpHeaders headers = new HttpHeaders(); // HttpHeader有点像一个Map，其add方法相当于put

        String body = "<a href =''>hi点我</a>";

        String ETag = "\"" + getMd5(body) + "\"";
        System.out.println("ETag: " + ETag);

        //由于Tomcat会帮着我们返回304或者200，所以我们自己不用做逻辑
        if (ETag.equals(ifNoneMatch)) {
            //返回304
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        } else {
            System.out.println("Setting the headers");
            // 加载资源返回. "ETag"是http协议的保留字段
            // 服务端设置了ETag之后，浏览器下次访问的时候会聪明地带上ETag，放在header的"If-None-Match"字段里供server检查
            headers.add("Date", simpleDateFormat.format(new Date(now)));
            headers.add("owner", "lisz");
            headers.add("ETag", ETag); //ETag是http关键字，Etag这里不区分大小写
            return new ResponseEntity<>(body,headers,HttpStatus.OK);
        }
    }

    /**
     * 字符串转md5
     * @param msg
     * @return
     */
    private String getMd5(String msg) {
        MessageDigest md5 = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        md5.update(msg.getBytes());
        byte[] digest = md5.digest();

        StringBuffer buf = null;
        buf = new StringBuffer(digest.length * 2);
        //遍历
        for (int i = 0; i < digest.length; i++) {
            if (((int) digest[i] & 0xff) < 0x10) { //(int) b[i] & 0xff 转换成无符号整型
                buf.append("0");
            }
            //Long.toHexString( 无符号长整数的十六进制字符串表示
            buf.append(Long.toHexString((int) digest[i] & 0xff));
        }
        return buf.toString();
    }

}