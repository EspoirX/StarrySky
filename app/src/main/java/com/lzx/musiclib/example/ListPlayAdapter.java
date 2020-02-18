package com.lzx.musiclib.example;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lzx.musiclib.R;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class ListPlayAdapter extends RecyclerView.Adapter<ListPlayAdapter.ListPlayHolder> {
    private List<SongInfo> mSongInfos = new ArrayList<>();
    private Context mContext;
    private static final int RED = 0xffFF8080;
    private static final int BLUE = 0xff8080FF;
    private static final int CYAN = 0xff80ffff;
    private static final int GREEN = 0xff80ff80;

    public ListPlayAdapter(Context context) {
        mContext = context;
    }

    public void setSongInfos(List<SongInfo> songInfos) {
        mSongInfos.clear();
        mSongInfos.addAll(songInfos);
        notifyDataSetChanged();
    }

    public List<SongInfo> getSongInfos() {
        return mSongInfos;
    }

    @NonNull
    @Override
    public ListPlayHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_play_list, viewGroup, false);
        return new ListPlayHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPlayHolder holder, int position) {
        SongInfo songInfo = mSongInfos.get(position);
        Glide.with(mContext).load(songInfo.getSongCover()).into(holder.cover);
        holder.title.setText(songInfo.getSongName());
        long progress = StarrySky.with().getPlayingPosition();
        long duration = StarrySky.with().getDuration();

        if (StarrySky.with().isCurrMusicIsPlaying(songInfo.getSongId())) {
            holder.itemView.setBackgroundColor(Color.GREEN);
            holder.state.setText("状态:播放中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration));
        } else if (StarrySky.with().isCurrMusicIsPaused(songInfo.getSongId())) {
            holder.itemView.setBackgroundColor(Color.GREEN);
            holder.state.setText("状态:暂停中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.state.setText("");
        }
        holder.itemView.setOnClickListener(v -> {
            StarrySky.with().playMusicByIndex(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (StarrySky.with().isCurrMusicIsPlaying(songInfo.getSongId())) {
                StarrySky.with().playMusicByIndex(position + 1);
            }
            StarrySky.with().removeSongInfo(songInfo.getSongId());
            notifyItemRemoved(position);
            mSongInfos.remove(position);
            Toast.makeText(mContext, "移除歌曲：" + songInfo.getSongName(), Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ListPlayHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        SongInfo songInfo = mSongInfos.get(position);
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            long progress = StarrySky.with().getPlayingPosition();
            long duration = StarrySky.with().getDuration();
            if (StarrySky.with().isCurrMusicIsPlaying(songInfo.getSongId())) {
                holder.state.setText("状态:播放中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration));
            } else if (StarrySky.with().isCurrMusicIsPaused(songInfo.getSongId())) {
                holder.state.setText("状态:暂停中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration));
            } else {
                holder.state.setText("");
            }
        }
    }

    public void updateItemProgress(int position) {
        notifyItemChanged(position, "position");
    }

    @Override
    public int getItemCount() {
        return mSongInfos.size();
    }

    class ListPlayHolder extends RecyclerView.ViewHolder {

        ImageView cover;
        TextView title, state;

        ListPlayHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            title = itemView.findViewById(R.id.title);
            state = itemView.findViewById(R.id.state);
        }
    }

    public static String formatMusicTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((int) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
