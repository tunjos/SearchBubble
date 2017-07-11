package com.tunjos.searchbubble.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.activities.MainActivity;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.others.itemtouch.ItemTouchHelperAdapter;
import com.tunjos.searchbubble.others.itemtouch.ItemTouchHelperViewHolder;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tunjos on 22/06/2015.
 */
public class ClipListAdapter extends RealmRecyclerViewAdapter<Clip> implements ItemTouchHelperAdapter {
    private final SimpleDateFormat sdf;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemDismissListener onItemDismissListener;
    private int colorSbBlue;
    private boolean loadMiniClipList;

    public ClipListAdapter(Context context, boolean loadMiniClipList) {
        this.loadMiniClipList = loadMiniClipList;
        sdf = new SimpleDateFormat(MyConstants.DATE_FORMAT, Locale.getDefault());
        if (context instanceof MainActivity) {
            onItemDismissListener = (MainActivity) context;
        }
        colorSbBlue = context.getResources().getColor(R.color.sb_blue);
    }

    @Override
    public void onItemDismiss(int position) {
        onItemDismissListener.onItemDismiss(position);
        notifyItemRemoved(position);
    }

    public class ClipViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, ItemTouchHelperViewHolder {
        @BindView(R.id.tvText) TextView tvText;
        @BindView(R.id.tvDate) TextView tvDate;
        @Nullable @BindView(R.id.imgvType) ImageView imgvType;

        public ClipViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(v,getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onItemSelected() {
//            ((CardView)itemView).setCardBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
//            ((CardView)itemView).setCardBackgroundColor(colorSbBlue);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView;
        if (loadMiniClipList) {
            inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clip_list_mini, parent, false);
        } else {
            inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clip_list, parent, false);
        }
        ClipViewHolder viewHolder = new ClipViewHolder(inflatedView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ClipViewHolder clipViewHolder = (ClipViewHolder) holder;
        Clip clip = getItem(position);

        String date = sdf.format(clip.getCreationDate().getTime());
        clipViewHolder.tvText.setText(clip.getText());
        clipViewHolder.tvDate.setText(date);

        if (!loadMiniClipList) {
            clipViewHolder.imgvType.setImageResource(getImageTypeResource(clip.getType()));
            clipViewHolder.imgvType.setBackgroundResource(getImageTypeBackground(clip.getType()));
        }
    }

    private int getImageTypeResource(int type) {
        switch (type) {
            case MyConstants.CLIPTYPE_TEXT:
                return R.drawable.ic_text;
            case MyConstants.CLIPTYPE_NO:
                return R.drawable.ic_number;
            case MyConstants.CLIPTYPE_URL:
                return R.drawable.ic_link;
            default:
                return R.drawable.ic_text;
        }
    }

    private int getImageTypeBackground(int type) {
        switch (type) {
            case MyConstants.CLIPTYPE_TEXT:
                return R.drawable.circle_text_bg;
            case MyConstants.CLIPTYPE_NO:
                return R.drawable.circle_number_bg;
            case MyConstants.CLIPTYPE_URL:
                return R.drawable.circle_link_bg;
            default:
                return R.drawable.circle_text_bg;
        }
    }

    @Override
    public int getItemCount() {
        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    public interface OnItemDismissListener {
        void onItemDismiss(int position);
    }

}