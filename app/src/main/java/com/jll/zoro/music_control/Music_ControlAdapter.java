package com.jll.zoro.music_control;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * @Author : Zoro.
 * @Date : 2017/4/6.
 * @Describe :
 */

public class Music_ControlAdapter extends BaseAdapter {
    private Context context;
    private List<Boolean> list;
    private int num = -1;
    //num_false用于结束歌曲时只更改播放状态不改变背景，原有的方法只要notify传入的position没有就全部变白色背景
    private int num_False = -1;
    //用于判断点击的item是否是同一个，同一个继续播放，不同的话重新播放
    private int num_Before = -1;
    private int num_Now = -1;
    private boolean isEnding = false;

    private showMusic_Control showMusic_control;
    private startMusic_Control startMusic_control;
    private pauseMusic_Control pauseMusic_control;
    private restartMusic_Control restartMusic_control;

    //更改每个item背景、更改每首歌的状态，点击一首歌，其他歌暂停,isEnding表示换了播放状态后item背景颜色要不要变化
    public void notify(int position, boolean isEnding) {
        this.num = position;
        this.isEnding = isEnding;
        if (isEnding) {
            this.num_False = num;
        }
        notifyDataSetChanged();
    }

    //刷新以后重置数据进行新一轮的开始
    public void changeState() {
        num_Before = -1;
        num_Now = -1;
    }


    public Music_ControlAdapter(Context context, List<Boolean> list, showMusic_Control showMusic_control, startMusic_Control startMusic_control, pauseMusic_Control pauseMusic_control, restartMusic_Control restartMusic_control) {
        this.context = context;
        this.list = list;
        this.showMusic_control = showMusic_control;
        this.startMusic_control = startMusic_control;
        this.pauseMusic_control = pauseMusic_control;
        this.restartMusic_control = restartMusic_control;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int index = position;
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.music_control_item, null);
            holder.phone_Number = (TextView) convertView.findViewById(R.id.phone_Number);
            holder.call_Time = (TextView) convertView.findViewById(R.id.call_Time);
            holder.call_Nopass = (TextView) convertView.findViewById(R.id.call_Nopass);
            holder.call_description = (TextView) convertView.findViewById(R.id.call_description);
            holder.call_Pass = (ImageView) convertView.findViewById(R.id.call_Pass);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            convertView.setTag(R.id.music_control_view, holder);
        } else {
            holder = (Music_ControlAdapter.ViewHolder) convertView.getTag(R.id.music_control_view);
        }
        holder.phone_Number.setText("来电号码===" + list.get(position));
        convertView.setTag(R.id.music_control_position, position);
       /*
        *当notify传入的值等于position表示点击了当前item，如果正在播放则停止，因为正在播放的背景显示灰色，
        *所以这边不再更改，当不是正在播放中状态则更改背景，同时把当前item 以外的item全部变成白底黑字
        */
        if (position == num) {
            if (!list.get(position) && holder.call_Pass.getDrawable().getCurrent().getConstantState().equals(ContextCompat.getDrawable(context, R.drawable.item_start).getConstantState())) {
                holder.call_Pass.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.item_stop));
                holder.phone_Number.setTextColor(ContextCompat.getColor(context, R.color.blue_alculator));
                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
            } else if(list.get(position) && holder.call_Pass.getDrawable().getCurrent().getConstantState().equals(ContextCompat.getDrawable(context, R.drawable.item_stop).getConstantState())){
                holder.call_Pass.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.item_start));
                holder.phone_Number.setTextColor(ContextCompat.getColor(context, R.color.blue_alculator));
                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
            }else{
                holder.phone_Number.setTextColor(ContextCompat.getColor(context, R.color.blue_alculator));
                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
            }
        } else {
            if (position != num_False) {
                holder.call_Pass.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.item_stop));
                holder.phone_Number.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                holder.call_Pass.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.item_stop));
                holder.phone_Number.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
            }
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num_Now = (int) v.getTag(R.id.music_control_position);
                for(int i=0;i<list.size();i++){
                    if(i==num_Now){
                    }else{
                        list.set(i,false);
                    }
                }
                if(list.get(num_Now)){
                    list.set(num_Now,false);
                }else{
                    list.set(num_Now,true);
                }
                if (num_Before == num_Now) {
                    if (holder.call_Pass.getDrawable().getCurrent().getConstantState().equals(ContextCompat.getDrawable(context, R.drawable.item_start).getConstantState())) {
                        pauseMusic_control.pause(index);
                    } else {
                        restartMusic_control.restart(index);
                    }
                } else {
                    startMusic_control.start(index);
                }
                num_Before = num_Now;
                showMusic_control.show(index);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView phone_Number;
        TextView call_Time;
        TextView call_Nopass;
        TextView call_description;
        ImageView call_Pass;
        LinearLayout layout;
    }

    //第一个作用控制板最开始隐藏，点击按钮显示，第二个作用循环列表把不是点击item的都变成停止播放
    public interface showMusic_Control {
        void show(int index);
    }

    //startMusic_Control和stopMusic_Control用于判断点击是否和上一个相同，相同则在原来的基础上播放，不然重新播放
    public interface startMusic_Control {
        void start(int index);
    }

    public interface pauseMusic_Control {
        void pause(int index);
    }

    public interface restartMusic_Control {
        void restart(int index);
    }
}
