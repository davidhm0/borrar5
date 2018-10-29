package com.davidhm.pqtm.borrar5;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.davidhm.pqtm.borrar5.model.BookContent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    // Contenedor de la lista de libros
    private View recyclerView;

    // Instancias de autenticación y base de datos de Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    // Parámetros para autenticación en Firebase
    private static final String mEmail = "davidhm0@yahoo.es";
    private static final String mPassword = "pqtm-davidhm";

    // Etiqueta para Logs
    private static final String TAG = "MyBooks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        // Inicializa instancias de Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;

        // Comprueba si el usuario ya está autenticado en Firebase
        //mAuth.signOut();
        //BookContent.BookItem.deleteAll(BookContent.BookItem.class);
        if (mAuth.getCurrentUser() == null ) {
            // No hay mingún usuario autenticado -> intenta hacer login
            signIn(mEmail, mPassword);
        } else {
            // El usuario ya está autenticado -> pide libros al servidor
            Log.d(TAG, "onCreate:usuario autenticado previamente");
            getFirebaseBookList();
        }
    }

    /**
     * Intenta hacer login en el servidor Firebase.
     * Si la autenticación tiene éxito, pide la lista de libros al servidor.
     * Si falla, carga en el Adapter la lista de libros guardada en la base
     * de datos local.
     *
     * @param email     email de autenticación en Firebase
     * @param password  contraseña de autenticación en Firebase
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Autenticación en Firebase correcta -> pide libros al servidor
                            Log.d(TAG, "signIn:usuario autenticado correctamente");
                            getFirebaseBookList();
                        } else {
                            // La autenticación falla -> muestra un mensaje al usuario.
                            Log.w(TAG, "signIn:error de autenticación", task.getException());
                            Toast.makeText(BookListActivity.this, "Error de autenticación en Firebase.",
                                    Toast.LENGTH_SHORT).show();
                            // Carga los libros de la base de datos local en el Adapter
                            setupRecyclerView((RecyclerView) recyclerView);
                        }
                    }
                });
    }

    /**
     * Pide la lista de libros al servidor Firebase, y asigna un listener a
     * la referencia obtenida. El método onDataChange es invocado por primera
     * vez cuando se asigna el listener, y cada vez que hay modificaciones en
     * los datos a los que apunta la referencia.
     */
    private void getFirebaseBookList() {
        database.getReference("books").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Recibe modificaciones de la lista de libros de Firebase
                // y actualiza la base de datos local.
                Log.d(TAG, "onDataChange:recibidos libros de Firebase");
                updateLocalDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Error en el acceso a la base de datos Firebase.
                Log.w(TAG, "onCancelled:error en acceso a BBDD Firebase", databaseError.toException());
                // Carga libros de la base de datos local en el Adapter
                setupRecyclerView((RecyclerView) recyclerView);
            }
        });
    }

    /**
     * Actualiza la base de datos local con la lista recibida de Firebase.
     * Solo añade a la base de datos aquellos libros que no estaban
     * previamente almacenados.
     *
     * @param dataSnapshot la lista de libros de Firebase
     */
    private void updateLocalDatabase(DataSnapshot dataSnapshot) {
        GenericTypeIndicator<List<BookContent.BookItem>> gtiBookList =
                new GenericTypeIndicator<List<BookContent.BookItem>>() {};
        Log.d(TAG, "Libros atualmente en la base de datos SugarORM: "
                + BookContent.BookItem.count(BookContent.BookItem.class));
        for (BookContent.BookItem book: BookContent.getBooks()) {
            Log.d(TAG, "SugarID = " + book.getId()
                    + "; identificador = " + book.getIdentificador()
                    + "; title = " + book.getTitle()
                    + "; author = " + book.getAuthor()
                    + "; fecha de publicación = " + book.getPublicationDate()
                    + "; urlImagen = " + book.getUrlImage());
        }
        for (BookContent.BookItem fbBook: dataSnapshot.getValue(gtiBookList)) {
            if (!BookContent.exists(fbBook)) {
                // Añade nuevo libro a la base de datos local
                fbBook.save();
                // Asigna el id del nuevo registro al campo 'identificador' del libro
                fbBook.setIdentificador(fbBook.getId().intValue());
                fbBook.update();
            }
        }
        // Carga libros de la base de datos local actualizada en el Adapter
        setupRecyclerView((RecyclerView) recyclerView);
    }

    /**
     * Carga los libros almacenados en la base de datos local, en el Adapter
     * del ReciclerView.
     *
     * @param recyclerView  el layout que contiene la lista de libros
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        Log.d(TAG, "setupRecyclerView:cargando datos en adapter (" + BookContent.getBooks().size() + " libros)");
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(BookContent.getBooks()));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        //private final BookListActivity mParentActivity;
        private final List<BookContent.BookItem> mValues;
        //private final boolean mTwoPane;
        /*private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookContent.BookItem item = (BookContent.BookItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));
                    BookDetailFragment fragment = new BookDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, BookDetailActivity.class);
                    intent.putExtra(BookDetailFragment.ARG_ITEM_ID, String.valueOf(item.getIdentificador()));

                    context.startActivity(intent);
                }
            }
        };*/

        SimpleItemRecyclerViewAdapter(List<BookContent.BookItem> items) {
            mValues = items;
            //mParentActivity = parent;
            //mTwoPane = twoPane;
        }

        /**
         * Actualiza la lista de libros del Adapter con los libros de la base
         * de datos local.
         *
         * @param items la lista de libros almacenados en la base de datos
         */
        public void setItems(List<BookContent.BookItem> items) {
            // ============ INICIO CODIGO A COMPLETAR ===============
            mValues.clear();
            mValues.addAll(items);
            // ============ FIN CODIGO A COMPLETAR ===============
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(String.valueOf(mValues.get(position).getIdentificador()));
            holder.mContentView.setText(mValues.get(position).getTitle());

            holder.itemView.setTag(mValues.get(position));
            //holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
