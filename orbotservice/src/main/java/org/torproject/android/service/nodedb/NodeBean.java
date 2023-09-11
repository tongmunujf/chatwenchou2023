package org.torproject.android.service.nodedb;

import java.io.Serializable;

public class NodeBean implements Serializable {
    private String guard_node;
    private String valid_time;
    public NodeBean(String guard_node, String valid_time){
        this.guard_node = guard_node;
        this.valid_time = valid_time;
    }

    public String getGuard_node() {
        return guard_node;
    }

    public void setGuard_node(String guard_node) {
        this.guard_node = guard_node;
    }

    public String getValid_time() {
        return valid_time;
    }

    public void setValid_time(String valid_time) {
        this.valid_time = valid_time;
    }

    @Override
    public String toString() {
        return "NodeBean{" +
                "guard_node='" + guard_node + '\'' +
                ", valid_time='" + valid_time + '\'' +
                '}';
    }
}
