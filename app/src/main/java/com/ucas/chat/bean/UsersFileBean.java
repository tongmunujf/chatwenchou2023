package com.ucas.chat.bean;

import java.io.Serializable;
import java.util.List;

public class UsersFileBean implements Serializable {

    private List<UsersBean> users;

    public List<UsersBean> getUsers() {
        return users;
    }

    public void setUsers(List<UsersBean> users) {
        this.users = users;
    }

    public static class UsersBean {
        /**
         * userId :
         * name : Porsche
         * orion :
         */
        private String userId;
        private String name;
        private String orion;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOrion() {
            return orion;
        }

        public void setOrion(String orion) {
            this.orion = orion;
        }
    }
}
