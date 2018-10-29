package com.davidhm.pqtm.borrar5.model;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class BookContent {

    // Etiqueta para Logs
    private static final String TAG = "MyBooks";

    /**
     * An array of sample (dummy) items.
     */
    public static final List<BookItem> ITEMS = new ArrayList<BookItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, BookItem> ITEM_MAP = new HashMap<String, BookItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(BookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.getIdentificador()), item);
    }

    private static BookItem createDummyItem(int position) {
        return new BookItem(position, "Item " + position, "Author" + position,
                makeDate(position), makeDetails(position), "URL_book_" + position);
    }

    private static Date makeDate(int position) {
        Calendar calendar = new GregorianCalendar(2016, 6, 29 + position);
        return calendar.getTime();
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static List<BookItem> getBooks(){
        // ============ INICIO CODIGO A COMPLETAR ===============
        return BookItem.listAll(BookItem.class);
        // ============ FIN CODIGO A COMPLETAR ===============
    }

    public static boolean exists(BookItem bookItem) {
        // ============ INICIO CODIGO A COMPLETAR ===============
        if (bookItem == null) return false;
        if (BookItem.find(BookItem.class, "title = ?", bookItem.getTitle()).isEmpty()) {
            return false;
        } else {
            return true;
        }
        // ============ FIN CODIGO A COMPLETAR ===============
    }

    /**
     * Clase que define la estructura de cada uno de los elementos a mostrar en el
     * catálogo de libros.
     */
    public static class BookItem extends SugarRecord {

        @Unique
        private int identificador;
        private String title;
        private String author;
        private Date publicationDate;
        private String description;
        private String urlImage;

        // Constructor por defecto, vacío
        public BookItem() {

        }

        public BookItem(int identificador, String title, String author, Date publicationDate,
                        String description, String urlImage) {
            this.identificador = identificador;
            this.title = title;
            this.author = author;
            this.publicationDate = publicationDate;
            this.description = description;
            this.urlImage = urlImage;
        }

        public int getIdentificador() {
            return identificador;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public Date getPublicationDate() {
            return publicationDate;
        }

        public String getDescription() {
            return description;
        }

        public String getUrlImage() {
            return urlImage;
        }

        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }

        /**
         * Llamado al importar datos de Firebase.
         * Lee el campo 'publicationDate' de la BBDD de Firebase, como cadena
         * de texto con formato dd/MM/yyyy, y lo asgina al mismo campo de
         * BookItem, en formato Date.
         *
         * @param publicationDate   fecha en formato dd/MM/yyyy
         */
        public void setPublicationDate(String publicationDate) {
            try {
                this.publicationDate = new SimpleDateFormat("dd/MM/yyyy").parse(publicationDate);
            }
            catch (ParseException ex)
            {
                Log.w(TAG, "setPublicationDate:formato de fecha incorrecto", ex);
            }
        }
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;

        public DummyItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
