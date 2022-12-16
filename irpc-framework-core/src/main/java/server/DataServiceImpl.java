package server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.interfaces.DataService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DataServiceImpl implements DataService {
    public String sendData(String body) {
        log.info("DataService己收到的参数长度：" + body.length());
        return "success";
    }

    public List<String> getList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }
}
