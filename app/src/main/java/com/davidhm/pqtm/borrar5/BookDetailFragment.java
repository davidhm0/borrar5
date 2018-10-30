package com.davidhm.pqtm.borrar5;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidhm.pqtm.borrar5.model.BookContent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private BookContent.BookItem mItem;

    private ImageView imageView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = BookContent.BookItem.findById(BookContent.BookItem.class,
                    Integer.valueOf(getArguments().getString(ARG_ITEM_ID)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Conserva el título cuando se gira el aparato
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mItem.getTitle());
        }

        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Muestra el contenido de prueba.
        if (mItem != null) {
            //Recupera la url de imagen de portada.
            String url =  mItem.getUrlImage();
            imageView = (ImageView) rootView.findViewById(R.id.book_image);
            new downloadImage().execute(url);
            // Obtiene el identificador de la imagen.
            //int img = getResources().getIdentifier(name, "drawable",
            //        rootView.getContext().getPackageName());
            //Obtiene la fecha, en formato dd/MM/yyyy.
            String date = new SimpleDateFormat("dd/MM/yyyy").format(mItem.getPublicationDate());
            // Muestra los detalles del contenido
            //((ImageView) rootView.findViewById(R.id.book_image)).
             //       setImageResource(img);
            ((TextView) rootView.findViewById(R.id.book_author)).
                    setText(mItem.getAuthor());
            ((TextView) rootView.findViewById(R.id.book_date)).
                    setText(date);
            ((TextView) rootView.findViewById(R.id.book_description)).
                    setText(mItem.getDescription());
        }

        return rootView;
    }

    private class downloadImage extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap image = null;
            URL imageUrl;
            try {
                imageUrl = new URL(url[0]);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                image = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap map) {
            super.onPostExecute(map);

            imageView.setImageBitmap(map);
        }
    }
}
