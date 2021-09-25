/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dnhsolution.restokabmalang.transaksi.produk_list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dnhsolution.restokabmalang.R;
import com.dnhsolution.restokabmalang.utilities.AddingIDRCurrency;
import com.dnhsolution.restokabmalang.utilities.Url;
import com.squareup.picasso.Picasso;
import com.dnhsolution.restokabmalang.utilities.CheckNetwork;

import java.io.File;
import java.util.ArrayList;

public class ProdukListAdapter extends BaseAdapter {

  private final Context mContext;
  private final ArrayList<ProdukListElement> books;

  public ProdukListAdapter(Context context, ArrayList<ProdukListElement> books) {
    this.mContext = context;
    this.books = books;
  }

  @Override
  public int getCount() {
    return books.size();
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ProdukListElement book = books.get(position);

    // view holder pattern
    if (convertView == null) {
      final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
      convertView = layoutInflater.inflate(R.layout.item_produk, parent,false);

      final ImageView imageViewCoverArt = convertView.findViewById(R.id.imageview_cover_art);
      final TextView nameTextView = convertView.findViewById(R.id.textview_book_name);
      final TextView authorTextView = convertView.findViewById(R.id.textview_book_author);
      final ImageView imageViewFavorite = convertView.findViewById(R.id.imageview_favorite);
      final TextView descriptionTextView = convertView.findViewById(R.id.tvDescription);

      final ViewHolder viewHolder = new ViewHolder(nameTextView, authorTextView, imageViewCoverArt, imageViewFavorite
              ,descriptionTextView);
      convertView.setTag(viewHolder);
    }

    final ViewHolder viewHolder = (ViewHolder)convertView.getTag();
//    viewHolder.imageViewCoverArt.setImageResource(book.getImageResource());
    viewHolder.nameTextView.setText(book.getName());
    String priceValue = new AddingIDRCurrency().formatIdrCurrencyNonKoma(Double.parseDouble(book.getPrice()));
    viewHolder.authorTextView.setText(priceValue);
    viewHolder.imageViewFavorite.setImageResource(book.isFavorite() ? R.drawable.ic_baseline_check_circle_24_dark_green : R.drawable.ic_baseline_check_circle_24_gray);
    viewHolder.descriptionTextView.setText(book.getDescription());
    String url = Url.serverFoto+book.getImageUrl();

    Glide.with(viewHolder.imageViewCoverArt.getContext()).load(url)
            .override(512,512)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .fitCenter()
            .listener(new RequestListener<Drawable>() {
              @Override
              public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.e("onLoadFailed","Error "+e);
//                viewHolder.imageViewCoverArt.setImageDrawable(mContext.getResources().getDrawable(R.drawable.img_no_image));
                return false;
              }

              @Override
              public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Log.e("onResourceReady","no Error ");
                return false;
              }
            })
            .into(viewHolder.imageViewCoverArt);
//    Log.i("url : ", url);
//    Picasso.get().load(url).fit().centerCrop().into(viewHolder.imageViewCoverArt);
    //Picasso.get().load(url).into(viewHolder.imageViewCoverArt);

    return convertView;
  }

  private class ViewHolder {
    private final TextView nameTextView;
    private final TextView authorTextView;
    private final ImageView imageViewCoverArt;
    private final ImageView imageViewFavorite;
    private final TextView descriptionTextView;

    ViewHolder(TextView nameTextView, TextView authorTextView
            , ImageView imageViewCoverArt, ImageView imageViewFavorite, TextView descriptionTextView) {
      this.nameTextView = nameTextView;
      this.authorTextView = authorTextView;
      this.imageViewCoverArt = imageViewCoverArt;
      this.imageViewFavorite = imageViewFavorite;
      this.descriptionTextView = descriptionTextView;
    }
  }
}
