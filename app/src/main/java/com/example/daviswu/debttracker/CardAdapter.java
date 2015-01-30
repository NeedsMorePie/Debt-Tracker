package com.example.daviswu.debttracker;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

/**
 * Created by Davis Wu on 2015-01-13.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ItemViewHolder> {

    private List<Item> itemList;

    public CardAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int i) {
        Item newItem = itemList.get(i);

        itemViewHolder.vName.setText(newItem.name);
        itemViewHolder.vItemOwed.setText(newItem.itemOwed);
        itemViewHolder.vStatus.setText(newItem.status);
        if (newItem.status.matches("Owed to")) {
            itemViewHolder.vStatus.setTextColor(Color.parseColor("#980000"));        }
        else {
            itemViewHolder.vStatus.setTextColor(Color.parseColor("#0b9800"));
        }

        if (newItem.imgPath != null && newItem.imgPath.getPath().length() != 0) {
            loadBitmap(newItem.imgPath.getAbsolutePath(), itemViewHolder.vImg);
        }
        else {
            String dir = Environment.getExternalStorageDirectory().toString()+File.separator+"money.jpg";
            File file = new File(dir);
            loadBitmap(file.getAbsolutePath(), itemViewHolder.vImg);
        }

        if (newItem.phone.length() == 0) {
            itemViewHolder.vPhone.setText("--");
        }
        else {
            itemViewHolder.vPhone.setText(newItem.phone);
        }
        if (newItem.email.length() == 0) {
            itemViewHolder.vEmail.setText("--");
        }
        else {
            itemViewHolder.vEmail.setText(newItem.email);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_layout, viewGroup, false);
        return new ItemViewHolder(itemView);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vItemOwed;
        protected TextView vStatus;
        protected ImageView vDelete;
        protected TextView vPhone;
        protected TextView vEmail;
        protected ImageView vImg;
        protected CardView vCardView;
        protected FrameLayout vScroll;
        protected LinearLayout vLayoutCard;
        public ItemViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.name);
            vItemOwed = (TextView) v.findViewById(R.id.item_owed);
            vStatus = (TextView) v.findViewById(R.id.status_text);
            vDelete = (ImageView) v.findViewById(R.id.delete);
            vPhone = (TextView) v.findViewById(R.id.phone_text);
            vEmail = (TextView) v.findViewById(R.id.email_text);
            vImg = (ImageView) v.findViewById(R.id.item_preview);
            vScroll = (FrameLayout) v.findViewById(R.id.scroll_view);
            vCardView = (CardView) v.findViewById(R.id.card_view_layout);
            vLayoutCard = (LinearLayout) v.findViewById(R.id.layout_card);

            vLayoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditClick(v);
                }
            });
            vDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteClick(v);
                }
            });
        }

        public void onDeleteClick(View v) {
            ((MainActivity) v.getContext()).delete(getPosition());
        }

        public void onEditClick(View v) {
            ((MainActivity) v.getContext()).edit(getPosition(), vImg);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return rotateBitmap(decodeSampledBitmapFromPath(data, 200, 400), 90);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public void loadBitmap(String path, ImageView imageView) {
        if (cancelPotentialWork(path, imageView)) {
            Bitmap placeHolderBitmap = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.grey);
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(Resources.getSystem(), placeHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(path);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == "" || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}
