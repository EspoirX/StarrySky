package com.example.xian.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzx.musiclib.manager.MusicManager;
import com.lzx.musiclib.model.MusicInfo;
import com.lzx.musiclib.service.MusicPlayService;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by xian on 2017/12/17.
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> implements Observer {

    private List<MusicInfo> musicInfos = new ArrayList<>();
    private Context mContext;

    public MusicAdapter(Context context) {
        mContext = context;
    }

    public void setMusicInfos(List<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;
        for (MusicInfo musicInfo : musicInfos) {
            MusicInfo music = MusicManager.get().getPlayingMusic();
            if (music.getMusicId().equals(musicInfo.getMusicId())) {
                if (MusicManager.get().isPlaying()) {
                    musicInfo.setPlayStatus(MusicPlayService.STATE_PLAYING);
                } else {
                    musicInfo.setPlayStatus(MusicPlayService.STATE_PAUSE);
                }
            } else {
                musicInfo.setPlayStatus(MusicPlayService.STATE_PAUSE);
            }
        }
        MusicManager.get().setMusicList(musicInfos);
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
        holder.mMusicTitle.setText(musicInfo.getMusicTitle() + "-" + musicInfo.getAlbumNickname());
        holder.mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicInfo info = MusicManager.get().getPlayingMusic();
                if (musicInfo.getMusicId().equals(info.getMusicId())) {
                    MusicManager.get().playPause();
                } else {
                    MusicManager.get().playByPosition(position);
                }
            }
        });
        if (musicInfo.getPlayStatus() == MusicPlayService.STATE_PLAYING) {
            holder.mBtn.setText("暂停");
        } else {
            holder.mBtn.setText("播放");
        }
        Glide.with(mContext)
                .load(musicInfo.getMusicCover())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.musicCover);
    }

    @Override
    public int getItemCount() {
        return musicInfos.size();
    }

    @Override
    public void update(Observable observable, Object o) {
        for (MusicInfo musicInfo : musicInfos) {
            MusicInfo music = MusicManager.get().getPlayingMusic();
            if (music.getMusicId().equals(musicInfo.getMusicId())) {
                if (MusicManager.get().isPlaying()) {
                    musicInfo.setPlayStatus(MusicPlayService.STATE_PLAYING);
                } else {
                    musicInfo.setPlayStatus(MusicPlayService.STATE_PAUSE);
                }
            } else {
                musicInfo.setPlayStatus(MusicPlayService.STATE_PAUSE);
            }
        }
        notifyDataSetChanged();
    }

    class MusicHolder extends RecyclerView.ViewHolder {
        TextView mMusicTitle;
        Button mBtn;
        ImageView musicCover;

        public MusicHolder(View itemView) {
            super(itemView);
            mMusicTitle = itemView.findViewById(R.id.textView);
            mBtn = itemView.findViewById(R.id.button);
            musicCover = itemView.findViewById(R.id.music_cover);
        }
    }
}
