package com.lzx.musiclib.example;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    private ValueAnimator colorAnim;

    public ListPlayAdapter(Context context) {
        mContext = context;
    }

    public void setSongInfos(List<SongInfo> songInfos) {
        mSongInfos = songInfos;
        notifyDataSetChanged();
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
        holder.state.setText("状态");
        if (StarrySky.with().isCurrMusicIsPlaying(songInfo.getSongId())) {
            initAnim(holder.itemView, RED, BLUE, CYAN, GREEN);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StarrySky.with().playMusic(mSongInfos, position);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSongInfos.size();
    }

    public void initAnim(View view, int... values) {
        if (colorAnim != null) {
            colorAnim.cancel();
        }
        colorAnim = ObjectAnimator.ofInt(view, "backgroundColor", values);
        colorAnim.setDuration(3000);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(ValueAnimator.INFINITE);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.start();
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

}
