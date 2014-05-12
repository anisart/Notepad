package ru.anisart.notebook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

public class NotebookActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = NotebookActivity.class.getSimpleName();
    private static final int PAGE_LIMIT = 2;

    private View contentView;
    private ExpandedSlidingDrawer slidingDrawer;
    private NotesAdapter pagerAdapter;

    private int columnIndexRowId;
    private int columnIndexTextContent;

    private int currentPagePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        contentView = findViewById(R.id.contentView);
        slidingDrawer = (ExpandedSlidingDrawer) findViewById(R.id.drawer);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int windowHeight = contentView.getMeasuredHeight();
                int menuOffset = getResources().getDimensionPixelSize(R.dimen.menu_offset);
                slidingDrawer.setBottomOffset(menuOffset - windowHeight);
            }
        });
        int menuButtonHeight = getResources().getDimensionPixelSize(R.dimen.menu_button_height);
        slidingDrawer.setTouchableContentHeight(menuButtonHeight);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new NotesAdapter(this, getSupportFragmentManager(), null);
        pager.setAdapter(pagerAdapter);
        int margin = getResources().getDimensionPixelSize(R.dimen.page_padding) * 2;
        pager.setPageMargin(-margin);
        pager.setOffscreenPageLimit(PAGE_LIMIT);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
                currentPagePosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
//                Log.d(TAG, "onPageScrolled, positionOffset = " + positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged, state = " + state);
            }
        });

        View sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "send email");
                EditText textField = (EditText) pagerAdapter.getFragmentAt(currentPagePosition).getView()
                        .findViewById(R.id.textField);
                String textContent = textField.getText().toString();
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("") +
                        "?subject=" + Uri.encode("") +
                        "&body=" + Uri.encode(textContent);
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send mail..."));
                slidingDrawer.open();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onBackPressed() {
        if (!slidingDrawer.isOpened()) {
            slidingDrawer.open();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader " + i);
        Uri uri = NotesProvider.CONTENT_URI;
        String projection[] = {NotesProvider.ROW_ID, NotesProvider.TEXT_CONTENT};
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");
        pagerAdapter.swapCursor(cursor);
        columnIndexRowId = cursor.getColumnIndex(NotesProvider.ROW_ID);
        columnIndexTextContent = cursor.getColumnIndex(NotesProvider.TEXT_CONTENT);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG, "onLoaderReset");
        pagerAdapter.swapCursor(null);
    }

    private class NotesAdapter extends CursorFragmentPagerAdapter {

        public NotesAdapter(Context context, FragmentManager fm, Cursor cursor) {
            super(context, fm, cursor);
        }

        public Fragment getFragmentAt(int currentPosition) {
            return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":"
                    + getItemId(currentPosition));
        }

        @Override
        public Fragment getItem(Context context, Cursor cursor) {
            long rowId = cursor.getLong(columnIndexRowId);
            String textContent = cursor.getString(columnIndexTextContent);
            return PageFragment.newInstance(rowId, textContent);
        }
    }

}
