package org.wanghailu.mybatismix.hotdeploy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.support.TwoTuple;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * mapper xml文件变化监听器
 */
public class MapperHotDeployWatcher implements Runnable {
    
    private static Logger logger = LoggerFactory.getLogger(MapperHotDeployWatcher.class);
    
    private MapperHotDeployManager mapperHotDeployManager;
    
    private WatchService watcher;
    
    public MapperHotDeployWatcher(MapperHotDeployManager mapperHotDeployManager) {
        this.mapperHotDeployManager = mapperHotDeployManager;
    }
    
    /**
     * 事件的短期存储。可能xml文件的修改，在build时，实际idea做的事情是先删除再创建修改。 一次修改可能会触发多次事件，如果先触发删除事件再触发创建事件，则会存在空挡时间内找不到mapper文件，即mybatis中出现数据不一致的情况
     */
    private List<TwoTuple<String, WatchEvent<?>>> eventList = new ArrayList<>();
    
    @Override
    public void run() {
        try {
            // 维护 WatchKey 与目录的映射，用于找到变化文件的目录。
            Map<WatchKey, String> watchKeyPathMap = new HashMap<>(8);
            watcher = FileSystems.getDefault().newWatchService();
            for (String watchPath : mapperHotDeployManager.getWatchPaths()) {
                WatchKey watchKey = Paths.get(watchPath).register(watcher,
                        new WatchEvent.Kind[] {StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE});
                watchKeyPathMap.put(watchKey, watchPath);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                close();
            }));
            while (true) {
                //等待，超时就返回
                WatchKey key;
                if (eventList.size() > 0) {
                    key = watcher.poll(1, TimeUnit.SECONDS);
                } else {
                    key = watcher.poll(20, TimeUnit.SECONDS);
                }
                if (key != null) {
                    List<WatchEvent<?>> events = key.pollEvents();
                    for (WatchEvent<?> event : events) {
                        eventList.add(new TwoTuple<>(watchKeyPathMap.get(key), event));
                    }
                    key.reset();
                    continue;
                } else if (eventList.size() == 0) {
                    continue;
                }
                //key为文件绝对路径，value是事件列表
                Map<String, List<TwoTuple<String, WatchEvent<?>>>> eventMap = eventList.stream()
                        .filter(x -> x.getSecond().kind() != StandardWatchEventKinds.OVERFLOW).filter(x -> {
                            String fileName = x.getSecond().context().toString();
                            String endStr = fileName.substring(fileName.length() - 4);
                            return ".xml".equalsIgnoreCase(endStr);
                        }).collect(Collectors.groupingBy(x -> x.getFirst() + "/" + x.getSecond().context().toString()));
                eventList.clear();
                
                if(isAllDeleteEvent(eventMap)){
                    logger.warn("收到mapper.xml全部删除的事件，忽略事件不做处理！");
                    continue;
                }
                for (Map.Entry<String, List<TwoTuple<String, WatchEvent<?>>>> entry : eventMap.entrySet()) {
                    String xmlPath = entry.getKey();
                    List<TwoTuple<String, WatchEvent<?>>> events = entry.getValue();
                    String fileName = events.get(0).getSecond().context().toString();
                    int eventKindFlag = mergeKind(events);
                    mapperHotDeployManager.reloadMapper(xmlPath, fileName, eventKindFlag);
                }
            }
        } catch (ClosedWatchServiceException c) {
            //do nothing
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private boolean isAllDeleteEvent(Map<String, List<TwoTuple<String, WatchEvent<?>>>> eventMap) {
        boolean isAllDelete = true;
        for (Map.Entry<String, List<TwoTuple<String, WatchEvent<?>>>> entry : eventMap.entrySet()) {
            boolean containDelete = false;
            for (TwoTuple<String, WatchEvent<?>> tuple : entry.getValue()) {
                if (tuple.getSecond().kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    containDelete = true;
                    break;
                }
            }
            if (!containDelete) {
                isAllDelete = false;
                break;
            }
        }
        return isAllDelete && (eventMap.size() / mapperHotDeployManager.getMapperSize() > 0.7);
    }
    
    /**
     * 分析是新增，删除，还是修改。小于0则为删除，大于0为新增，等于0为修改
     *
     * @param events
     * @return
     */
    private int mergeKind(List<TwoTuple<String, WatchEvent<?>>> events) {
        int count = 0;
        for (TwoTuple<String, WatchEvent<?>> tuple : events) {
            WatchEvent<?> event = tuple.getSecond();
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                count++;
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                count--;
            }
        }
        return count;
    }
    
    public void close(){
        if(watcher!=null){
            try {
                watcher.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
