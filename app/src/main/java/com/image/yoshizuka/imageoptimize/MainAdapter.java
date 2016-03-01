package com.image.yoshizuka.imageoptimize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by yoshizuka on 28/02/16.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainHolder> {

    private Context context;

    private List<MainObject> mainList;

    private OnMainAdapterListener onMainAdapterListener;

    public MainAdapter(Context context) {
        this.context = context;
        if(context instanceof OnMainAdapterListener)
            onMainAdapterListener = (OnMainAdapterListener)context;
        else throw new RuntimeException(context.toString() + " doit impélementer OnMainAdapterListener");
    }

    /**
     * Met à jour la liste des données
     * @param mainList
     */
    public void setMainList(List<MainObject> mainList) {
        this.mainList = mainList;
        notifyDataSetChanged();
    }

    /**
     * Met à jour la liste des données
     * @param mainList
     */
    public void setMainList(List<MainObject> mainList, int position) {
        this.mainList = mainList;
        notifyItemInserted(position);
        notifyItemChanged(position);
    }

    /**
     * Met à jour la liste des données
     * @param mainList
     */
    public void updateMainList(List<MainObject> mainList, int position) {
        this.mainList = mainList;
        notifyItemChanged(position);
    }

    /**
     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p/>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p/>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public MainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.adapter_main, parent, false);
        return new MainHolder(contentView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p/>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle effcient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MainHolder holder, final int position) {
        holder.image.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mainList.get(position).getImageOptimize().getHeight()));
        //Glide.with(context).load(mainList.get(position).getImageOptimize().getStream().toByteArray()).dontAnimate().dontTransform().into(holder.image);
        ImageOptimize optimize = mainList.get(position).getImageOptimize();
        if(optimize.getStream() == null) {
            holder.image.setLayoutParams(new RelativeLayout.LayoutParams(optimize.getWidth(), optimize.getHeight()));
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.empty);
            holder.image.setImageBitmap(Bitmap.createScaledBitmap(bitmap, optimize.getWidth(), optimize.getHeight(), false));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.image.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            holder.image.setLayoutParams(params);

        } else {
            Glide.with(context).load(mainList.get(position).getImageOptimize().getStream().toByteArray()).dontAnimate().into(holder.image);
        }
        holder.size.setText("Taille d'origine : ".concat(MainObject.humanReadableByteCount(mainList.get(position).getSize())).concat("\nTaille compressée : ").concat(optimize.size() == 0 ? "calcul..." : MainObject.humanReadableByteCount(optimize.size())).concat("\nCompression : ".concat(String.valueOf(optimize.getRatio())).concat("%")));

        holder.bar.setProgress(mainList.get(position).getImageOptimize().getRatio());
        holder.bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                holder.size.setText("Taille d'origine : ".concat(MainObject.humanReadableByteCount(mainList.get(position).getSize())).concat("\nTaille compressée : ").concat(MainObject.humanReadableByteCount(mainList.get(position).getImageOptimize().size())).concat("\nCompression : ".concat(String.valueOf(progress)).concat("%")));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int progress = seekBar.getProgress();
                Compress compress = Compress.getInstance(context);
                compress.setOnCompressListener(new Compress.OnCompressListener() {
                    @Override
                    public void onCompress(MainObject mainObject) {
                        mainObject.getImageOptimize().setRatio(progress);
                        holder.size.setText("Taille d'origine : ".concat(MainObject.humanReadableByteCount(mainList.get(position).getSize())).concat("\nTaille compressée : ").concat(MainObject.humanReadableByteCount(mainObject.getImageOptimize().size())).concat("\nCompression : ".concat(String.valueOf(progress)).concat("%")));
                        Glide.with(context).load(mainObject.getImageOptimize().getStream().toByteArray()).into(holder.image);
                        onMainAdapterListener.onUpdateRatio(position, mainObject);
                    }

                    @Override
                    public void onCompressStart(MainObject mainObject) {
                        mainObject.getImageOptimize().setRatio(progress);
                        holder.size.setText("Taille d'origine : ".concat(MainObject.humanReadableByteCount(mainList.get(position).getSize())).concat("\nTaille compressée : calcul...").concat("\nCompression : ").concat(String.valueOf(progress)).concat("%"));
                    }

                    @Override
                    public void onCompressCancel() {
                    }
                });
                compress.compressImage(mainList.get(position).getImagePath(), progress);
            }
        });
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mainList != null ? mainList.size() : 0;
    }

    protected class MainHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView size;
        public SeekBar bar;

        public MainHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.adapter_image);
            size = (TextView) itemView.findViewById(R.id.adapter_size);
            bar = (SeekBar) itemView.findViewById(R.id.adapter_bar);
        }
    }

    public interface OnMainAdapterListener {
        void onUpdateRatio(int position, MainObject mainObject);
    }

}
