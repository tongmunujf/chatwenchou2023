package com.ucas.chat.ui.listener;

import com.ucas.chat.bean.MsgListBean;
import com.ucas.chat.bean.session.message.IMMessage;
import com.ucas.chat.ui.view.chat.RViewHolder;

public interface OnItemClickListener {
    void onItemClick(RViewHolder holder, MsgListBean message);
}
