package org.idea.irpc.framework.core.registry.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.idea.irpc.framework.core.common.event.IRpcEvent;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.event.IRpcUpdateEvent;
import org.idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.idea.irpc.framework.core.registry.RegistryService;
import org.idea.irpc.framework.core.registry.URL;
import org.idea.irpc.framework.interfaces.DataService;

import java.util.List;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 13:47
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private AbstractZookeeperClient zkClient;

    private String ROOT = "/irpc";

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host")+":";
    }

    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }




    @Override
    public List<String> getProviderIps(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        return nodeDataList;
    }

    @Override
    public void register(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }

        String urlStr = URL.buildConsumerUrlStr(url);
        if (!zkClient.existNode(getProviderPath(url))){
            zkClient.createTemporaryData(getProviderPath(url), urlStr);
        } else {
            zkClient.deleteNode(getProviderPath(url));
            zkClient.createTemporaryData(getProviderPath(url), urlStr);
        }
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (!zkClient.existNode(getConsumerPath(url))) {
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        } else {
            zkClient.deleteNode(getConsumerPath(url));
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        }
        super.subscribe(url);
    }


    @Override
    public void doAfterSubscribe(URL url) {
        // 监听是否有新的服务注册
        String newServerNodePath = ROOT + "/" + url.getServiceName() + "/provider";
        watchChildNodeData(newServerNodePath);
    }

    /**
     * 当监听到某个节点的数据发生更新之后，会发送一个节点更新的事件，
     * 然后在事件的监听端对不同的行为做不同的事件处理操作。
     * @param newServerNodePath
     */
    private void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent);
                String path = watchedEvent.getPath();
                List<String> childrenDataList = zkClient.getChildrenData(path);
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                urlChangeWrapper.setProviderUrl(childrenDataList);
                urlChangeWrapper.setServiceName(path.split("/")[2]);

                // 自定义的一套事件监听组件
                IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);
                IRpcListenerLoader.sendEvent(iRpcEvent);

                // 收到回调后再注册一次监听，保证一直能收到消息
                // 因为zk节点的消息通知其实是只具有一次性的功效，
                // 所以可能会出现第一次修改节点之后发送一次通知，
                // 之后再次修改节点不再会发送节点变更通知操作。
                // 因此在watchChildNodeData函数中的尾部又加入了一次监听操作。
                watchChildNodeData(path);

            }
        });
    }


    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public void doUnSubScribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubScribe(url);
    }

    public static void main(String[] args) throws InterruptedException {
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println(urls);
        Thread.sleep(2000000);
    }
}
