package com.example.xian.myapplication;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.musiclib.manager.MusicManager;
import com.example.musiclib.model.MusicInfo;
import com.example.musiclib.service.MusicPlayService;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by xian on 2017/12/17.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> implements Observer {

    private List<MusicInfo> musicInfos = new ArrayList<>();

    public void setMusicInfos(List<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;

        MusicManager.setMusicList(musicInfos);
        notifyDataSetChanged();
    }

    @Override
    public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new MusicHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicHolder holder, final int position) {
        final MusicInfo musicInfo = musicInfos.get(position);
        holder.mMusicTitle.setText("歌曲_" + (position + 1));
        holder.mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicInfo info = MusicManager.getPlayingMusic();
                if (info != null && info.getMusicUrl().equals(musicInfo.getMusicUrl())) {
                    MusicManager.playPause();
                } else {
                    MusicManager.playByPosition(position);
                }
            }
        });

        if (musicInfo.getPlayStatus() == MusicPlayService.STATE_PLAYING) {
            holder.mBtn.setText("暂停");
        } else {
            holder.mBtn.setText("播放");
        }
    }

    @Override
    public int getItemCount() {
        return musicInfos.size();
    }

    @Override
    public void update(Observable observable, Object o) {
        int status = (int) o;
        Log.i("xian", "收到通知 = " + status);
        for (int i = 0; i < musicInfos.size(); i++) {
            if (i == MusicManager.getPlayingPosition()) {
                musicInfos.get(i).setPlayStatus(status);
            } else {
                musicInfos.get(i).setPlayStatus(MusicPlayService.STATE_PAUSE);
            }
        }
        notifyDataSetChanged();
    }

    class MusicHolder extends RecyclerView.ViewHolder {
        TextView mMusicTitle;
        Button mBtn;

        public MusicHolder(View itemView) {
            super(itemView);
            mMusicTitle = itemView.findViewById(R.id.textView);
            mBtn = itemView.findViewById(R.id.button);
        }
    }
}
