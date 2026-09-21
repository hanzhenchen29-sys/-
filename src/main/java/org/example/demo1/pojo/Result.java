package org.example.demo1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer code;//响应码：
    private String message;//响应信息，描述字符串
    private Object data;//返回的数据
    public static Result success(Object data){
        return new Result(0,"ok",data);
    }
    public static Result error(String message,Integer code){return new Result(code,message,null);}
    public static Result success(){
        return new Result(0,"ok",null);
    }

}
