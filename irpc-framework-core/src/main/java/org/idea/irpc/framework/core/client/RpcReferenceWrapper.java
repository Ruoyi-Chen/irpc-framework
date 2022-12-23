package org.idea.irpc.framework.core.client;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc远程调用包装类
 *
 * @Author Chen Ruoyi
 * @Date created in 11:28 上午 2022/1/29
 */
public class RpcReferenceWrapper<T> {
    /**
     * aimClass: 目标类
     * attachments：
     *  async：是否异步
     *  url：url地址
     *  serviceToken：发送的服务token
     *  group：所属类
     */
    private Class<T> aimClass;

    private Map<String,Object> attatchments = new ConcurrentHashMap<>();

    public Class<T> getAimClass() {
        return aimClass;
    }

    public void setAimClass(Class<T> aimClass) {
        this.aimClass = aimClass;
    }

    public boolean isAsync(){
        return Boolean.valueOf(String.valueOf(attatchments.get("async")));
    }

    public void setAsync(boolean async){
        this.attatchments.put("async",async);
    }

    public String getUrl(){
        return String.valueOf(attatchments.get("url"));
    }

    public void setUrl(String url){
        attatchments.put("url",url);
    }

    public String getServiceToken(){
        return String.valueOf(attatchments.get("serviceToken"));
    }

    public void setServiceToken(String serviceToken){
        attatchments.put("serviceToken",serviceToken);
    }

    public String getGroup(){
        return String.valueOf(attatchments.get("group"));
    }

    public void setGroup(String group){
        attatchments.put("group",group);
    }

    public Map<String, Object> getAttatchments() {
        return attatchments;
    }

    public void setAttatchments(Map<String, Object> attatchments) {
        this.attatchments = attatchments;
    }

}
