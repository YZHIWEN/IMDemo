package com.yzw.demoim.im;

import com.yzw.demoim.bean.Friend;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzw on 2016/3/17 0017.
 */
public class IMAdpter {

    // 默认组名
    private final String DEFAULT_GROUP_NAME = "我的好友";

    private IMManger imManger;

    public IMAdpter(IMManger imManger) {
        this.imManger = imManger;
    }

    public List<Friend> getFriends() {
        List<Friend> list = new ArrayList<>();
        List<RosterEntry> res = imManger.getRosterEntrys();
        for (RosterEntry re : res) {
            Presence p = imManger.getPresence(re.getUser());
            Friend f = new Friend();
            f.setUsername(re.getUser());
            f.setName(re.getName());
            f.setMode(p.getMode().toString());
            List<RosterGroup> rgs = re.getGroups();
            String gn = rgs.size() > 0 ? rgs.get(0).getName() : DEFAULT_GROUP_NAME;
            f.setGroupname(gn);
            list.add(f);
        }
        return list;
    }


}
